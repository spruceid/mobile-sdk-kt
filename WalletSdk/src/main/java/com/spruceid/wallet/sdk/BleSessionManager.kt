package com.spruceid.wallet.sdk

import android.bluetooth.BluetoothManager
import android.util.Log
import com.spruceid.wallet.sdk.rs.ItemsRequest
import com.spruceid.wallet.sdk.rs.SessionManager
import com.spruceid.wallet.sdk.rs.SessionManagerEngaged
import com.spruceid.wallet.sdk.rs.initialiseSession
import com.spruceid.wallet.sdk.rs.handleRequest
import com.spruceid.wallet.sdk.rs.submitResponse
import com.spruceid.wallet.sdk.rs.submitSignature
import java.security.KeyStore
import java.security.Signature
import java.util.UUID

abstract class BLESessionStateDelegate {
    abstract fun update(state: Map<String, Any>)
}

public class BLESessionManager {

    val callback: BLESessionStateDelegate
    val uuid: UUID
    var state: SessionManagerEngaged? = null
    var sessionManager: SessionManager? = null
    var itemsRequests: List<ItemsRequest> = listOf()
    val mdoc: MDoc
    var bleManager: Transport? = null

    constructor(
        mdoc: MDoc,
        bluetoothManager: BluetoothManager,
        callback: BLESessionStateDelegate,
    ) {
        this.callback = callback
        this.uuid = UUID.randomUUID()
        this.mdoc = mdoc
        try {
            val sessionData = initialiseSession(mdoc.inner, uuid.toString())
            this.state = sessionData.state
            this.bleManager = Transport(bluetoothManager)
            this.bleManager!!
                .initialize(
                    "Holder",
                    this.uuid,
                    "BLE",
                    "Central",
                    sessionData.bleIdent.toByteArray(),
                    ::updateRequestData,
                    callback
                )
            this.callback.update(mapOf(Pair("engagingQRCode", sessionData.qrCodeUri)))
        } catch (e: Error) {
            Log.e("BleSessionManager.constructor", e.toString())
        }
    }

    fun cancel() {
        this.bleManager?.terminate()
    }

    fun submitNamespaces(items: Map<String, Map<String, List<String>>>) {
        val payload = submitResponse(
            this.sessionManager!!,
            items
        )

        val ks: KeyStore = KeyStore.getInstance(
            "AndroidKeyStore"
        )

        ks.load(
            null
        )

        val entry = ks.getEntry(this.mdoc.keyAlias, null)
        if (entry !is KeyStore.PrivateKeyEntry) {
            throw IllegalStateException("No such private key under the alias <${this.mdoc.keyAlias}>")
        }

        try {
            val signer = Signature.getInstance("SHA256withECDSA")
            signer.initSign(entry.privateKey)

            signer.update(payload)

            val signature = signer.sign()
            val response = submitSignature(this.sessionManager!!, signature)
            this.bleManager!!.send(response)
        } catch (e: Error) {
            Log.e("CredentialsViewModel.submitNamespaces", e.toString())
            this.callback.update(mapOf(Pair("error", e.toString())))
            throw e
        }
    }

    fun updateRequestData(data: ByteArray) {
        val requestData = handleRequest(this.state!!, data)
        this.sessionManager = requestData.sessionManager
        this.itemsRequests = requestData.itemsRequests
        this.callback.update(mapOf(Pair("selectNamespaces", requestData.itemsRequests)))
    }
}

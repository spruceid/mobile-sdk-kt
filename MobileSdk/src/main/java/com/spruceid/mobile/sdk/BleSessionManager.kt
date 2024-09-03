package com.spruceid.mobile.sdk

import android.bluetooth.BluetoothManager
import android.util.Log
import com.spruceid.mobile.sdk.rs.ItemsRequest
import com.spruceid.mobile.sdk.rs.Key
import com.spruceid.mobile.sdk.rs.KeyManagerInterface
import com.spruceid.mobile.sdk.rs.MdlPresentationSession
import com.spruceid.mobile.sdk.rs.StorageManagerInterface
import com.spruceid.mobile.sdk.rs.Wallet
import java.security.KeyStore
import java.security.Signature
import java.util.UUID

abstract class BLESessionStateDelegate {
    abstract fun update(state: Map<String, Any>)
}

public class BLESessionManager(val callback: BLESessionStateDelegate) {

    val uuid: UUID = UUID.randomUUID()
    private var itemsRequests: List<ItemsRequest> = listOf()
    private var bleManager: Transport? = null
    private var session: MdlPresentationSession? = null

    suspend fun initialize(
        bluetoothManager: BluetoothManager, wallet: Wallet,
        mdocId: String,
    ) {
        try {
            this.session = wallet.initializeMdlPresentation(mdocId, uuid.toString())
            this.bleManager = Transport(bluetoothManager)
            this.bleManager!!
                .initialize(
                    "Holder",
                    this.uuid,
                    "BLE",
                    "Central",
                    session!!.getBleIdent(),
                    ::updateRequestData,
                    callback
                )
            this.callback.update(mapOf(Pair("engagingQRCode", session!!.getQrCodeUri())))
        } catch (e: Error) {
            Log.e("BleSessionManager.constructor", e.toString())
        }
    }

    fun cancel() {
        this.bleManager?.terminate()
    }

    fun submitNamespaces(items: Map<String, Map<String, List<String>>>, keyid: Key) {
        try {
            val response = session!!.submitResponse(
                items,
                keyid
        )
            this.bleManager!!.send(response)
        } catch (e: Error) {
            Log.e("CredentialsViewModel.submitNamespaces", e.toString())
            this.callback.update(mapOf(Pair("error", e.toString())))
            throw e
        }
    }

    fun updateRequestData(data: ByteArray) {
        val itemsRequests = session!!.handleRequest(data)
        this.itemsRequests = itemsRequests
        this.callback.update(mapOf(Pair("selectNamespaces", itemsRequests)))
    }
}

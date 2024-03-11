package com.spruceid.wallet.sdk

import android.bluetooth.BluetoothManager
import android.util.Log
import androidx.lifecycle.ViewModel
import com.spruceid.wallet.sdk.rs.RequestData
import com.spruceid.wallet.sdk.rs.SessionData
import com.spruceid.wallet.sdk.rs.handleRequest
import com.spruceid.wallet.sdk.rs.initialiseSession
import com.spruceid.wallet.sdk.rs.submitResponse
import com.spruceid.wallet.sdk.rs.submitSignature
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.security.KeyStore
import java.security.Signature
import java.util.UUID

class CredentialsViewModel : ViewModel() {

    private val _credentials = MutableStateFlow<List<BaseCredential>>(listOf())
    val credentials = _credentials.asStateFlow()

    private val _currState = MutableStateFlow(PresentmentState.UNINITIALIZED)
    val currState = _currState.asStateFlow()

    private val _requestData = MutableStateFlow<RequestData?>(null)
    val requestData = _requestData.asStateFlow()

    private val _session = MutableStateFlow<SessionData?>(null)
    val session = _session.asStateFlow()

    private val _allowedNamespaces =
        MutableStateFlow<Map<String, Map<String, List<String>>>>(mapOf())
    val allowedNamespaces = _allowedNamespaces.asStateFlow()

    private val _uuid = MutableStateFlow<UUID>(UUID.randomUUID())

    private val _transport = MutableStateFlow<Transport?>(null)

    fun storeCredental(credential: BaseCredential) {
        _credentials.value = _credentials.value.plus(credential)
    }

    fun toggleAllowedNamespace(docType: String, specName: String, fieldName: String) {
        if (_allowedNamespaces.value.isEmpty()) {
            _allowedNamespaces.value = mapOf(Pair(docType, mapOf(Pair(specName, listOf()))))
        }
        val allowedForSpec = _allowedNamespaces.value[docType]!![specName]

        if (!allowedForSpec!!.contains(fieldName)) {
            _allowedNamespaces.value = mapOf(
                Pair(
                    docType,
                    mapOf(Pair(specName, allowedForSpec.plus(fieldName)))
                )
            )
        } else {
            _allowedNamespaces.value = mapOf(
                Pair(
                    docType,
                    mapOf(Pair(specName, allowedForSpec.minus(fieldName)))
                )
            )
        }
    }

    private fun updateRequestData(data: ByteArray) {
        _requestData.value = handleRequest(_session.value!!.state, data)
        val namespaces =
            requestData.value!!.itemsRequests.map { itemsRequest -> itemsRequest.namespaces }
        Log.d(
            "CredentialsViewModel.updateRequestData",
            "Updating requestData: \nitemRequests ${requestData.value!!.itemsRequests.map { itemsRequest -> itemsRequest.docType }} namespaces: $namespaces"
        )
        _currState.value = PresentmentState.SELECT_NAMESPACES
    }

    fun present(bluetoothManager: BluetoothManager) {
        Log.d("CredentialsViewModel.present", "Credentials: ${_credentials.value}")
        _uuid.value = UUID.randomUUID()
        val first: MDoc = _credentials.value.first() as MDoc
        _session.value = initialiseSession(first.inner, _uuid.value.toString())
        _currState.value = PresentmentState.ENGAGING_QR_CODE
        _transport.value = Transport(bluetoothManager)
        _transport.value!!
            .initialize(
                "Holder",
                _uuid.value,
                "BLE",
                "Central",
                _session.value!!.bleIdent.toByteArray(),
                ::updateRequestData,
                null
            )
    }

    fun submitNamespaces(allowedNamespaces: Map<String, Map<String, List<String>>>) {
        val firstMDoc: MDoc = _credentials.value.first() as MDoc
        val payload = submitResponse(
            _requestData.value!!.sessionManager,
            allowedNamespaces
        )


        val ks: KeyStore = KeyStore.getInstance(
            "AndroidKeyStore"
        )

        ks.load(
            null
        )

        val entry = ks.getEntry(firstMDoc.keyAlias, null)
        if (entry !is KeyStore.PrivateKeyEntry) {
            throw IllegalStateException("No such private key under the alias <${firstMDoc.keyAlias}>")
        }

        try {
            val signer = Signature.getInstance("SHA256withECDSA")
            signer.initSign(entry.privateKey)
            signer.update(payload)
            val signature = signer.sign()
            val response = submitSignature(_requestData.value!!.sessionManager, signature)
            _transport.value!!.send(response)
            _currState.value = PresentmentState.SUCCESS
        } catch (e: Error) {
            Log.e("CredentialsViewModel.submitNamespaces", e.toString())
            _currState.value = PresentmentState.ERROR
            throw e
        }
    }
}
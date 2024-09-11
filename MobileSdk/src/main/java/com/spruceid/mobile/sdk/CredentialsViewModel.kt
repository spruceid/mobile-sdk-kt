package com.spruceid.mobile.sdk

import android.bluetooth.BluetoothManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.spruceid.mobile.sdk.rs.ItemsRequest
import com.spruceid.mobile.sdk.rs.Key
import com.spruceid.mobile.sdk.rs.MdlPresentationSession
import com.spruceid.mobile.sdk.rs.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CredentialsViewModelFactory(private val wallet: Wallet) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(Wallet::class.java).newInstance(wallet)
    }
}

class CredentialsViewModel(private val wallet: Wallet) : ViewModel() {
    private val _currState = MutableStateFlow(PresentmentState.UNINITIALIZED)
    val currState = _currState.asStateFlow()

    private val _requestData = MutableStateFlow<List<ItemsRequest>?>(null)
    val requestData = _requestData.asStateFlow()

    private val _session = MutableStateFlow<MdlPresentationSession?>(null)
    val session = _session.asStateFlow()

    private val _allowedNamespaces =
        MutableStateFlow<Map<String, Map<String, List<String>>>>(mapOf())
    val allowedNamespaces = _allowedNamespaces.asStateFlow()

    private val _credentials = MutableStateFlow(listOf<String>())
    val credentials = _credentials.asStateFlow()

    private val _credentialId = MutableStateFlow<Key?>(null)
    val credentialId = _credentialId.asStateFlow()

    private val _uuid = MutableStateFlow<UUID>(UUID.randomUUID())

    private val _transport = MutableStateFlow<Transport?>(null)

    fun gatherCredentialList() {
        viewModelScope.launch(Dispatchers.IO) {
            _credentials.value = wallet.getCredentialList()
        }
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
        _requestData.value = _session.value!!.handleRequest(data)
        val namespaces =
            requestData.value!!.map { itemsRequest -> itemsRequest.namespaces }
        Log.d(
            "CredentialsViewModel.updateRequestData",
            "Updating requestData: \nitemRequests ${requestData.value!!.map { itemsRequest -> itemsRequest.docType }} namespaces: $namespaces"
        )
        _currState.value = PresentmentState.SELECT_NAMESPACES
    }

    fun present(bluetoothManager: BluetoothManager, credential: Key) {
        Log.d("CredentialsViewModel.present", "Credential: $credential")
        _uuid.value = UUID.randomUUID()
        _credentialId.value = credential
        viewModelScope.launch(Dispatchers.IO) {
            _session.value = wallet.initializeMdlPresentation(credential, _uuid.value.toString())

            _currState.value = PresentmentState.ENGAGING_QR_CODE
            _transport.value = Transport(bluetoothManager)
            _transport.value!!
                .initialize(
                    "Holder",
                    _uuid.value,
                    "BLE",
                    "Central",
                    _session.value!!.getBleIdent(),
                    ::updateRequestData,
                    null
                )
        }
    }

    fun submitNamespaces(allowedNamespaces: Map<String, Map<String, List<String>>>) {
        try {
            val response = _session.value!!.submitResponse(allowedNamespaces, _credentialId.value!!)
            _transport.value!!.send(response)
            _currState.value = PresentmentState.SUCCESS
        } catch (e: Error) {
            Log.e("CredentialsViewModel.submitNamespaces", e.toString())
            _currState.value = PresentmentState.ERROR
            throw e
        }
    }
}
package com.spruceid.mobile.sdk

import android.bluetooth.BluetoothManager
import android.util.Log
import java.util.*

/**
 * Selects the type of BLE transport option to use. 18013-5 section 8.3.3.1.1.
 */
class TransportBle(private var bluetoothManager: BluetoothManager) {

    private lateinit var transportBleCentralClientHolder: TransportBleCentralClientHolder
    private lateinit var transportBlePeripheralServerHolder: TransportBlePeripheralServerHolder

    /**
     * Reserved for later matching with available cbor options.
     */
    val DEVICE_RETRIEVAL_METHOD_TYPE = 2
    val DEVICE_RETRIEVAL_METHOD_VERSION = 1
    val RETRIEVAL_OPTION_KEY_SUPPORTS_PERIPHERAL_SERVER_MODE = 0
    val RETRIEVAL_OPTION_KEY_SUPPORTS_CENTRAL_CLIENT_MODE = 1
    val RETRIEVAL_OPTION_KEY_PERIPHERAL_SERVER_MODE_UUID = 10
    val RETRIEVAL_OPTION_KEY_CENTRAL_CLIENT_MODE_UUID = 11
    val RETRIEVAL_OPTION_KEY_PERIPHERAL_SERVER_MODE_BLE_DEVICE_ADDRESS = 20

    /**
     * Initializes one of the transport modes (Central Client/Peripheral Server).
     */
    fun initialize(
        application: String, serviceUUID: UUID,
        deviceRetrievalOption: String, ident: ByteArray,
        updateRequestData: (data: ByteArray) -> Unit,
        callback: BLESessionStateDelegate?
    ) {

        /**
         * Transport Central Client Holder
         */
        if (deviceRetrievalOption == "Central" && application == "Holder") {
            Log.d("TransportBle.initialize", "-- Selecting Transport Central Client Holder --")

            transportBleCentralClientHolder = TransportBleCentralClientHolder(
                application,
                bluetoothManager,
                serviceUUID,
                updateRequestData,
                callback,
            )
            transportBleCentralClientHolder.connect(ident)
        }

        /**
         * Transport Peripheral Server Holder
         */
        if (deviceRetrievalOption == "Peripheral" && application == "Holder") {
            Log.d("TransportBle.initialize", "-- Selecting Peripheral Server Holder --")

            transportBlePeripheralServerHolder =
                TransportBlePeripheralServerHolder(application, bluetoothManager, serviceUUID)
            transportBlePeripheralServerHolder.start()
        }
    }

    /**
     * For sending the mDL based on initialized transport option.
     */
    fun send(payload: ByteArray) {
        if (this::transportBleCentralClientHolder.isInitialized) {
            transportBleCentralClientHolder.send(payload)
        }

        if (this::transportBlePeripheralServerHolder.isInitialized) {
            transportBlePeripheralServerHolder.send(payload)
        }
    }

    /**
     * Terminates BLE transports based on what is initialized.
     */
    fun terminate() {
        if (this::transportBleCentralClientHolder.isInitialized) {
            transportBleCentralClientHolder.disconnect()
        }

        if (this::transportBlePeripheralServerHolder.isInitialized) {
            transportBlePeripheralServerHolder.stop()
        }
    }

    /**
     * Terminates and resets all connections to ensure a clean state.
     */
    fun hardReset() {
        if (this::transportBleCentralClientHolder.isInitialized) {
            transportBleCentralClientHolder.hardReset()
        }

        if (this::transportBlePeripheralServerHolder.isInitialized) {
            transportBlePeripheralServerHolder.hardReset()
        }
    }
}
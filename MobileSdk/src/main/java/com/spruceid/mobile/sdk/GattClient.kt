package com.spruceid.mobile.sdk

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothStatusCodes
import android.content.Context
import android.os.Build
import android.util.Log
import java.io.ByteArrayOutputStream
import java.lang.reflect.InvocationTargetException
import java.util.ArrayDeque
import java.util.Arrays
import java.util.Queue
import java.util.UUID
import kotlin.math.min


/**
 * GATT client responsible for consuming data sent from a GATT server.
 * 18013-5 section 8.3.3.1.1.4 Table 11.
 */
class GattClient(private var callback: GattClientCallback,
                 private var context: Context,
                 private var serviceUuid: UUID,
                 private var characteristicStateUuid: UUID,
                 private var characteristicClient2ServerUuid: UUID,
                 private var characteristicServer2ClientUuid: UUID,
                 private var characteristicIdentUuid: UUID?,
                 private var characteristicL2CAPUuid: UUID?) {

    private var clientCharacteristicConfigUuid =
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    var gattClient: BluetoothGatt? = null

    var characteristicState: BluetoothGattCharacteristic? = null
    var characteristicClient2Server: BluetoothGattCharacteristic? = null
    var characteristicServer2Client: BluetoothGattCharacteristic? = null
    var characteristicIdent: BluetoothGattCharacteristic? = null
    var characteristicL2CAP: BluetoothGattCharacteristic? = null

    private var mtu = 0
    private var identValue: ByteArray? = byteArrayOf()
    private var usingL2CAP = false
    private var writeIsOutstanding = false
    private var writingQueue: Queue<ByteArray> = ArrayDeque()
    private var writingQueueTotalChunks = 0
    private var incomingMessage: ByteArrayOutputStream = ByteArrayOutputStream()

    /**
     * Bluetooth GATT callback containing all of the events.
     */
    private val bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        /**
         * Discover services to connect to.
         */
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            callback.onLog("onConnectionStateChange")

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                clearCache()

                callback.onState(BleStates.GattClientConnected.string)
                callback.onLog("Gatt Client is connected.")

                try {
                    gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH)
                    gatt.discoverServices()
                } catch (error: SecurityException) {
                    callback.onError(error)
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                callback.onPeerDisconnected()
                callback.onLog("GATT Server disconnected.")
            }
        }

        /**
         * Validating services characteristics and setting MTU limit. Assuming maximum payload of 515
         * bytes, adjusting for the Bluetooth Core specification Part F section 3.2.9 and
         * 18013-5 section 8.3.3.1.1.6.
         */
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            callback.onLog("onServicesDiscovered")

            if (status == BluetoothGatt.GATT_SUCCESS) {
                try {
                    Log.d("GattClient.onServicesDiscovered", "uuid: $serviceUuid, gatt: $gatt")
                    val service: BluetoothGattService = gatt.getService(serviceUuid)

                    for (gattService in service.characteristics) {
                        Log.d("GattClient.onServicesDiscovered", "gattServiceUUID: ${gattService.uuid}")
                    }

                    if (characteristicL2CAPUuid != null) {
                        characteristicL2CAP = service.getCharacteristic(characteristicL2CAPUuid)
                        if (characteristicL2CAP != null) {
                            callback.onError(Error("L2CAP characteristic found $characteristicL2CAPUuid."))
                        }
                    }

                    characteristicState = service.getCharacteristic(characteristicStateUuid)
                    if (characteristicState == null) {
                        callback.onError(Error("State characteristic not found."))
                        return
                    }

                    characteristicClient2Server = service.getCharacteristic(characteristicClient2ServerUuid)
                    if (characteristicClient2Server == null) {
                        callback.onError(Error("Client2Server characteristic not found."))
                        return
                    }

                    characteristicServer2Client = service.getCharacteristic(characteristicServer2ClientUuid)
                    if (characteristicServer2Client == null) {
                        callback.onError(Error("Server2Client characteristic not found."))
                        return
                    }

                    if (characteristicIdentUuid != null) {
                        characteristicIdent = service.getCharacteristic(characteristicIdentUuid)
                        if (characteristicIdent == null) {
                            callback.onError(Error("Ident characteristic not found."))
                            return
                        }
                    }

                    callback.onState(BleStates.ServicesDiscovered.string)
                    callback.onLog("Discovered expected services")
                } catch (error: Exception) {
                    callback.onError(error)
                }

                try {
                    if (!gatt.requestMtu(515)) {
                        callback.onError(Error("Error requesting MTU."))
                        return
                    }
                } catch (error: SecurityException) {
                    callback.onError(error)
                    return
                }

                gattClient = gatt
            }
        }

        /**
         * Detecting the MTU limit adjustment.
         */
        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            callback.onLog("onMtuChanged")

            this@GattClient.set_mtu(mtu)

            if (status != BluetoothGatt.GATT_SUCCESS) {
                callback.onError(Error("Error changing MTU, status: $status."))
                return
            }

            callback.onLog("Negotiated MTU changed to $mtu.")

            /**
             * Optional ident characteristic is used for additional reader validation. 18013-5 section
             * 8.3.3.1.1.4.
             */
            if (characteristicIdent != null) {
                try {
                    if (!gatt.readCharacteristic(characteristicIdent)) {
                        callback.onLog("Warning: Reading from ident characteristic.")
                    }
                } catch (error: SecurityException) {
                    callback.onError(error)
                }
            } else {
                afterIdentObtained(gatt)
            }
        }

        /**
         * Detecting character read and validating the expected characteristic.
         */
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(gatt: BluetoothGatt,
                                          characteristic: BluetoothGattCharacteristic, status: Int) {

            callback.onLog("onCharacteristicRead, uuid=${characteristic.uuid} status=$status")
            @Suppress("deprecation")
            val value = characteristic.value
            /**
             * 18013-5 section 8.3.3.1.1.3.
             */
            if (characteristic.uuid.equals(characteristicIdentUuid)) {
                callback.onLog("Received identValue: ${byteArrayToHex(value)}.")

                if (!Arrays.equals(value, identValue)) {
                    callback.onLog("Warning: Received ident does not match expected ident.")
                }

                afterIdentObtained(gatt)
            } else if (characteristic.uuid.equals(characteristicL2CAPUuid)) {
                /**
                 * L2CAP placeholder.
                 */
            } else {
                callback.onError(Error("Unexpected onCharacteristicRead for characteristic " +
                        "${characteristic.uuid} expected $characteristicIdentUuid."))
            }
        }

        /**
         * Detecting descriptor write.
         */
        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor,
                                       status: Int) {

            callback.onLog("onDescriptorWrite, descriptor-uuid=${descriptor.uuid} " +
                    "characteristic-uuid=${descriptor.characteristic.uuid} status=$status.")

            try {
                val charUuid = descriptor.characteristic.uuid

                if (charUuid.equals(characteristicServer2ClientUuid)
                    && descriptor.uuid.equals(clientCharacteristicConfigUuid)
                ) {
                    if (!gatt.setCharacteristicNotification(characteristicState, true)) {
                        callback.onError(Error("Error setting notification on State."))
                        return
                    }

                    val stateDescriptor: BluetoothGattDescriptor =
                        characteristicState!!.getDescriptor(clientCharacteristicConfigUuid)

                    Log.d("GattClient.onDescriptorWrite","-- descriptor value --\n${(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)}")

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val res = gatt.writeDescriptor(stateDescriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                        if(res != BluetoothStatusCodes.SUCCESS) {
                            callback.onError(Error("Error writing to Server2Client. Code: $res"))
                            return
                        }
                    } else {
                        // Above code addresses the deprecation but requires API 33+
                        @Suppress("deprecation")
                        stateDescriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

                        @Suppress("deprecation")
                        if (!gatt.writeDescriptor(stateDescriptor)) {
                            callback.onError(Error("Error writing to Server2Client clientCharacteristicConfig: desc."))
                            return
                        }
                    }

                } else if (charUuid.equals(characteristicStateUuid)
                    && descriptor.uuid.equals(clientCharacteristicConfigUuid)
                ) {

                    // Finally we've set everything up, we can write 0x01 to state to signal
                    // to the other end (mDL reader) that it can start sending data to us..
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val res = gatt.writeCharacteristic(characteristicState!!, byteArrayOf(0x01.toByte()), BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                        if(res != BluetoothStatusCodes.SUCCESS) {
                            callback.onError(Error("Error writing to Server2Client. Code: $res"))
                            return
                        }
                    } else {
                        // Above code addresses the deprecation but requires API 33+
                        @Suppress("deprecation")
                        characteristicState!!.value = byteArrayOf(0x01.toByte())
                        @Suppress("deprecation")
                        if (!gatt.writeCharacteristic(characteristicState)) {
                            callback.onError(Error("Error writing to state characteristic."))
                        }
                    }
                } else {
                    callback.onError(Error("Unexpected onDescriptorWrite for characteristic UUID $charUuid " +
                            "and descriptor UUID ${descriptor.uuid}."))
                }
            } catch (error: SecurityException) {
                callback.onError(error)
            }

        }

        /**
         * Observe state fully connected or messages writing progress.
         */
        override fun onCharacteristicWrite(gatt: BluetoothGatt,
                                           characteristic: BluetoothGattCharacteristic, status: Int) {

            val charUuid = characteristic.uuid

            callback.onLog("onCharacteristicWrite, status=$status uuid=$charUuid")

            if (charUuid.equals(characteristicStateUuid)) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    callback.onError(Error("Unexpected status for writing to State, status=$status."))
                    return
                }

                callback.onPeerConnected()

            } else if (charUuid.equals(characteristicClient2ServerUuid)) {
                if (status != BluetoothGatt.GATT_SUCCESS) {
                    callback.onError(Error("Unexpected status for writing to Client2Server, status=$status."))
                    return
                }

                if (writingQueueTotalChunks > 0) {
                    if (writingQueue.size == 0) {
                        callback.onMessageSendProgress(writingQueueTotalChunks, writingQueueTotalChunks)
                        writingQueueTotalChunks = 0
                    } else {
                        callback.onMessageSendProgress(writingQueueTotalChunks - writingQueue.size,
                            writingQueueTotalChunks)
                    }
                }

                writeIsOutstanding = false
                drainWritingQueue()
            }
        }

        /**
         * Detect characteristic change and inspect incoming message.
         */
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt,
                                             characteristic: BluetoothGattCharacteristic) {

            callback.onLog("onCharacteristicChanged, uuid=${characteristic.uuid}")
            @Suppress("deprecation")
            val value = characteristic.value
            if (characteristic.uuid.equals(characteristicServer2ClientUuid)) {
                if (value.isEmpty()) {
                    callback.onError(Error("Invalid data length ${value.size} for Server2Client " +
                            "characteristic."))
                    return
                }

                Log.d("GattClient.onCharacteristicChanged", byteArrayToHex(value))

                incomingMessage.write(value, 1, value.size - 1)

                callback.onLog("Received chunk with ${value.size} bytes (last=${value[0].toInt() == 0x00}), " +
                        "incomingMessage.length=${incomingMessage.toByteArray().size}")

                if (value[0].toInt() == 0x00) {
                    /**
                     * Last message.
                     */
                    val entireMessage: ByteArray = incomingMessage.toByteArray()

                    incomingMessage.reset()
                    callback.onMessageReceived(entireMessage)
                } else if (value[0].toInt() == 0x01) {
                    // Message size is three less than MTU, as opcode and attribute handle take up 3 bytes.
                    if (value.size > mtu - 3) {
                        callback.onError(Error("Invalid size ${value.size} of data written Server2Client " +
                                "characteristic, expected maximum size ${mtu - 3}."))
                        return
                    }
                } else {
                    callback.onError(Error("Invalid first byte ${value[0]} in Server2Client data chunk, " +
                            "expected 0 or 1."))
                    return
                }
            } else if (characteristic.uuid.equals(characteristicStateUuid)) {
                if (value.size != 1) {
                    callback.onError(Error("Invalid data length ${value.size} for state characteristic."))
                    return
                }

                if (value[0].toInt() == 0x02) {
                    callback.onTransportSpecificSessionTermination()
                } else {
                    callback.onError(Error("Invalid byte ${value[0]} for state characteristic."))
                }
            }
        }
    }

    /**
     *
     */
    private fun afterIdentObtained(gatt: BluetoothGatt) {
        try {
            // Use L2CAP if supported by GattServer and by this OS version
            usingL2CAP = characteristicL2CAP != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

            if (usingL2CAP) {
                callback.onLog("Using L2CAP: $usingL2CAP")

                // value is returned async above in onCharacteristicRead()
                if (!gatt.readCharacteristic(characteristicL2CAP)) {
                    callback.onError(Error("Error reading L2CAP characteristic."))
                }
                return
            }

            if (!gatt.setCharacteristicNotification(characteristicServer2Client, true)) {
                callback.onError(Error("Error setting notification on Server2Client."))
                return
            }

            val descriptor: BluetoothGattDescriptor =
                characteristicServer2Client!!.getDescriptor(clientCharacteristicConfigUuid)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val res = gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                if(res != BluetoothStatusCodes.SUCCESS) {
                    callback.onError(Error("Error writing to Server2Client. Code: $res"))
                    return
                }
            } else {
                // Above code addresses the deprecation but requires API 33+
                @Suppress("deprecation")
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

                @Suppress("deprecation")
                if (!gatt.writeDescriptor(descriptor)) {
                    callback.onError(Error("Error writing to Server2Client clientCharacteristicConfig: desc."))
                    return
                }
            }

        } catch (error: SecurityException) {
            callback.onError(error)
        }
    }

    /**
     * Draining writing queue when the write is not outstanding.
     */
    private fun drainWritingQueue() {
        callback.onLog("drainWritingQueue: write is outstanding $writeIsOutstanding")

        if (writeIsOutstanding) {
            return
        }

        val chunk: ByteArray = writingQueue.poll() ?: return

        callback.onLog("Sending chunk with ${chunk.size} bytes (last=${chunk[0].toInt() == 0x00})")


        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val res = gattClient!!.writeCharacteristic(characteristicClient2Server!!, chunk, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                if(res != BluetoothStatusCodes.SUCCESS) {
                    callback.onError(Error("Error writing to Server2Client. Code: $res"))
                    return
                }
            } else {
                // Above code addresses the deprecation but requires API 33+
                @Suppress("deprecation")
                characteristicClient2Server!!.value = chunk
                @Suppress("deprecation")
                if (!gattClient!!.writeCharacteristic(characteristicClient2Server)) {
                    callback.onError(Error("Error writing to Client2Server characteristic"))
                    return
                }
            }
        } catch (error: SecurityException) {
            callback.onError(error)
            return
        }

        writeIsOutstanding = true
    }

    /**
     * Clears the GATT state. Needing to access a private function in GATT.
     */
    private fun clearCache() {
        try {
        } catch (error: NoSuchMethodException) {
            callback.onError(error)
        } catch (error: IllegalAccessException) {
            callback.onError(error)
        } catch (error: InvocationTargetException) {
            callback.onError(error)
        }
    }

    /**
     * Sends a message to the other device - can be any payload that is converted into
     * a byte array.
     */
    fun sendMessage(data: ByteArray) {
        callback.onLog("Sending message: $data")

        /**
         * L2CAP placeholder - when client is implemented send message via socket instead.
         */

        if (mtu == 0) {
            callback.onLog("MTU not negotiated, defaulting to 23. Performance will suffer.")
            mtu = 23
        }

        /**
         * Three less the MTU but we also need room for the leading 0x00 or 0x01.
         */
        val maxChunkSize: Int = mtu - 4
        var offset = 0

        do {
            val moreChunksComing = offset + maxChunkSize < data.size
            var size = data.size - offset

            if (size > maxChunkSize) {
                size = maxChunkSize
            }

            val chunk = ByteArray(size + 1)

            chunk[0] = if (moreChunksComing) 0x01.toByte() else 0x00.toByte()
            System.arraycopy(data, offset, chunk, 1, size)
            writingQueue.add(chunk)
            offset += size
        } while (offset < data.size)

        writingQueueTotalChunks = writingQueue.size
        drainWritingQueue()
    }

    /**
     * When using L2CAP it doesn't support characteristics notification.
     */
    fun supportsTransportSpecificTerminationMessage(): Boolean {
        return !usingL2CAP
    }

    /**
     * Sends termination message to the other device to terminate session
     * and disconnect.
     */
    fun sendTransportSpecificTermination() {
        val terminationCode = byteArrayOf(0x02.toByte())
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val res = gattClient!!.writeCharacteristic(characteristicState!!, terminationCode, BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT)
                if(res != BluetoothStatusCodes.SUCCESS) {
                    callback.onError(Error("Error writing to state characteristic. Code: $res"))
                    return
                }
            } else {
                // Above code addresses the deprecation but requires API 33+
                if (characteristicState != null) {
                    @Suppress("deprecation")
                    characteristicState!!.value = terminationCode
                }

                @Suppress("deprecation")
                if (gattClient != null && !gattClient!!.writeCharacteristic(characteristicState)) {
                    callback.onError(Error("Error writing to state characteristic."))
                }
            }
        } catch (error: SecurityException) {
            callback.onError(error)
        }
    }

    /**
     * Primary GATT connection setup.
     */
    fun connect(device: BluetoothDevice, ident: ByteArray?) {
        identValue = ident

        this.reset()

        try {
            gattClient = device.connectGatt(context, false, bluetoothGattCallback,
                BluetoothDevice.TRANSPORT_LE)

            callback.onState(BleStates.ConnectingGattClient.string)
            callback.onLog("Connecting to GATT server.")
        } catch (error: SecurityException) {
            callback.onError(error)
        }
    }

    /**
     * Primary GATT disconnect setup.
     */
    fun disconnect() {
        try {
            if (gattClient != null) {
                gattClient?.close()
                gattClient?.disconnect()
                gattClient = null

                callback.onState(BleStates.DisconnectGattClient.string)
                callback.onLog("Gatt Client disconnected.")
            }
        } catch (error: SecurityException) {
            callback.onError(error)
        }
    }

    fun reset() {
        mtu = 0
        writingQueueTotalChunks = 0
        writingQueue.clear()
        incomingMessage.reset()
    }

    fun set_mtu(mtu: Int) {
        this.mtu = min(515, mtu)
    }
}
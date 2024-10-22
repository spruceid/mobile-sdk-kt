import android.content.Context
import android.util.Base64
import com.spruceid.mobile.sdk.KeyManager
import com.spruceid.mobile.sdk.rs.StorageManagerInterface
import java.io.File

class StorageManager(val context: Context) : StorageManagerInterface {
    /// Function: add
    ///
    /// Adds a key-value pair to storage.  Should the key already exist, the value will be
    /// replaced.
    ///
    /// Arguments:
    /// key - The key to add
    /// value - The value to add under the key
    override fun add(key: String, value: ByteArray) =
        context.openFileOutput(filename(key), 0).use { it.write(encrypt(value)) }


    /// Function: get
    ///
    /// Retrieves the value from storage identified by key.
    ///
    /// Arguments:
    /// key - The key to retrieve
    override fun get(key: String): ByteArray {
        val bytes = ByteArray(0)
        context.openFileInput(filename(key)).use { it.read(bytes) }
        return decrypt(bytes)
    }

    /// Function: remove
    ///
    /// Removes a key-value pair from storage by key.
    ///
    /// Arguments:
    /// key - The key to remove
    override fun remove(key: String) {
        File(context.filesDir, filename(key)).delete()
    }


    /// Function: list
    ///
    /// Lists all key-value pair in storage
    override fun list(): List<String> {
        val list = context.filesDir.list() ?: throw Exception("cannot list stored objects")

        return list.mapNotNull {
            if (it.startsWith(FILENAME_PREFIX)) {
                it.substring(FILENAME_PREFIX.length + 1)
            } else {
                null
            }
        }
    }


    companion object {
        private const val B64_FLAGS = Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        private const val KEY_NAME = "sprucekit/datastore"

        /// Function: encrypt
        ///
        /// Encrypts the given string.
        ///
        /// Arguments:
        /// value - The string value to be encrypted
        private fun encrypt(value: ByteArray): ByteArray {
            val keyManager = KeyManager()
            if (!keyManager.keyExists(KEY_NAME)) {
                keyManager.generateEncryptionKey(KEY_NAME)
            }
            val encrypted = keyManager.encryptPayload(KEY_NAME, value)
            val iv = Base64.encodeToString(encrypted.first, B64_FLAGS)
            val bytes = Base64.encodeToString(encrypted.second, B64_FLAGS)
            val res = "$iv;$bytes".toByteArray()
            return res
        }

        /// Function: decrypt
        ///
        /// Decrypts the given byte array.
        ///
        /// Arguments:
        /// value - The byte array to be decrypted
        private fun decrypt(value: ByteArray): ByteArray {
            val keyManager = KeyManager()
            if (!keyManager.keyExists(KEY_NAME)) {
                throw Exception("Cannot retrieve values before creating encryption keys")
            }
            val decoded = value.decodeToString().split(";")
            assert(decoded.size == 2)
            val iv = Base64.decode(decoded.first(), B64_FLAGS)
            val encrypted = Base64.decode(decoded.last(), B64_FLAGS)
            return keyManager.decryptPayload(KEY_NAME, iv, encrypted)
        }

        private const val FILENAME_PREFIX = "sprucekit:datastore"

        private fun filename(filename: String) = "$FILENAME_PREFIX:$filename"
    }
}

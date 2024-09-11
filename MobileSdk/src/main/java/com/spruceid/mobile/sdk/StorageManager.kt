import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStoreFile
import com.spruceid.mobile.sdk.KeyManager
import com.spruceid.mobile.sdk.rs.EncryptedPayload
import com.spruceid.mobile.sdk.rs.Key
import com.spruceid.mobile.sdk.rs.StorageManagerException
import com.spruceid.mobile.sdk.rs.StorageManagerInterface
import com.spruceid.mobile.sdk.rs.Value
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private class DataStoreSingleton private constructor(context: Context) {
    val dataStore: DataStore<Preferences> = store(context, "default")

    companion object {
        private const val FILENAME_PREFIX = "sprucekit/datastore/"

        private fun location(context: Context, file: String) =
            context.preferencesDataStoreFile(FILENAME_PREFIX + file.lowercase())

        private fun store(context: Context, file: String): DataStore<Preferences> =
            PreferenceDataStoreFactory.create(produceFile = { location(context, file) })

        @Volatile
        private var instance: DataStoreSingleton? = null

        fun getInstance(context: Context) =
            instance
                ?: synchronized(this) {
                    instance ?: DataStoreSingleton(context).also {
                        instance = it
                    }
                }
    }
}

class StorageManager(val context: Context) : StorageManagerInterface {

    /// Function: encrypt
    ///
    /// Encrypts the given string.
    ///
    /// Arguments:
    /// value - The string value to be encrypted
    private fun encrypt(value: Value): Result<ByteArray> {
        val keyManager = KeyManager()
        try {
            if (!keyManager.keyExists(KEY_NAME)) {
                keyManager.generateEncryptionKey(KEY_NAME)
            }
            val encrypted = keyManager.encryptPayload(KEY_NAME, value)
            val iv = Base64.encodeToString(encrypted.iv(), B64_FLAGS)
            val bytes = Base64.encodeToString(encrypted.ciphertext(), B64_FLAGS)
            val res = "$iv;$bytes".toByteArray()
            return Result.success(res)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    /// Function: decrypt
    ///
    /// Decrypts the given byte array.
    ///
    /// Arguments:
    /// value - The byte array to be decrypted
    private fun decrypt(value: ByteArray): Result<String> {
        val keyManager = KeyManager()
        try {
            if (!keyManager.keyExists(KEY_NAME)) {
                return Result.failure(Exception("Cannot retrieve values before creating encryption keys"))
            }
            val decoded = value.decodeToString().split(";")
            assert(decoded.size == 2)
            val iv = Base64.decode(decoded.first(), B64_FLAGS)
            val encrypted = Base64.decode(decoded.last(), B64_FLAGS)
            val decrypted =
                keyManager.decryptPayload(KEY_NAME, EncryptedPayload(iv, encrypted))
                    ?: return Result.failure(Exception("Failed to decrypt value"))
            return Result.success(decrypted.decodeToString())
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    /// Function: add
    ///
    /// Adds a key-value pair to storage.  Should the key already exist, the value will be
    /// replaced.
    ///
    /// Arguments:
    /// context - The application context to be able to access the DataStore
    /// key - The key to add
    /// value - The value to add under the key
    override suspend fun add(key: Key, value: Value) {
        val storeKey = byteArrayPreferencesKey(key)
        val storeValue = encrypt(value)

        if (storeValue.isFailure) {
             throw StorageManagerException.InternalException()
        }

        DataStoreSingleton.getInstance(context).dataStore.edit { store ->
            store[storeKey] = storeValue.getOrThrow()
        }
    }

    override suspend fun list(): List<Key> {
        val keys = DataStoreSingleton.getInstance(context).dataStore.data.map { store -> store.asMap().keys.toList() }.first()
        return keys.map { key -> key.name }
    }

    /// Function: get
    ///
    /// Retrieves the value from storage identified by key.
    ///
    /// Arguments:
    /// context - The application context to be able to access the DataStore
    /// key - The key to retrieve
    override suspend fun get(key: Key): Value? {
        val storeKey = byteArrayPreferencesKey(key)
        return DataStoreSingleton.getInstance(context)
            .dataStore
            .data
            .map { store ->
                try {
                    store[storeKey]?.let { v ->
                        val storeValue = decrypt(v)
                        when {
                            storeValue.isSuccess -> storeValue.getOrThrow()
                            storeValue.isFailure -> throw StorageManagerException.CouldNotDecryptValue()
                            else -> throw StorageManagerException.CouldNotDecryptValue()
                        }
                    }
                } catch (e: Exception) {
                    throw StorageManagerException.InvalidLookupKey()
                }
            }
            .catch { exception -> throw exception }
            .first()
            ?.toByteArray()
    }

    /// Function: remove
    ///
    /// Removes a key-value pair from storage by key.
    ///
    /// Arguments:
    /// context - The application context to be able to access the DataStore
    /// key - The key to remove
    override suspend fun remove(key: Key) {
        val storeKey = stringPreferencesKey(key)
        DataStoreSingleton.getInstance(context).dataStore.edit { store ->
            if (store.contains(storeKey)) {
                store.remove(storeKey)
            }
        }
    }

    companion object {
        private const val B64_FLAGS = Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        private const val KEY_NAME = "sprucekit/datastore"
    }
}

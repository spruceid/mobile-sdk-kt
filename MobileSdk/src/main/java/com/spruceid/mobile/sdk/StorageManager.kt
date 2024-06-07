import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStoreFile
import com.spruceid.wallet.sdk.KeyManager
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private class DataStoreSingleton private constructor(context: Context) {
    val dataStore: DataStore<Preferences> = store(context, "default")

    companion object {
        private const val FILENAME_PREFIX = "datastore_"

        private fun location(context: Context, file: String) =
            context.preferencesDataStoreFile(FILENAME_PREFIX + file.lowercase())

        private fun store(context: Context, file: String): DataStore<Preferences> =
            PreferenceDataStoreFactory.create(produceFile = { location(context, file) })

        @Volatile
        private var instance: DataStoreSingleton? = null

        fun getInstance(context: Context) =
            instance ?: synchronized(this) {
                instance ?: DataStoreSingleton(context).also { instance = it }
            }
    }
}

object StorageManager {
    private val flags = Base64.URL_SAFE xor Base64.NO_PADDING xor Base64.NO_WRAP

    /// Function: encrypt
    ///
    /// Encrypts the given string.
    ///
    /// Arguments:
    /// value - The string value to be encrypted
    private fun encrypt(value: String): Result<ByteArray> {
        val keyManager = KeyManager()
        try {
            if (!keyManager.keyExists("datastore")) {
                keyManager.generateEncryptionKey("datastore")
            }
            val encrypted = keyManager.encryptPayload("datastore", value.toByteArray())
            val iv = Base64.encodeToString(encrypted.first, flags)
            val bytes = Base64.encodeToString(encrypted.second, flags)
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
            if (!keyManager.keyExists("datastore")) {
                return Result.failure(Exception("Cannot retrieve values before creating encryption keys"))
            }
            val decoded = value.decodeToString().split(";")
            assert(decoded.size == 2)
            val iv = Base64.decode(decoded.first(), flags)
            val encrypted = Base64.decode(decoded.last(), flags)
            val decrypted = keyManager.decryptPayload("datastore", iv, encrypted)
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
    suspend fun add(context: Context, key: String, value: String): Result<Unit> {
        val storeKey = byteArrayPreferencesKey(key)
        val storeValue = encrypt(value)

        if (storeValue.isFailure) {
            return Result.failure(Exception("Failed to encrypt value for storage"))
        }

        DataStoreSingleton.getInstance(context).dataStore.edit { store ->
            store[storeKey] = storeValue.getOrThrow()
        }

        return Result.success(Unit)
    }

    /// Function: get
    ///
    /// Retrieves the value from storage identified by key.
    ///
    /// Arguments:
    /// context - The application context to be able to access the DataStore
    /// key - The key to retrieve
    suspend fun get(context: Context, key: String): Result<String?> {
        val storeKey = byteArrayPreferencesKey(key)
        return DataStoreSingleton.getInstance(context).dataStore.data.map { store ->
            try {
                store[storeKey]?.let { v ->
                    val storeValue = decrypt(v)
                    when {
                        storeValue.isSuccess -> Result.success(storeValue.getOrThrow())
                        storeValue.isFailure -> Result.failure(storeValue.exceptionOrNull()!!)
                        else -> Result.failure(Exception("Failed to decrypt value for storage"))
                    }
                } ?: Result.success(null)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }.catch { exception ->
            emit(Result.failure(exception))
        }.first()
    }

    /// Function: remove
    ///
    /// Removes a key-value pair from storage by key.
    ///
    /// Arguments:
    /// context - The application context to be able to access the DataStore
    /// key - The key to remove
    suspend fun remove(context: Context, key: String): Result<Unit> {
        val storeKey = stringPreferencesKey(key)
        DataStoreSingleton.getInstance(context).dataStore.edit { store ->
            if (store.contains(storeKey)) {
                store.remove(storeKey)
            }
        }
        return Result.success(Unit)
    }
}

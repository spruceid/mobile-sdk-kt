package com.spruceid.wallet.sdk

import org.junit.Assert
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith

import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
//@RunWith(RobolectricTestRunner::class)
//@Config(sdk = [30])
class KeyManagerTest {

    @Test
    fun getKeyStore() {
        val keyManager = KeyManager()
        val keyStore = keyManager.getKeyStore()

        assertNotNull(keyStore)
    }

    @Test
    fun reset() {
        Assert.assertEquals(4, 2 + 2)
    }

    @Test
    fun generateSigningKey() {
    }

    @Test
    fun getJwk() {
    }

    @Test
    fun keyExists() {
    }

    @Test
    fun signPayload() {
    }

    @Test
    fun generateEncryptionKey() {
    }

    @Test
    fun encryptPayload() {
    }

    @Test
    fun decryptPayload() {
    }
}

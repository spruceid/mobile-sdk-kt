package com.spruceid.mobile.sdk

import java.security.KeyFactory
import java.security.KeyStore
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Base64

/**
 * Collection of BaseCredentials with methods to interact with all instances
 */
class CredentialPack {
    private val credentials: MutableList<BaseCredential>

    constructor() {
        credentials = mutableListOf()
    }

    constructor(credentialsArray: MutableList<BaseCredential>) {
        this.credentials = credentialsArray
    }

    fun addW3CVC(credentialString: String): List<BaseCredential> {
        val vc = W3CVC(credentialString = credentialString)
        credentials.add(vc)
        return credentials
    }

    fun addMDoc(
        id: String,
        mdocBase64: String,
        keyPEM: String,
        keyBase64: String
    ): List<BaseCredential> {
        try {
            val decodedKey = Base64.getDecoder().decode(
                keyBase64
            )

            val privateKey = KeyFactory.getInstance(
                "EC"
            ).generatePrivate(
                PKCS8EncodedKeySpec(
                    decodedKey
                )
            )

            val cert: Array<Certificate> = arrayOf(
                CertificateFactory.getInstance(
                    "X.509"
                ).generateCertificate(
                    keyPEM.byteInputStream()
                )
            )

            val ks: KeyStore = KeyStore.getInstance(
                "AndroidKeyStore"
            )

            ks.load(
                null
            )

            ks.setKeyEntry(
                "someAlias",
                privateKey,
                null,
                cert
            )

            credentials.add(
                MDoc(
                    id,
                    Base64.getDecoder().decode(mdocBase64),
                    "someAlias"
                )
            )
        } catch (e: Throwable) {
            print(
                e
            )
            throw e
        }
        return credentials
    }

    fun get(keys: List<String>): Map<String, Map<String, Any>> {
        val values = emptyMap<String, Map<String, Any>>().toMutableMap()

        for (credential in credentials) {
            values[credential.getId()!!] = credential.get(keys)
        }
        return values
    }

    fun getCredentialsByIds(credentialsIds: List<String>): List<BaseCredential> {
        return credentials.filter { credential -> credentialsIds.contains(credential.getId()) }
    }

    fun getCredentials(): List<BaseCredential> {
        return credentials
    }

    fun getCredentialById(credentialId: String): BaseCredential? {
        return credentials.find { credential -> credential.getId().equals(credentialId) }
    }
}
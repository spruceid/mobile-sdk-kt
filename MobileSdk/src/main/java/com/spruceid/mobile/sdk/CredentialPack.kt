package com.spruceid.mobile.sdk

import com.spruceid.mobile.sdk.rs.JsonVc
import com.spruceid.mobile.sdk.rs.JwtVc
import com.spruceid.mobile.sdk.rs.Mdoc
import com.spruceid.mobile.sdk.rs.ParsedCredential
import org.json.JSONObject

/**
 * Collection of BaseCredentials with methods to interact with all instances
 */
class CredentialPack {
    private val credentials: MutableList<ParsedCredential>

    constructor() {
        credentials = mutableListOf()
    }

    constructor(credentialsArray: MutableList<ParsedCredential>) {
        this.credentials = credentialsArray
    }

    /**
     * Add a JwtVc to the CredentialPack.
     */
    fun addJwtVc(jwtVc: JwtVc): List<ParsedCredential> {
        credentials.add(ParsedCredential.newJwtVcJson(jwtVc))
        return credentials
    }

    /**
     * Add a JsonVc to the CredentialPack.
     */
    fun addJsonVc(jsonVc: JsonVc): List<ParsedCredential> {
        credentials.add(ParsedCredential.newLdpVc(jsonVc))
        return credentials
    }

    /**
     * Add an Mdoc to the CredentialPack.
     */
    fun addMdoc(mdoc: Mdoc): List<ParsedCredential> {
        credentials.add(ParsedCredential.newMsoMdoc(mdoc))
        return credentials
    }

    /**
     *  Find claims from all credentials in this CredentialPack.
     */
    fun findCredentialClaims(claimNames: List<String>): Map<String, JSONObject> =
        this.list()
            .map { credential ->
                var claims: JSONObject
                val mdoc = credential.asMsoMdoc()
                val jwtVc = credential.asJwtVc()
                val jsonVc = credential.asJsonVc()

                if (mdoc != null) {
                    claims = mdoc.jsonEncodedDetailsFiltered(claimNames)
                } else if (jwtVc != null) {
                    claims = jwtVc.credentialClaimsFiltered(claimNames)
                } else if (jsonVc != null) {
                    claims = jsonVc.credentialClaimsFiltered(claimNames)
                } else {
                    var type: String
                    try {
                        type = credential.intoGenericForm().type
                    } catch (e: Error) {
                        type = "unknown"
                    }
                    print("unsupported credential type: $type")
                    claims = JSONObject()
                }

                return@map Pair(credential.id(), claims)
            }
            .toMap()


    /**
     * Get credentials by id.
     */
    fun getCredentialsByIds(credentialsIds: List<String>): List<ParsedCredential> =
        this.list().filter { credential -> credentialsIds.contains(credential.id()) }


    /**
     * Get a credential by id.
     */
    fun getCredentialById(credentialId: String): ParsedCredential? =
        this.list().find { credential -> credential.id() == credentialId }


    /**
     * List all of the credentials in the CredentialPack.
     */
    fun list(): List<ParsedCredential> = this.credentials
}
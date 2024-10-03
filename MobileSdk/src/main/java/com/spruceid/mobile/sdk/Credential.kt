package com.spruceid.mobile.sdk

import com.spruceid.mobile.sdk.rs.JsonVc
import com.spruceid.mobile.sdk.rs.JwtVc
import com.spruceid.mobile.sdk.rs.Mdoc
import org.json.JSONObject

/**
 * Access all of the elements in the mdoc, ignoring namespaces and missing elements that cannot be encoded as JSON.
 */
fun Mdoc.jsonEncodedDetailsAll(): JSONObject = this.jsonEncodedDetailsInternal(null)

/**
 * Access the specified elements in the mdoc, ignoring namespaces and missing elements that cannot be encoded as JSON.
 */
fun Mdoc.jsonEncodedDetailsFiltered(elementIdentifiers: List<String>): JSONObject = this.jsonEncodedDetailsInternal(elementIdentifiers)


private fun Mdoc.jsonEncodedDetailsInternal(elementIdentifiers: List<String>?): JSONObject =
    JSONObject(
        // Ignore the namespaces.
        this.details().values.flatMap { elements ->
            elements.map { element ->
                val id = element.identifier
                val jsonString = element.value

                // If a filter is provided, filter out non-specified ids.
                if (elementIdentifiers != null) {
                    if (!elementIdentifiers.contains(id)) {
                        return@map null
                    }
                }

                if (jsonString != null) {
                    val json: JSONObject
                    try {
                        json = JSONObject(jsonString)
                    } catch (e: Error) {
                        print("failed to decode '$id' as JSON: $e")
                        return@map null
                    }
                    return@map Pair(id, json)
                }

                return@map null
            }
        }.filterNotNull().toMap()
    )

/**
 * Access the W3C VCDM credential (not including the JWT envelope).
 */
fun JwtVc.credentialClaims(): JSONObject {
    try {
        return JSONObject(this.credentialAsJsonEncodedUtf8String())
    } catch (e: Error) {
        print("failed to decode VCDM data from UTF-8-encoded JSON")
        return JSONObject()
    }
}

/**
 * Access the specified claims from the W3C VCDM credential (not including the JWT envelope).
 */
fun JwtVc.credentialClaimsFiltered(claimNames: List<String>): JSONObject {
    val old = this.credentialClaims()
    val new = JSONObject()
    for (name in claimNames) {
        if (old.has(name)) {
            new.put(name, old.get(name))
        }
    }
    return new
}

/**
 * Access the W3C VCDM credential.
 */
fun JsonVc.credentialClaims(): JSONObject {
    try {
        return JSONObject(this.credentialAsJsonEncodedUtf8String())
    } catch (e: Error) {
        print("failed to decode VCDM data from UTF-8-encoded JSON")
        return JSONObject()
    }
}

/**
 * Access the specified claims from the W3C VCDM credential.
 */
fun JsonVc.credentialClaimsFiltered(claimNames: List<String>): JSONObject {
    val old = this.credentialClaims()
    val new = JSONObject()
    for (name in claimNames) {
        if (old.has(name)) {
            new.put(name, old.get(name))
        }
    }
    return new
}
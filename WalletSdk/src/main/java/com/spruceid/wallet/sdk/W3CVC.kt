package com.spruceid.wallet.sdk

import org.json.JSONObject

class W3CVC(credentialString: String): BaseCredential() {
    private var credential: JSONObject = JSONObject(credentialString)

    init {
        super.setId(credential.getString("id"))
    }

    override fun get(keys: List<String>): Map<String, Any> {
        val res = mutableMapOf<String,Any>()

        for (key in keys) {
            res[key] = keyPathFinder(credential, key.split(".").toMutableList())
        }
        return res
    }

    private fun keyPathFinder(json: Any, path: MutableList<String>): Any {
        try {
            val firstKey = path.first()
            val element = (json as JSONObject)[firstKey]
            path.removeAt(0)
            if (path.isNotEmpty()) {
                return keyPathFinder(element, path)
            }
            return element
        } catch (e: Exception) {
            return ""
        }
    }
}

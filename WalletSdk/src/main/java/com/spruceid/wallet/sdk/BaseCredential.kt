package com.spruceid.wallet.sdk

open class BaseCredential {
    private var id: String?

    constructor() {
        this.id = null
    }

    constructor(id: String) {
        this.id = id
    }

    fun getId(): String? {
        return this.id
    }

    fun setId(id: String) {
        this.id = id
    }

    override fun toString(): String {
        return "Credential($id)"
    }

    open fun get(keys: List<String>): Map<String, Any> {
        return if (keys.contains("id")) {
            mapOf("id" to  this.id!!)
        } else {
            emptyMap()
        }
    }
}
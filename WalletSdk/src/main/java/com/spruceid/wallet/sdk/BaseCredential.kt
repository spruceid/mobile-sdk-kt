package com.spruceid.wallet.sdk

open class BaseCredential constructor(private val id: String?) {

    fun getId(): String? {
        return this.id
    }

    override fun toString(): String {
        return "Credential($id)"
    }
}
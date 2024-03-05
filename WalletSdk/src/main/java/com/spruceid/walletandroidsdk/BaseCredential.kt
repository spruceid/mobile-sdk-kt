package com.spruceid.walletandroidsdk
open class BaseCredential constructor(private val id: String?) {

    fun getId(): String {
        return id ?: "";
    }

    override fun toString(): String {
        return "Credential($id)"
    }
}
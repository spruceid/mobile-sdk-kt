package com.spruceid.wallet.sdk

open class BaseCredential constructor(private val id: String?) {

    override fun toString(): String {
        return "Credential($id)"
    }
}
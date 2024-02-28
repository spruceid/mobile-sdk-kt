package com.spruceid.walletandroidsdk

import com.spruceid.wallet.sdk.rs.helloFfi

fun helloRust(): String {
    return helloFfi()
}
open class BaseCredential constructor(private val id: String?) {

    fun getId(): String {
        return id ?: "";
    }

    override fun toString(): String {
        return "Credential($id)"
    }
}
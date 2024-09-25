package com.spruceid.mobile.sdk

import android.util.Log
import com.spruceid.mobile.sdk.rs.Mdoc as InnerMDoc

class MDoc(id: String, issuerAuth: ByteArray, val keyAlias: String) : BaseCredential(id) {
     val inner: InnerMDoc

    init {
        try {
            inner = InnerMDoc.fromCborEncodedDocument(issuerAuth, keyAlias)
        } catch (e: Throwable) {
            Log.e("MDoc.init", e.toString())
            throw e
        }
    }
}
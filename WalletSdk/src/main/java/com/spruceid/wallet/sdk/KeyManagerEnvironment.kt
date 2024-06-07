package com.spruceid.wallet.sdk

/**
 * The Keystore environment used for the key generation.
 */
enum class KeyManagerEnvironment(val string: String) {
    TEE("tee"),
    Strongbox("strongbox"),
}
package com.spruceid.mobile.sdk

import com.spruceid.mobile.sdk.rs.MDocItem
import org.json.JSONArray
import org.json.JSONObject


fun hexToByteArray(value: String): ByteArray {
    val stripped = value.substring(2)

    return stripped.chunked(2).map { it.toInt(16).toByte() }
        .toByteArray()
}

fun byteArrayToHex(bytes: ByteArray): String {
    return "0x${bytes.joinToString("") { "%02x".format(it) }}"
}

enum class PresentmentState {
    /// Presentment has yet to start
    UNINITIALIZED,

    /// App should display the error message
    ERROR,

    /// App should display the QR code
    ENGAGING_QR_CODE,

    /// App should display an interactive page for the user to chose which values to reveal
    SELECT_NAMESPACES,

    /// App should display a success message and offer to close the page
    SUCCESS,
}

// Recursive function to convert MDocItem to JSONObject
fun mDocItemToJson(item: MDocItem): Any {
    return when (item) {
        is MDocItem.Text -> item.v1
        is MDocItem.Bool -> item.v1
        is MDocItem.Integer -> item.v1
        is MDocItem.ItemMap -> mapToJson(item.v1)
        is MDocItem.Array -> JSONArray(item.v1.map { mDocItemToJson(it) })
    }
}

// Convert Map<String, MDocItem> to JSONObject
fun mapToJson(map: Map<String, MDocItem>): JSONObject {
    val jsonObject = JSONObject()
    for ((key, value) in map) {
        jsonObject.put(key, mDocItemToJson(value))
    }
    return jsonObject
}

// Convert Map<String, Map<String, MDocItem>> to JSONObject
fun convertToJson(map: Map<String, Map<String, MDocItem>>): JSONObject {
    val jsonObject = JSONObject()
    for ((key, value) in map) {
        jsonObject.put(key, mapToJson(value))
    }
    return jsonObject
}

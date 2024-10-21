package com.spruceid.mobilesdkexample.wallet

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import org.json.JSONObject

@Composable
fun BitmapImage(
    byteArray: ByteArray,
    contentDescription: String,
    modifier: Modifier,
) {
    fun convertImageByteArrayToBitmap(imageData: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(imageData, 0, imageData.size)
    }

    val bitmap = convertImageByteArrayToBitmap(byteArray)

    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = contentDescription,
        modifier = modifier,
    )
}

fun checkAndRequestBluetoothPermissions(
    context: Context,
    permissions: Array<String>,
    launcher: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
) {
    if (
        permissions.all {
            ContextCompat.checkSelfPermission(
                context,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    ) {
        // Use bluetooth because permissions are already granted
    } else {
        // Request permissions
        launcher.launch(permissions)
    }
}

fun keyPathFinder(json: Any, path: MutableList<String>): Any {
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

fun String.splitCamelCase() = replace(String.format(
        "%s|%s|%s",
        "(?<=[A-Z])(?=[A-Z][a-z])",
        "(?<=[^A-Z])(?=[A-Z])",
        "(?<=[A-Za-z])(?=[^A-Za-z])"
    ).toRegex(), " ")
    .replaceFirstChar(Char::titlecase)

fun String.removeUnderscores() = replace("_", "")

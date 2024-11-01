package com.spruceid.mobilesdkexample.verifier

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.spruceid.mobile.sdk.rs.verifyJwtVp
import com.spruceid.mobilesdkexample.ScanningComponent
import com.spruceid.mobilesdkexample.ScanningType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun VerifyVCView(
    navController: NavController
) {
    var success by remember {
        mutableStateOf<Boolean?>(null)
    }

    fun onRead(content: String) {
        GlobalScope.launch {
            try {
                verifyJwtVp(jwtVp = content)
                success = true
            } catch (e: Exception) {
                success = false
                e.printStackTrace()
            }
        }
    }

    if (success == null) {
        ScanningComponent(
            navController = navController,
            scanningType = ScanningType.QRCODE,
            onRead = ::onRead
        )
    } else {
        VerifierBinarySuccessView(
            navController = navController,
            success = success!!,
            description = if (success!!) "Valid Verifiable Credential" else "Invalid Verifiable Credential"
        )
    }
}
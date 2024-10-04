package com.spruceid.mobilesdkexample.wallet

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.spruceid.mobilesdkexample.ScanningComponent
import com.spruceid.mobilesdkexample.ScanningType
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.spruceid.mobile.sdk.rs.verifyJwtVp
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun DispatchQRView(
    navController: NavController
) {
    var success by remember {
        mutableStateOf<Boolean?>(null)
    }

    fun onRead(url: String) {
        GlobalScope.launch {
            // TODO: Add other checks as necessary for validating OID4VP url
            // and handle OID4VP flow
            
            // dispatchQRcode(url)
        }
    }
    
    ScanningComponent(
        navController = navController,
        scanningType = ScanningType.QRCODE,
        onRead = ::onRead
    )
}
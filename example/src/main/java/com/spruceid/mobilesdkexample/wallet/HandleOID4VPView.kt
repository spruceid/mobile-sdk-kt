package com.spruceid.mobilesdkexample.wallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.spruceid.mobile.sdk.rs.Holder
import com.spruceid.mobile.sdk.rs.ParsedCredential
import com.spruceid.mobile.sdk.rs.PermissionRequest
import com.spruceid.mobile.sdk.rs.SdJwt
import com.spruceid.mobilesdkexample.ui.theme.Inter
import com.spruceid.mobilesdkexample.ui.theme.MobileSdkTheme
import com.spruceid.mobilesdkexample.viewmodels.IRawCredentialsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button


@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HandleOID4VPView(
    navController: NavController,
    rawCredentialsViewModel: IRawCredentialsViewModel,
    url: String
) {
    val scope = rememberCoroutineScope()

    val rawCredentials by rawCredentialsViewModel.rawCredentials.collectAsState()

    var holder by remember { mutableStateOf<Holder?>(null) }
    var permissionRequest by remember { mutableStateOf<PermissionRequest?>(null) }

    LaunchedEffect(Unit) {
        println("URL: $url")

        val credentials = rawCredentials.map { rawCredential ->
            ParsedCredential
                // TODO: Update to use VDC collection in the future
                // to detect the type of credential.
                .newSdJwt(SdJwt.newFromCompactSdJwt(rawCredential.rawCredential))
                .intoGenericForm()
        }

        withContext(Dispatchers.IO) {

            // Retrieve the permission request from the OID4VP URL.
            val trustedDids = MutableList(1, { list ->
                "did:web:192.168.68.72%3A3000:oid4vp:client"
            })

            holder = Holder.newWithCredentials(credentials, trustedDids);
            permissionRequest = holder!!.authorizationRequest(url)
        }
    }

    if (permissionRequest == null) {
        // Show a loading screen
        MobileSdkTheme {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Loading... $url",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                )
            }
        }
    } else {
        // Load the Credential View
        SelectCredentialsView(permissionRequest!!.credentials(), onSelectedCredential = { selectedCredential ->
            scope.launch {
                val permissionResponse = permissionRequest!!.createPermissionResponse(selectedCredential)
                var response = holder!!.submitPermissionResponse(permissionResponse)
            }
        })
    }

}


@Composable
fun SelectCredentialsView(
    credentials: List<ParsedCredential>,
    onSelectedCredential: (ParsedCredential) -> Unit
) {
    return LazyColumn {
        items(credentials.size) { credential ->
            val cred = credentials[credential]

            CredentialItem(cred, onClick = { onSelectedCredential(cred) })
        }
    }
}

@Composable
fun CredentialItem(credential: ParsedCredential, onClick: () -> Unit) {
    Button(onClick = onClick) { Text(text = credential.format().toString()) }
}
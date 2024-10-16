package com.spruceid.mobilesdkexample.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.material3.*
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
import com.spruceid.mobilesdkexample.navigation.Screen
import com.spruceid.mobilesdkexample.utils.trustedDids


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

        try {
            val credentials = rawCredentials.map { rawCredential ->
                ParsedCredential
                    // TODO: Update to use VDC collection in the future
                    // to detect the type of credential.
                    .newSdJwt(SdJwt.newFromCompactSdJwt(rawCredential.rawCredential))
                    .intoGenericForm()
            }

            withContext(Dispatchers.IO) {
                holder = Holder.newWithCredentials(credentials, trustedDids);
                permissionRequest = holder!!.authorizationRequest(url)
            }
        } catch (e: Exception) {
            println("Error: $e")
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
        CredentialSelector(navController, permissionRequest!!.credentials(), onSelectedCredential = { selectedCredentials ->
            scope.launch {
                try {
                    val selectedCredential = selectedCredentials.first()
                    val permissionResponse = permissionRequest!!.createPermissionResponse(selectedCredential)

                    println("Submitting permission response")

                    holder!!.submitPermissionResponse(permissionResponse)
                } catch (e: Exception) {
                    println("Error: $e")
                }
            }
        })
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialSelector(
    navController: NavController,
    credentials: List<ParsedCredential>,
    onSelectedCredential: (List<ParsedCredential>) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCredentials = remember { mutableStateListOf<ParsedCredential>() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp)
    ) {
//        Text(
//            text = "SpruceiD Demo Wallet",
//            style = MaterialTheme.typography.headlineSmall
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Select the credential(s) to share",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        ElevatedButton(
            onClick = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = if (expanded) "Select All" else "Select Credentials")
        }

        if (expanded) {
            credentials.forEach { credential ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = credential in selectedCredentials,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                selectedCredentials.add(credential)
                            } else {
                                selectedCredentials.remove(credential)
                            }
                        }
                    )
                    Text(
                        text = credential.format().toString(),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(onClick = {
                // navigate back to home screen
                navController.navigate(Screen.HomeScreen.route)
            }) {
                Text("Cancel")
            }
            Button(onClick = {
                onSelectedCredential(selectedCredentials)

                // Navigate
                navController.navigate(Screen.HomeScreen.route)
            }) {
                Text("Continue")
            }
        }
    }
}
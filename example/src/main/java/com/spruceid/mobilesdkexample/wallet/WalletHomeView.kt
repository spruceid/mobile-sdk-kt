package com.spruceid.mobilesdkexample.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spruceid.mobilesdkexample.R
import com.spruceid.mobilesdkexample.navigation.Screen
import com.spruceid.mobilesdkexample.ui.theme.CTAButtonBlue
import com.spruceid.mobilesdkexample.ui.theme.Inter
import com.spruceid.mobilesdkexample.ui.theme.TextHeader
import com.spruceid.mobilesdkexample.ui.theme.Primary
import com.spruceid.mobilesdkexample.viewmodels.IRawCredentialsViewModel
import kotlinx.coroutines.launch

@Composable
fun WalletHomeView(
    navController: NavController,
    rawCredentialsViewModel: IRawCredentialsViewModel
) {
    Column(
        Modifier
            .padding(all = 20.dp)
            .padding(top = 20.dp)
    ) {
        WalletHomeHeader(navController = navController)
        WalletHomeBody(
            rawCredentialsViewModel = rawCredentialsViewModel,
            navController = navController
        )
    }
}

@Composable
fun WalletHomeHeader(navController: NavController) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Spruce Wallet",
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            color = TextHeader
        )
        Spacer(Modifier.weight(1f))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(36.dp)
                .height(36.dp)
                .padding(start = 4.dp)
                .clip(shape = RoundedCornerShape(8.dp))
                .background(Primary)
                .clickable {
                    navController.navigate(Screen.WalletSettingsHomeScreen.route)
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.user),
                contentDescription = stringResource(id = R.string.user),
                modifier = Modifier
                    .width(20.dp)
                    .height(20.dp)
            )
        }
    }
}

@Composable
fun WalletHomeBody(
    rawCredentialsViewModel: IRawCredentialsViewModel,
    navController: NavController
) {
    val scope = rememberCoroutineScope()

    val rawCredentials by rawCredentialsViewModel.rawCredentials.collectAsState()

    if(rawCredentials.isNotEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp)
                    .padding(bottom = 60.dp)
            ) {
                items(rawCredentials) { rawCredential ->
                    AchievementCredentialItem(
                        rawCredential.rawCredential,
                        onDelete = {
                            scope.launch {
                                rawCredentialsViewModel.deleteRawCredential(id = rawCredential.id)
                            }
                        }
                    ).component()
                }
    //        item {
    //            vcs.map { vc ->
    //                GenericCredentialListItems(vc = vc)
    //            }
    //            ShareableCredentialListItems(mdocBase64 = mdocBase64)
    //        }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Button(
                    onClick = {
                        navController.navigate(Screen.ScanQRScreen.route)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CTAButtonBlue,
                        contentColor = Color.White,
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.scan_qr_code_white),
                            contentDescription = "QR Code Icon",
                            tint = Color.White,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Text(
                            text = "Scan to share",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                        )
                    }
                }
            }
        }
    } else {
        Column {
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.empty_wallet),
                contentDescription = stringResource(id = R.string.user),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

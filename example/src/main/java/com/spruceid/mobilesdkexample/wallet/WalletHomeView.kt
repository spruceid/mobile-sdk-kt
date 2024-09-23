package com.spruceid.mobilesdkexample.wallet

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spruceid.mobilesdkexample.R
import com.spruceid.mobilesdkexample.navigation.Screen
import com.spruceid.mobilesdkexample.ui.theme.Inter
import com.spruceid.mobilesdkexample.ui.theme.TextHeader
import com.spruceid.mobilesdkexample.ui.theme.MobileSdkTheme
import com.spruceid.mobilesdkexample.ui.theme.Primary
import com.spruceid.mobilesdkexample.utils.vcs
import com.spruceid.mobilesdkexample.utils.mdocBase64

@Composable
fun WalletHomeView(navController: NavController) {
    Column(
        Modifier
            .padding(all = 20.dp)
            .padding(top = 20.dp)
    ) {
        WalletHomeHeader(navController = navController)
        WalletHomeBody()
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
fun WalletHomeBody() {
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(top = 20.dp)
    ) {
        AchievementCredentialItem(rawCredential = "{}").component()
        vcs.map { vc ->
            GenericCredentialListItems(vc = vc)
        }
        ShareableCredentialListItems(mdocBase64 = mdocBase64)
    }
}

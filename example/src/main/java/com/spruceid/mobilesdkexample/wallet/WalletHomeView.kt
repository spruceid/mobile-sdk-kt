package com.spruceid.mobilesdkexample.wallet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spruceid.mobilesdkexample.ui.theme.Inter
import com.spruceid.mobilesdkexample.ui.theme.TextHeader
import com.spruceid.mobilesdkexample.ui.theme.MobileSdkTheme
import com.spruceid.mobilesdkexample.utils.vcs
import com.spruceid.mobilesdkexample.utils.mdocBase64

@Composable
fun WalletHomeView() {
    Column(
        Modifier
            .padding(all = 20.dp)
            .padding(top = 20.dp)
    ) {
        WalletHomeHeader()
        WalletHomeBody()
    }
}

@Composable
fun WalletHomeHeader() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Spruce Wallet",
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            color = TextHeader
        )
        Spacer(Modifier.weight(1f))
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
        vcs.map { vc ->
            GenericCredentialListItems(vc = vc)
        }
        ShareableCredentialListItems(mdocBase64 = mdocBase64)
    }
}

@Preview(showBackground = true)
@Composable
fun WalletHomeViewPreview() {
    MobileSdkTheme {
        WalletHomeView()
    }
}
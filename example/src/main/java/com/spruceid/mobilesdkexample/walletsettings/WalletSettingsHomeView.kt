package com.spruceid.mobilesdkexample.walletsettings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spruceid.mobilesdkexample.R
import com.spruceid.mobilesdkexample.ui.theme.Inter
import com.spruceid.mobilesdkexample.ui.theme.TextHeader
import com.spruceid.mobilesdkexample.ui.theme.VerifiedRedInvalid
import com.spruceid.mobilesdkexample.viewmodels.IRawCredentialsViewModel
import kotlinx.coroutines.launch

@Composable
fun WalletSettingsHomeView(
    navController: NavController,
    rawCredentialsViewModel: IRawCredentialsViewModel
) {
    Column(
        Modifier
            .padding(all = 20.dp)
            .padding(top = 20.dp)
    ) {
        WalletSettingsHomeHeader(
            onBack = {
                navController.popBackStack()
            }
        )
        WalletSettingsHomeBody(rawCredentialsViewModel)
    }
}

@Composable
fun WalletSettingsHomeHeader(
    onBack: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable {
            onBack()
        }
    ) {
        Image(
            painter = painterResource(id = R.drawable.chevron),
            contentDescription = stringResource(id = R.string.chevron),
            modifier = Modifier
                .rotate(180f)
                .scale(0.7f)
        )
        Text(
            text = "Wallet Settings",
            fontFamily = Inter,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            color = TextHeader,
            modifier = Modifier.padding(start = 10.dp)
        )
        Spacer(Modifier.weight(1f))
    }

}

@Composable
fun WalletSettingsHomeBody(rawCredentialsViewModel: IRawCredentialsViewModel) {
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .padding(horizontal = 20.dp)
            .padding(top = 10.dp),
    ) {
        Button(
            onClick = {
                scope.launch {
                    rawCredentialsViewModel.deleteAllRawCredentials()
                }
            },
            shape = RoundedCornerShape(5.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerifiedRedInvalid,
                contentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Delete all added credentials",
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
    }
}
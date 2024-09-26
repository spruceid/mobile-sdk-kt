package com.spruceid.mobilesdkexample.wallet

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.spruceid.mobilesdkexample.db.RawCredentials
import com.spruceid.mobilesdkexample.navigation.Screen
import com.spruceid.mobilesdkexample.ui.theme.CTAButtonGreen
import com.spruceid.mobilesdkexample.ui.theme.Inter
import com.spruceid.mobilesdkexample.ui.theme.MobileSdkTheme
import com.spruceid.mobilesdkexample.ui.theme.SecondaryButtonRed
import com.spruceid.mobilesdkexample.ui.theme.TextHeader
import com.spruceid.mobilesdkexample.viewmodels.IRawCredentialsViewModel
import com.spruceid.mobilesdkexample.viewmodels.RawCredentialsViewModelPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@SuppressLint("RestrictedApi")
@Composable
fun AddToWalletView(
    navController: NavHostController,
    rawCredential: String,
    rawCredentialsViewModel: IRawCredentialsViewModel
) {
    val credential = AchievementCredentialItem(rawCredential)
    val scope = rememberCoroutineScope()

    Column(
        Modifier
            .padding(all = 20.dp)
            .padding(top = 20.dp)
    ) {
        Text(
            text = "Review Info",
            textAlign = TextAlign.Center,
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = TextHeader,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
        )

        credential.borderedListComponent()

        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .weight(1f, false)
        ) {
            credential.detailsComponent()
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                scope.launch {
                    rawCredentialsViewModel.saveRawCredential(RawCredentials(
                        rawCredential = rawCredential
                    ))

                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(0)
                    }
                }
            },
            shape = RoundedCornerShape(5.dp),
            colors =  ButtonDefaults.buttonColors(
                containerColor = CTAButtonGreen,
                contentColor = Color.White,
            ),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Add to Wallet",
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }

        Button(
            onClick = {
                navController.popBackStack()
            },
            shape = RoundedCornerShape(5.dp),
            colors =  ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = SecondaryButtonRed,
            ),
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Close",
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                color = SecondaryButtonRed,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddToWalletPreview() {
    var navController: NavHostController = rememberNavController()

    MobileSdkTheme {
        AddToWalletView(
            navController = navController,
            rawCredential = "{}",
            rawCredentialsViewModel = RawCredentialsViewModelPreview()
        )
    }
}
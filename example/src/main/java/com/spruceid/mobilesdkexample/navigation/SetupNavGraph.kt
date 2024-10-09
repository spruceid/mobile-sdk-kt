package com.spruceid.mobilesdkexample.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.spruceid.mobilesdkexample.HomeView
import com.spruceid.mobilesdkexample.verifier.VerifyDLView
import com.spruceid.mobilesdkexample.verifier.VerifyEAView
import com.spruceid.mobilesdkexample.verifier.VerifyVCView
import com.spruceid.mobilesdkexample.verifiersettings.VerifierSettingsHomeView
import com.spruceid.mobilesdkexample.viewmodels.IRawCredentialsViewModel
import com.spruceid.mobilesdkexample.wallet.AddToWalletView
import com.spruceid.mobilesdkexample.walletsettings.WalletSettingsHomeView
import com.spruceid.mobilesdkexample.wallet.DispatchQRView

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    rawCredentialsViewModel: IRawCredentialsViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.HomeScreen.route
    ) {
        composable(
            route = Screen.HomeScreen.route,
        ) {
            HomeView(navController, rawCredentialsViewModel)
        }
        composable(
            route = Screen.VerifyDLScreen.route,
        ) {
            VerifyDLView(navController)
        }
        composable(
            route = Screen.VerifyEAScreen.route,
        ) {
            VerifyEAView(navController)
        }
        composable(
            route = Screen.VerifyVCScreen.route,
        ) {
            VerifyVCView(navController)
        }
        composable(
            route = Screen.VerifierSettingsHomeScreen.route,
        ) {
            VerifierSettingsHomeView(navController)
        }
        composable(
            route = Screen.WalletSettingsHomeScreen.route,
        ) {
            WalletSettingsHomeView(navController, rawCredentialsViewModel)
        }
        composable(
            route = Screen.AddToWalletScreen.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "spruceid://?sd-jwt={rawCredential}"
                }
            )
        ) { backStackEntry ->
            val rawCredential = backStackEntry.arguments?.getString("rawCredential")!!
            AddToWalletView(navController, rawCredential, rawCredentialsViewModel)
        }
        composable(
            route = Screen.ScanQRScreen.route,
            deepLinks = listOf(
                navDeepLink {
                    uriPattern = "oid4vp://{params}"
                }
            )
        ) {
            DispatchQRView(navController)
        }
    }
}

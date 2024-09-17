package com.spruceid.mobilesdkexample.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.spruceid.mobilesdkexample.HomeView
import com.spruceid.mobilesdkexample.verifier.VerifyDLView
import com.spruceid.mobilesdkexample.verifier.VerifyEAView
import com.spruceid.mobilesdkexample.verifier.VerifyVCView
import com.spruceid.mobilesdkexample.verifiersettings.VerifierSettingsHomeView

@Composable
fun SetupNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.HomeScreen.route
    ) {
        composable(
            route = Screen.HomeScreen.route,
        ) {
            HomeView(navController)
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

    }
}

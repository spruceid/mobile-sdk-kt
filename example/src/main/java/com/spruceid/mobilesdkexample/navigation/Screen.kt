package com.spruceid.mobilesdkexample.navigation

const val HOME_SCREEN_PATH = "home"
const val VERIFY_DL_PATH = "verify_dl"
const val VERIFY_EA_PATH = "verify_ea"
const val VERIFY_VC_PATH = "verify_vc"
const val VERIFIER_SETTINGS_HOME_PATH = "verifier_settings_home"
const val WALLET_SETTINGS_HOME_PATH = "wallet_settings_home"
const val ADD_TO_WALLET_PATH = "add_to_wallet/{rawCredential}"
const val OID4VP_PATH = "oid4vp/{params}"


sealed class Screen(val route: String) {
    object HomeScreen : Screen(HOME_SCREEN_PATH)
    object VerifyDLScreen : Screen(VERIFY_DL_PATH)
    object VerifyEAScreen : Screen(VERIFY_EA_PATH)
    object VerifyVCScreen : Screen(VERIFY_VC_PATH)
    object VerifierSettingsHomeScreen : Screen(VERIFIER_SETTINGS_HOME_PATH)
    object WalletSettingsHomeScreen : Screen(WALLET_SETTINGS_HOME_PATH)
    object AddToWalletScreen : Screen(ADD_TO_WALLET_PATH)
    object ScanQRScreen : Screen(OID4VP_PATH)
}
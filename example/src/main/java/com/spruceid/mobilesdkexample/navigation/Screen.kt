package com.spruceid.mobilesdkexample.navigation

const val HOME_SCREEN_PATH = "home"
const val VERIFY_DL_PATH = "verify_dl"
const val VERIFY_EA_PATH = "verify_ea"
const val VERIFY_VC_PATH = "verify_vc"
const val VERIFIER_SETTINGS_HOME_PATH = "verifier_settings_home"
const val WALLET_SETTINGS_HOME_PATH = "wallet_settings_home"
const val ADD_TO_WALLET_PATH = "add_to_wallet/{rawCredential}"
const val SCAN_QR_PATH = "scan_qr"
const val OID4VCI_PATH = "oid4vci"
const val HANDLE_OID4VP_PATH = "oid4vp/{url}"

sealed class Screen(val route: String) {
    object HomeScreen : Screen(HOME_SCREEN_PATH)
    object VerifyDLScreen : Screen(VERIFY_DL_PATH)
    object VerifyEAScreen : Screen(VERIFY_EA_PATH)
    object VerifyVCScreen : Screen(VERIFY_VC_PATH)
    object VerifierSettingsHomeScreen : Screen(VERIFIER_SETTINGS_HOME_PATH)
    object WalletSettingsHomeScreen : Screen(WALLET_SETTINGS_HOME_PATH)
    object AddToWalletScreen : Screen(ADD_TO_WALLET_PATH)
    object ScanQRScreen : Screen(SCAN_QR_PATH)
    object OID4VCIScreen : Screen(OID4VCI_PATH)
    object HandleOID4VP : Screen(HANDLE_OID4VP_PATH)
}

package com.spruceid.mobilesdkexample

import android.app.Application
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.spruceid.mobilesdkexample.db.AppDatabase
import com.spruceid.mobilesdkexample.db.RawCredentialsRepository
import com.spruceid.mobilesdkexample.navigation.SetupNavGraph
import com.spruceid.mobilesdkexample.ui.theme.Bg
import com.spruceid.mobilesdkexample.ui.theme.MobileSdkTheme
import com.spruceid.mobilesdkexample.viewmodels.IRawCredentialsViewModel
import com.spruceid.mobilesdkexample.viewmodels.RawCredentialsViewModelFactory

class MainActivity : ComponentActivity() {
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val deepLinkUri: Uri? = intent.data
        if (deepLinkUri != null) {
            // @TODO: integrate with the OID4VP flow
        }

        enableEdgeToEdge()
        setContent {
            MobileSdkTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = Bg,
                ) {
                    navController = rememberNavController()

                    val credentialsViewModel: IRawCredentialsViewModel by viewModels {
                        RawCredentialsViewModelFactory((application as MainApplication).rawCredentialsRepository)
                    }
                    SetupNavGraph(navController, credentialsViewModel)
                }
            }
        }
    }
}

class MainApplication : Application() {
    val db by lazy { AppDatabase.getDatabase(applicationContext) }
    val rawCredentialsRepository by lazy { RawCredentialsRepository(db.rawCredentialsDao()) }
}
package com.indicefossile.indiceapp.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.indicefossile.indiceapp.ui.components.HomeScreen
import com.indicefossile.indiceapp.ui.theme.IndiceAppTheme
import android.content.pm.ActivityInfo
import com.indicefossile.indiceapp.data.database.AppDatabase
import com.indicefossile.indiceapp.data.repository.ScannedProductRepository
import com.indicefossile.indiceapp.ui.viewmodel.ScannedProductViewModel


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val database = AppDatabase.getDatabase(this)
        val repository = ScannedProductRepository(database.scannedProductDao())
        val viewModel = ScannedProductViewModel(repository)
        setContent {
            IndiceAppTheme {
                HomeScreen(
                    viewModel = viewModel,
                    onScanClick = {
                        // Lancer ScanActivity dans l'application
                        val scanIntent = Intent(this, ScanActivity::class.java)
                        startActivity(scanIntent)
                    },
                    onWebsiteClick = {
                        // Ouvrir le site du catalogue
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://indicefossile.fr/catalogue"))
                        startActivity(browserIntent)
                    }
                )
            }
        }
    }
}

package com.indicefossile.indiceapp.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.indicefossile.indiceapp.ui.components.HomeScreen
import com.indicefossile.indiceapp.ui.theme.IndiceAppTheme
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.annotation.RequiresApi
import com.indicefossile.indiceapp.data.database.AppDatabase
import com.indicefossile.indiceapp.data.repository.ScannedProductRepository
import com.indicefossile.indiceapp.ui.viewmodel.ScannedProductViewModel
import androidx.core.net.toUri


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
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
                        val scanIntent = Intent(this, ScanActivity::class.java)
                        startActivity(scanIntent)
                    },
                    onWebsiteClick = {
                        val browserIntent = Intent(Intent.ACTION_VIEW,
                            "https://indicefossile.fr/catalogue".toUri())
                        startActivity(browserIntent)
                    },
                    onProductClick = { barcode ->
                        val detailIntent = Intent(this, DetailActivity::class.java).apply {
                            putExtra("barcode", barcode)
                        }
                        startActivity(detailIntent)
                    }
                )
            }
        }
    }
}

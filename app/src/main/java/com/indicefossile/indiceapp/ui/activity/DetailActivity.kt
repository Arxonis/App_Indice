package com.indicefossile.indiceapp.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.indicefossile.indiceapp.ui.components.ProductDetailScreen
import com.indicefossile.indiceapp.viewmodel.ProductViewModel

class DetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Récupère le code-barres passé en extra
        val barcode = intent.getStringExtra("barcode") ?: ""
        setContent {
            // Créez (ou récupérez via DI) votre ViewModel
            val viewModel = ProductViewModel()
            // Lancez la récupération du produit
            viewModel.fetchProduct(barcode)
            // Observer l'état du produit
            val product by viewModel.product.collectAsState()

            Surface(modifier = Modifier.fillMaxSize()) {
                if (product != null) {
                    ProductDetailScreen(product = product!!)
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Produit non trouvé")
                    }
                }
            }
        }
    }
}

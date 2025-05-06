package com.indicefossile.indiceapp.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.indicefossile.indiceapp.ui.components.ProductDetailScreen
import com.indicefossile.indiceapp.ui.components.ProductDetailScreenWithTopBar
import com.indicefossile.indiceapp.viewmodel.ProductViewModel

class DetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val barcode = intent.getStringExtra("barcode") ?: ""
        val viewModel = ProductViewModel()
        viewModel.fetchProduct(barcode)

        setContent {
            val product by viewModel.product.collectAsState()
            // Envoi des résultats uniquement lorsque le produit est trouvé
            LaunchedEffect(product) {
                if (product != null) {
                    val resultIntent = Intent().apply {
                        putExtra("product_name", product!!.product_name)
                        putExtra("barcode", barcode)
                        putExtra("CO2_TOTAL", product?.ecoscore_data?.agribalyse?.co2_total)
                    }
                    setResult(RESULT_OK, resultIntent)
                }
            }

            Surface(modifier = Modifier.fillMaxSize()) {
                if (product != null) {
                    ProductDetailScreenWithTopBar(product = product!!)
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


package com.indicefossile.indiceapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indicefossile.indiceapp.data.model.ScannedProduct
import com.indicefossile.indiceapp.data.repository.ScannedProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ScannedProductViewModel(private val repository: ScannedProductRepository) : ViewModel() {

    val allProducts: Flow<List<ScannedProduct>> = repository.allProducts

    fun insertProduct(barcode: String, name: String, imageUrl: String?, CO2_TOTAL: Double?, GreenScore: String?) {
        viewModelScope.launch {
            val product = ScannedProduct(
                barcode = barcode,
                name = name,
                imageUrl = imageUrl,
                timestamp = System.currentTimeMillis(),
                CO2_TOTAL = CO2_TOTAL,
                GreenScore = GreenScore
            )
            product.GreenScore?.let { Log.d("PRINTED", it) }
            repository.insertProduct(product)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}

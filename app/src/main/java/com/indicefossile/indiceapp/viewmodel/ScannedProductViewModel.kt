package com.indicefossile.indiceapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indicefossile.indiceapp.data.model.ScannedProduct
import com.indicefossile.indiceapp.data.repository.ScannedProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ScannedProductViewModel(private val repository: ScannedProductRepository) : ViewModel() {

    val allProducts: Flow<List<ScannedProduct>> = repository.allProducts

    fun insertProduct(barcode: String, name: String, imageUrl: String?) {
        viewModelScope.launch {
            val product = ScannedProduct(
                barcode = barcode,
                name = name,
                imageUrl = imageUrl,
                timestamp = System.currentTimeMillis()
            )
            repository.insertProduct(product)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}

package com.indicefossile.indiceapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.indicefossile.indiceapp.data.model.ScannedProduct
import com.indicefossile.indiceapp.data.repository.ScannedProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ScannedProductViewModel(private val repository: ScannedProductRepository) : ViewModel() {

    val allProducts: Flow<List<ScannedProduct>> = repository.allProducts

    fun insertProduct(product: ScannedProduct) {
        viewModelScope.launch {
            repository.insertProduct(product)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }
}

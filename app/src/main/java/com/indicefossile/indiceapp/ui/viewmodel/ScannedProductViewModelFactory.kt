package com.indicefossile.indiceapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.indicefossile.indiceapp.data.repository.ScannedProductRepository

class ScannedProductViewModelFactory(private val repository: ScannedProductRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScannedProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScannedProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

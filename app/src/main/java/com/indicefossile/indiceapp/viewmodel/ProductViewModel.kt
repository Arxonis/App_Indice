package com.indicefossile.indiceapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.indicefossile.indiceapp.data.model.Product
import com.indicefossile.indiceapp.data.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product

    fun fetchProduct(barcode: String) {
        viewModelScope.launch {
            try {
                val product = RetrofitInstance.api.getProduct(barcode)
                Log.d("ProductViewModel", "Received JSON: ${Gson().toJson(product)}")
                _product.value = product.product
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error fetching product", e)
                _product.value = null
            }
        }
    }
}

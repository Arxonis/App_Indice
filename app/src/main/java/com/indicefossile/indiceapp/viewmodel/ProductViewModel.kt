package com.indicefossile.indiceapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.indicefossile.indiceapp.data.model.Product
import com.indicefossile.indiceapp.data.model.ScannedProduct
import com.indicefossile.indiceapp.data.network.RetrofitInstance
import com.indicefossile.indiceapp.ui.utils.getProductImageUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    private val _product = MutableStateFlow<Product?>(null)
    val product: StateFlow<Product?> = _product

    fun fetchProduct(barcode: String) {
        viewModelScope.launch {
            try {
                // Récupérer le produit depuis l'API
                val productResponse = RetrofitInstance.api.getProduct(barcode)
                Log.d("ProductViewModel", "Received JSON: ${Gson().toJson(productResponse)}")

                // Extraire l'image si elle est présente
                val imageUrl = productResponse.product.images?.front_fr?.let { frontImage ->
                    getProductImageUrl(
                        productBarcode = productResponse.product.code,
                        imageType = "front_fr",
                        resolution = "400",
                        rev = frontImage.rev
                    )
                }

                // Créer un ScannedProduct avec l'image et autres informations
                val scannedProduct =
                    ScannedProduct(
                    barcode = productResponse.product.code,
                    name = productResponse.product.product_name ?: "Nom non disponible",
                    imageUrl = imageUrl,
                    timestamp = System.currentTimeMillis()
                )

                // Enregistrer le produit avec l'image dans la base de données (utiliser ton repository ici)
                // Exemple : repository.insertProduct(scannedProduct)

                _product.value = productResponse.product // Mettre à jour l'état du produit
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error fetching product", e)
                _product.value = null
            }
        }
    }

}

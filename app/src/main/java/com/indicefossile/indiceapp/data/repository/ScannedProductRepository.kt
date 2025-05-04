package com.indicefossile.indiceapp.data.repository

import android.util.Log
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.indicefossile.indiceapp.data.dao.ScannedProductDao
import com.indicefossile.indiceapp.data.model.ScannedProduct
import com.indicefossile.indiceapp.data.network.RetrofitInstance
import kotlinx.coroutines.flow.Flow

class ScannedProductRepository(private val dao: ScannedProductDao) {

    val allProducts: Flow<List<ScannedProduct>> = dao.getAllProducts()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ScannedProduct) : Unit {
        // Logguer l'URL + barcode avant l'insertion
        val barcode = product.barcode
        println("Requête envoyée pour le barcode : $barcode")
        Log.d("ScannedProduRepository", "Requête envoyée pour le barcode : $barcode")

        // Appeler Retrofit pour obtenir les détails du produit avec le barcode
        val productResponse = RetrofitInstance.getProductWithLogging(barcode)
        Log.d("ScannedProductRository", "Données de produit reçues : $productResponse")
        dao.insertProduct(product)
    }

    suspend fun clearHistory() : Unit {
        dao.clearHistory()
    }
}

package com.indicefossile.indiceapp.data.network

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.indicefossile.indiceapp.data.model.ProductResponse

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.net/") // Vérifie si c’est la bonne URL de base
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: OpenFoodFactsApi by lazy {
        retrofit.create(OpenFoodFactsApi::class.java)
    }

    // Wrapper pour logguer l'appel avec le barcode
    suspend fun getProductWithLogging(barcode: String): ProductResponse {
        // Logguer l'URL + barcode avant d'appeler l'API
        Log.d("OpenFoodFacts", "Requête envoyée pour le barcode : $barcode")

        // Appeler réellement l'API avec le barcode
        return api.getProduct(barcode)
    }
}

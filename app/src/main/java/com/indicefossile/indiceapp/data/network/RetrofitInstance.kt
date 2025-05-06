package com.indicefossile.indiceapp.data.network

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.indicefossile.indiceapp.data.model.ProductResponse

object RetrofitInstance {
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: OpenFoodFactsApi by lazy {
        retrofit.create(OpenFoodFactsApi::class.java)
    }

    suspend fun getProductWithLogging(barcode: String): ProductResponse {
        Log.d("OpenFoodFacts", "Requête envoyée pour le barcode : $barcode")

        return api.getProduct(barcode)
    }
}

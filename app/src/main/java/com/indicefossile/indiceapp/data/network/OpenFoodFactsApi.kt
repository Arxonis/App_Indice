package com.indicefossile.indiceapp.data.network

import com.indicefossile.indiceapp.data.model.Product
import com.indicefossile.indiceapp.data.model.ProductResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenFoodFactsApi {
    @GET("api/v0/product/{barcode}.json")
    suspend fun getProduct(
        @Path("barcode") barcode: String,
    ): ProductResponse

}








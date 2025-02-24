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
        @Query("fields") fields: String = "code,product_name,brands,quantity,ingredients_text,nutrition_grades,nutriscore_data,nutriscore_score,nutriments,agribalyse,ecoscore_data,nova_groups_tags,images"
    ): ProductResponse

}








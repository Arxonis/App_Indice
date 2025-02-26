package com.indicefossile.indiceapp.data.repository

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.indicefossile.indiceapp.data.dao.ScannedProductDao
import com.indicefossile.indiceapp.data.model.ScannedProduct
import kotlinx.coroutines.flow.Flow

class ScannedProductRepository(private val dao: ScannedProductDao) {

    val allProducts: Flow<List<ScannedProduct>> = dao.getAllProducts()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ScannedProduct) : Unit {
        dao.insertProduct(product)
    }

    suspend fun clearHistory() : Unit {
        dao.clearHistory()
    }
}

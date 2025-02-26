package com.indicefossile.indiceapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.indicefossile.indiceapp.data.model.ScannedProduct

@Dao
interface ScannedProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ScannedProduct): Long

    @Query("SELECT * FROM scanned_products ORDER BY timestamp DESC")
    fun getAllProducts(): Flow<List<ScannedProduct>>

    @Query("DELETE FROM scanned_products")
    suspend fun clearHistory()
}

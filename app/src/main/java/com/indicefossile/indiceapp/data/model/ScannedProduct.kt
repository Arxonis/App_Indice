package com.indicefossile.indiceapp.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_products")
data class ScannedProduct(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,  // ID unique
    val barcode: String,  // Code-barres du produit
    val name: String,  // Nom du produit
    val timestamp: Long = System.currentTimeMillis(),
    val imageUrl: String? = null,
    val CO2_TOTAL: Double?,
    val GreenScore: String?
)

package com.indicefossile.indiceapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.indicefossile.indiceapp.data.dao.ScannedProductDao
import com.indicefossile.indiceapp.data.model.ScannedProduct

@Database(entities = [ScannedProduct::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scannedProductDao(): ScannedProductDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scanned_products ADD COLUMN CO2_TOTAL REAL")
                db.execSQL("ALTER TABLE scanned_products ADD COLUMN GreenScore TEXT")
            }
        }

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "scanned_products_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}


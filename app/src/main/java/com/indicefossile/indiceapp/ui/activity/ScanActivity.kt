package com.indicefossile.indiceapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.indicefossile.indiceapp.R
import com.indicefossile.indiceapp.data.database.AppDatabase
import com.indicefossile.indiceapp.data.model.ScannedProduct
import com.indicefossile.indiceapp.data.repository.ScannedProductRepository
import com.indicefossile.indiceapp.ui.viewmodel.ScannedProductViewModel
import com.indicefossile.indiceapp.ui.viewmodel.ScannedProductViewModelFactory

class ScanActivity : AppCompatActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { ScannedProductRepository(database.scannedProductDao()) }

    private val viewModel: ScannedProductViewModel by viewModels {
        ScannedProductViewModelFactory(repository)
    }

    // 🔹 Lance DetailActivity et attend le résultat
    private val detailLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val productName = result.data?.getStringExtra("product_name")
            val barcode = result.data?.getStringExtra("barcode")

            if (productName != null && barcode != null) {
                val scannedProduct = ScannedProduct(barcode = barcode, name = productName)
                viewModel.insertProduct(scannedProduct) // 🔹 Sauvegarde dans la base de données
                Toast.makeText(this, "Produit enregistré: $productName", Toast.LENGTH_SHORT).show()
            }
        }
        finish()
    }

    // 🔹 Lance le scan
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(this, "Scan annulé", Toast.LENGTH_SHORT).show()
        } else {
            val codeBarre = result.contents
            Toast.makeText(this, "Code scanné : $codeBarre", Toast.LENGTH_SHORT).show()

            // 🔹 Lancement de DetailActivity pour récupérer le vrai nom du produit
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("barcode", codeBarre)
            }
            detailLauncher.launch(intent) // 🚀 Attend la réponse de DetailActivity
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)
        startScan() // 🔹 Démarre le scan directement
    }

    private fun startScan() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            setPrompt("Scannez un code-barres")
            setCameraId(0)
            setBeepEnabled(false)
            setOrientationLocked(false)
        }
        barcodeLauncher.launch(options)
    }
}

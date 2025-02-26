package com.indicefossile.indiceapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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

    // Initialisation de la base de données et du repository
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { ScannedProductRepository(database.scannedProductDao()) }

    // Initialisation du ViewModel avec la Factory
    private val viewModel: ScannedProductViewModel by viewModels {
        ScannedProductViewModelFactory(repository)
    }

    // Prépare le launcher pour le scan
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(this, "Scan annulé", Toast.LENGTH_SHORT).show()
        } else {
            // Récupération du code scanné
            val codeBarre = result.contents
            Toast.makeText(this, "Code scanné : $codeBarre", Toast.LENGTH_SHORT).show()

            // Création de l'objet produit scanné
            val scannedProduct = ScannedProduct(barcode = codeBarre, name = )

            // Insertion du produit dans la base de données
            viewModel.insertProduct(scannedProduct)

            // Lancement de DetailActivity avec le code scanné
            val intent = Intent(this, DetailActivity::class.java)
            intent.putExtra("barcode", codeBarre)
            startActivity(intent)
            finish() // Ferme ScanActivity après le scan
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan) // Assure-toi que ce layout existe

        // Lance immédiatement le scan
        startScan()
    }

    private fun startScan() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            setPrompt("Scannez un code-barres")
            setCameraId(0) // Utilise la caméra arrière
            setBeepEnabled(false)
            setOrientationLocked(false)
        }
        barcodeLauncher.launch(options)
    }
}

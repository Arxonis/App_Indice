package com.indicefossile.indiceapp.ui.activity

import android.app.AlertDialog
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

    private val scannedProducts = mutableListOf<ScannedProduct>() // 🔹 Liste pour le mode multiple
    private var isMultipleScan = false // 🔹 Détermine le mode de scan

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        if (savedInstanceState == null) {
            showScanModeDialog() // 🔥 Demande à l'utilisateur de choisir un mode
        }
    }

    // 🔹 Demande à l'utilisateur de choisir entre scan unique et multiple
    private fun showScanModeDialog() {
        val options = arrayOf("Scan Unique", "Scan Multiple")
        AlertDialog.Builder(this)
            .setTitle("Choisissez un mode de scan")
            .setItems(options) { _, which ->
                isMultipleScan = (which == 1)
                startScan() // 🔥 Démarrer le scan après le choix
            }
            .setCancelable(false)
            .show()
    }

    // 🔹 Gestion du scan unique ou multiple
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            if (isMultipleScan && scannedProducts.isNotEmpty()) {
                saveAllScannedProducts() // ✅ Enregistrer tous les produits en mode multiple
            } else {
                Toast.makeText(this, "Scan annulé", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            val codeBarre = result.contents
            Toast.makeText(this, "Code scanné : $codeBarre", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra("barcode", codeBarre)
            }
            if (!isMultipleScan)
                detailLauncher.launch(intent) // 🚀 Attend la réponse de DetailActivity
        }
    }

    // 🔹 Gestion de la récupération du nom du produit
    private val detailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val productName = result.data?.getStringExtra("product_name")
                val barcode = result.data?.getStringExtra("barcode")

                if (productName != null && barcode != null) {
                    val scannedProduct = ScannedProduct(barcode = barcode, name = productName)

                    if (isMultipleScan) {
                        scannedProducts.add(scannedProduct)
                        Toast.makeText(this, "$productName ajouté", Toast.LENGTH_SHORT).show()
                        startScan() // 🔄 Relance un nouveau scan
                    } else {
                        viewModel.insertProduct(scannedProduct) // ✅ Mode unique : Enregistrement immédiat
                        Toast.makeText(this, "Produit enregistré: $productName", Toast.LENGTH_SHORT)
                            .show()
                        finish()
                    }
                }
            }
        }

    private fun startScan() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            setPrompt(if (isMultipleScan) "Scannez un produit (annulez pour terminer)" else "Scannez un code-barres")
            setCameraId(0)
            setBeepEnabled(false)
            setOrientationLocked(false)
        }
        barcodeLauncher.launch(options)
    }

    private fun saveAllScannedProducts() {
        for (product in scannedProducts)
            viewModel.insertProduct(product)
        Toast.makeText(this, "${scannedProducts.size} produit(s) enregistré(s)", Toast.LENGTH_SHORT)
            .show()
        finish()
    }
}
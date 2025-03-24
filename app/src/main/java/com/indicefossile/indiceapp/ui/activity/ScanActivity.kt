package com.indicefossile.indiceapp.ui.activity

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.indicefossile.indiceapp.data.database.AppDatabase
import com.indicefossile.indiceapp.data.model.ScannedProduct
import com.indicefossile.indiceapp.data.repository.ScannedProductRepository
import com.indicefossile.indiceapp.ui.viewmodel.ScannedProductViewModel
import com.indicefossile.indiceapp.ui.viewmodel.ScannedProductViewModelFactory
import com.indicefossile.indiceapp.viewmodel.ProductViewModel

class ScanActivity : AppCompatActivity() {
    private val productViewModel by viewModels<ProductViewModel>()
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { ScannedProductRepository(database.scannedProductDao()) }
    private val viewModel: ScannedProductViewModel by viewModels {
        ScannedProductViewModelFactory(repository)
    }

    private val scannedProducts = mutableListOf<ScannedProduct>()
    private var scannedBarcode: String? by mutableStateOf(null)

    private var isMultipleScan = false // ✅ Ne pas déclarer en `mutableStateOf` ici !

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            // ✅ Si l'activité est recréée, on récupère le mode choisi et on affiche directement l'UI
            isMultipleScan = savedInstanceState.getBoolean("isMultipleScan", false)
            setContent { ScanScreen(isMultipleScan) }
            startScan()
        } else {
            // ✅ Si c'est la première fois, on demande à l'utilisateur de choisir
            showScanModeDialog()
        }
    }

    // ✅ Sauvegarde le choix en cas de recréation de l'activité
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isMultipleScan", isMultipleScan)
    }


    /**
     * Affiche la boîte de dialogue et attend la réponse avant d'afficher `ScanScreen`
     */
    private fun showScanModeDialog() {
        val options = arrayOf("Scan Unique", "Scan Multiple")
        AlertDialog.Builder(this)
            .setTitle("Choisissez un mode de scan")
            .setItems(options) { _, which ->
                isMultipleScan = (which == 1) // ✅ Mise à jour du mode choisi

                // ✅ Maintenant que l'utilisateur a choisi, on affiche `ScanScreen`
                setContent { ScanScreen(isMultipleScan) }

                // ✅ Démarrer le scan après l'affichage de l'écran
                startScan()
            }
            .setCancelable(false)
            .show()
    }

    @Composable
    fun ScanScreen(isMultipleScan: Boolean) {
        val context = LocalContext.current
        val product by productViewModel.product.collectAsState()

        LaunchedEffect(scannedBarcode) {
            scannedBarcode?.let { barcode ->
                productViewModel.fetchProduct(barcode)
            }
        }

        if (product != null) {
            val scannedProduct = product!!.product_name?.let {
                ScannedProduct(
                    barcode = scannedBarcode ?: "",
                    name = it
                )
            }

            if (isMultipleScan) {
                scannedProduct?.let { scannedProducts.add(it) }
                Toast.makeText(context, "${product!!.product_name} ajouté", Toast.LENGTH_SHORT).show()
                scannedBarcode = null
                startScan()
            } else {
                scannedProduct?.let { viewModel.insertProduct(it) }
                Toast.makeText(context, "Produit enregistré: ${product!!.product_name}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            if (isMultipleScan && scannedProducts.isNotEmpty()) {
                saveAllScannedProducts()
            } else {
                Toast.makeText(this, "Scan annulé", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            scannedBarcode = result.contents
            Toast.makeText(this, "Code scanné : $scannedBarcode", Toast.LENGTH_SHORT).show()
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
        for (product in scannedProducts) {
            viewModel.insertProduct(product)
        }
        Toast.makeText(this, "${scannedProducts.size} produit(s) enregistré(s)", Toast.LENGTH_SHORT).show()
        finish()
    }
}

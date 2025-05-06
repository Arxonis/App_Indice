package com.indicefossile.indiceapp.ui.activity

import android.app.AlertDialog
import android.content.Intent
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
import com.indicefossile.indiceapp.ui.utils.getProductImageUrl
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

    private val scannedProducts = mutableSetOf<ScannedProduct>()  // Utilisation d'un Set pour éviter les doublons
    private var scannedBarcode: String? by mutableStateOf(null)

    private var isMultipleScan = false // Mode de scan
    private var hasScannedOnce = false // Flag pour empêcher la relance du scan en mode unique

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState != null) {
            isMultipleScan = savedInstanceState.getBoolean("isMultipleScan", false)
            hasScannedOnce = savedInstanceState.getBoolean("hasScannedOnce", false)
            if (isMultipleScan) {
                setContent { ScanScreen(isMultipleScan) }
                if (!hasScannedOnce) startScan()
            } else {
                setContent { ScanScreen(isMultipleScan) }
            }
        } else {
            showScanModeDialog()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isMultipleScan", isMultipleScan)
        outState.putBoolean("hasScannedOnce", hasScannedOnce)
    }

    private fun showScanModeDialog() {
        val options = arrayOf("Scan Unique", "Scan Multiple")
        AlertDialog.Builder(this)
            .setTitle("Choisissez un mode de scan")
            .setItems(options) { _, which ->
                isMultipleScan = (which == 1)
                setContent { ScanScreen(isMultipleScan) }
                startScan()
            }
            .setCancelable(false)
            .show()
    }

    fun extractNumericValue(value: String): Double? {
        val isKg = value.contains("kg", ignoreCase = true)
        val numericValue = value.replace("[^0-9.,]".toRegex(), "")
            .replace(",", ".")
        val result = numericValue.toDoubleOrNull()
        return if (result != null && isKg) result * 1000 else result
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

        if (product != null && scannedBarcode != null) {
            // Récupérer l'URL de l'image
            val imageUrl = product!!.images?.front_fr?.let { frontImage ->
                getProductImageUrl(
                    productBarcode = product!!.code,
                    imageType = "front_fr",
                    resolution = "400",
                    rev = frontImage.rev
                )
            }

            val scannedProduct = ScannedProduct(
                barcode = scannedBarcode ?: "",
                name = product!!.product_name ?: "Nom non disponible",
                imageUrl = imageUrl,
                CO2_TOTAL = (product?.ecoscore_data?.agribalyse?.co2_total?.div(1000))?.times(extractNumericValue(product?.quantity ?: "") ?: 1.0),
                GreenScore = product?.ecoscore_data?.grade
            )

            if (isMultipleScan) {
                if (!scannedProducts.contains(scannedProduct)) {
                    scannedProducts.add(scannedProduct)
                    Toast.makeText(context, "${product!!.product_name} ajouté", Toast.LENGTH_SHORT).show()
                }
                scannedBarcode = null
                startScan() // Relance le scan
            } else {
                val detailIntent = Intent(this, DetailActivity::class.java).apply {
                    putExtra("barcode", scannedBarcode)
                }
                startActivity(detailIntent)
                viewModel.insertProduct(
                    barcode = scannedProduct.barcode,
                    name = scannedProduct.name,
                    imageUrl = scannedProduct.imageUrl,
                    CO2_TOTAL = scannedProduct.CO2_TOTAL,
                    scannedProduct.GreenScore
                )
                Toast.makeText(context, "Produit enregistré: ${product!!.product_name}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }


    // Enregistre les produits scannés en mode multiple
    private fun saveAllScannedProducts() {
        for (product in scannedProducts) {
            viewModel.insertProduct(product.barcode, product.name, product.imageUrl, product.CO2_TOTAL, product.GreenScore)
        }
        Toast.makeText(this, "${scannedProducts.size} produit(s) enregistré(s)", Toast.LENGTH_SHORT).show()
        finish()
    }


    // Lance le scanner de code-barres
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            if (isMultipleScan && scannedProducts.isNotEmpty()) {
                saveAllScannedProducts() // Sauvegarde tous les produits scannés en mode multiple
            } else {
                Toast.makeText(this, "Scan annulé", Toast.LENGTH_SHORT).show()
                finish() // Annule et ferme si pas de scan
            }
        } else {
            scannedBarcode = result.contents
            hasScannedOnce = true // Après le premier scan en mode unique, ne relance plus le scan
            Toast.makeText(this, "Code scanné : $scannedBarcode", Toast.LENGTH_SHORT).show()
        }
    }

    // Démarre le scanner
    private fun startScan() {
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.ALL_CODE_TYPES)
            setPrompt(if (isMultipleScan) "Scannez un produit (annulez pour terminer)" else "Scannez un code-barres")
            setCameraId(0)
            setBeepEnabled(false)
            setOrientationLocked(false)
        }
        barcodeLauncher.launch(options) // Lance le scanner
    }
}


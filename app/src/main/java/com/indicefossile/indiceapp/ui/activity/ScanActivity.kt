// package & imports inchangés
package com.indicefossile.indiceapp.ui.activity

import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import android.Manifest
import androidx.compose.ui.viewinterop.AndroidView
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.indicefossile.indiceapp.data.database.AppDatabase
import com.indicefossile.indiceapp.data.model.ScannedProduct
import com.indicefossile.indiceapp.data.repository.ScannedProductRepository
import com.indicefossile.indiceapp.ui.utils.getProductImageUrl
import com.indicefossile.indiceapp.ui.viewmodel.ScannedProductViewModel
import com.indicefossile.indiceapp.ui.viewmodel.ScannedProductViewModelFactory
import com.indicefossile.indiceapp.viewmodel.ProductViewModel
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class ScanActivity : AppCompatActivity() {

    private val productViewModel by viewModels<ProductViewModel>()
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val repository by lazy { ScannedProductRepository(database.scannedProductDao()) }
    private val viewModel: ScannedProductViewModel by viewModels {
        ScannedProductViewModelFactory(repository)
    }

    private val cameraExecutor = Executors.newSingleThreadExecutor()
    private var isMultipleScan = false
    private var hasScannedOnce = false
    private val scannedProducts = mutableStateListOf<ScannedProduct>()
    private var scannedBarcode by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                0
            )
        }

        if (savedInstanceState != null) {
            isMultipleScan = savedInstanceState.getBoolean("isMultipleScan", false)
            hasScannedOnce = savedInstanceState.getBoolean("hasScannedOnce", false)
            setContent { ScanScreen(isMultipleScan) }
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
            }
            .setCancelable(false)
            .show()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ScanScreen(isMultiple: Boolean) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val product by productViewModel.product.collectAsState()
        val scaffoldState = rememberBottomSheetScaffoldState()

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = 64.dp,
            sheetContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .background(Color.Gray, shape = MaterialTheme.shapes.small)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Produits scannés", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn {
                        items(scannedProducts) { product ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!product.imageUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = product.imageUrl,
                                        contentDescription = "Image produit",
                                        modifier = Modifier
                                            .size(48.dp)
                                            .padding(end = 8.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = product.name ?: product.barcode,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "Code : ${product.barcode}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }

                                IconButton(onClick = {
                                    scannedProducts.remove(product)
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Supprimer"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Box(Modifier.padding(padding)) {
                CameraPreview { code ->
                    if (!isMultiple && hasScannedOnce) return@CameraPreview
                    hasScannedOnce = true
                    scannedBarcode = code
                    Toast.makeText(context, "Code scanné : $code", Toast.LENGTH_SHORT).show()
                    productViewModel.fetchProduct(code)
                }
            }
        }

        LaunchedEffect(scannedBarcode) {
            scannedBarcode?.let { productViewModel.fetchProduct(it) }
        }

        LaunchedEffect(product) {
            val code = scannedBarcode
            val prod = product
            if (!code.isNullOrEmpty() && prod != null) {
                val imageUrl = prod.images?.front_fr?.let {
                    getProductImageUrl(code, "front_fr", "400", it.rev)
                }
                val scanned = ScannedProduct(
                    barcode = code,
                    name = prod.product_name ?: "Nom non disponible",
                    imageUrl = imageUrl,
                    CO2_TOTAL = prod.ecoscore_data?.agribalyse?.co2_total?.div(1000)
                        ?.times(extractNumericValue(prod.quantity ?: "") ?: 1.0),
                    GreenScore = prod.ecoscore_data?.grade
                )

                if (isMultiple) {
                    scannedProducts.add(scanned)
                    Toast.makeText(context, "${scanned.name} ajouté", Toast.LENGTH_SHORT).show()
                    scannedBarcode = null
                } else {
                    viewModel.insertProduct(
                        scanned.barcode,
                        scanned.name,
                        scanned.imageUrl,
                        scanned.CO2_TOTAL,
                        scanned.GreenScore
                    )
                    context.startActivity(Intent(context, DetailActivity::class.java).apply {
                        putExtra("barcode", scanned.barcode)
                    })
                    (context as? ScanActivity)?.finish()
                }
            }
        }
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    @Composable
    fun CameraPreview(onBarcodeScanned: (String) -> Unit) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val preview = Preview.Builder().build()
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                    .build()
                val scanner = BarcodeScanning.getClient(options)

                imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                barcodes.firstOrNull()?.rawValue?.let { onBarcodeScanned(it) }
                            }
                            .addOnFailureListener { Log.e("Scanner", "Échec du scan", it) }
                            .addOnCompleteListener { imageProxy.close() }
                    } else {
                        imageProxy.close()
                    }
                }

                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    private fun extractNumericValue(value: String): Double? {
        val regex = """(\d[\d.,]*)(\s*(kg|g|l|cl))?""".toRegex(RegexOption.IGNORE_CASE)
        val match = regex.find(value) ?: return null
        val numeric = match.groupValues[1].replace(",", ".")
        val unit = match.groupValues[3].lowercase()

        val base = numeric.toDoubleOrNull() ?: return null
        return when (unit) {
            "kg" -> base * 1000
            "g" -> base
            "l" -> base * 1000
            "cl" -> base * 10
            else -> base
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isMultipleScan && scannedProducts.isNotEmpty()) {
            scannedProducts.forEach {
                viewModel.insertProduct(it.barcode, it.name, it.imageUrl, it.CO2_TOTAL, it.GreenScore)
            }
            Toast.makeText(this, "${scannedProducts.size} produit(s) enregistré(s)", Toast.LENGTH_SHORT).show()
        }
    }
}

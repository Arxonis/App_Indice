// ScanActivity.kt
package com.indicefossile.indiceapp.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.indicefossile.indiceapp.R

class ScanActivity : AppCompatActivity() {

    // Préparez le launcher pour le scan
    private val barcodeLauncher = registerForActivityResult(ScanContract()) { result ->
        if (result.contents == null) {
            Toast.makeText(this, "Scan annulé", Toast.LENGTH_SHORT).show()
        } else {
            // On lance DetailActivity en passant le code scanné
            val codeBarre = result.contents
            Toast.makeText(this, "Code scanné : $codeBarre", Toast.LENGTH_SHORT).show()
            val intent = android.content.Intent(this, DetailActivity::class.java)
            intent.putExtra("barcode", codeBarre)
            startActivity(intent)
            finish() // Optionnel : fermer ScanActivity après le scan
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan) // Vous devez définir ce layout avec la vue de scan

        // Lancez immédiatement le scan (ou via un bouton dans le layout)
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

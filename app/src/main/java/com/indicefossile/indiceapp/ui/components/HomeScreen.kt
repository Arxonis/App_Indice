package com.indicefossile.indiceapp.ui.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.indicefossile.indiceapp.R
import com.indicefossile.indiceapp.data.model.ScannedProduct
import com.indicefossile.indiceapp.ui.viewmodel.ScannedProductViewModel
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    viewModel: ScannedProductViewModel,
    onScanClick: () -> Unit,
    onWebsiteClick: () -> Unit,
    onProductClick: (String) -> Unit,
) {
    val scannedProducts by viewModel.allProducts.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.indice_logo),
            contentDescription = "Logo Indice Fossile",
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Historique",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Deux colonnes
        Row(modifier = Modifier.weight(1f)) {

            // Colonne gauche
            if (scannedProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucun produit scanné pour le moment.", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    items(scannedProducts) { product ->
                        ScannedProductItem(product, onProductClick)
                    }
                }
            }

            // Colonne droite
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(start = 8.dp)
            ) {
                var selectedPeriod by remember { mutableStateOf("Semaine") }

                // Exemple de données
                val fakeData = listOf(
                    Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -6) }.time to 4.2f,
                    Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -5) }.time to null,
                    Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -4) }.time to 3.8f,
                    Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -3) }.time to null,
                    Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -2) }.time to 5.0f,
                    Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }.time to 4.1f,
                    Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 0) }.time to 4.7f
                )

                Text(
                    text = "Statistiques",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Sélecteur de période
                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                    listOf("Jour", "Semaine", "Mois", "Année").forEach { period ->
                        Button(
                            onClick = { selectedPeriod = period },
                            modifier = Modifier
                                .padding(4.dp)
                                .weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedPeriod == period)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(text = period)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Graphique
                SimpleLineChart(fakeData, selectedPeriod)

                Spacer(modifier = Modifier.height(16.dp))

                // Trois lignes
                Text("CO₂ : —", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 4.dp))
                Text("Énergie : —", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 4.dp))
                Text("Note globale : —", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(vertical = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Boutons bas
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onScanClick,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text("Scanner")
            }
            Button(
                onClick = onWebsiteClick,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text("Catalogue")
            }
        }
    }
}

@Composable
fun ScannedProductItem(product: ScannedProduct, onProductClick: (String) -> Unit) {
    val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(product.timestamp))

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onProductClick(product.barcode) },
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberAsyncImagePainter(product.imageUrl),
                contentDescription = "Image du produit",
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Nom: ${product.name}", style = MaterialTheme.typography.bodyLarge)
            Text("Code-barres: ${product.barcode}", style = MaterialTheme.typography.bodyMedium)
            Text("Scanné le : $formattedDate", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun SimpleLineChart(
    rawData: List<Pair<Date, Float?>>,
    period: String
) {
    // Filtrer les données selon la période sélectionnée
    val filteredData = when (period) {
        "Jour" -> rawData.filter { it.first == Date() }
        "Semaine" -> rawData.filter { it.first.after(Date(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000)) }
        "Mois" -> rawData.filter { it.first.month == Date().month }
        "Année" -> rawData.filter { it.first.year == Date().year }
        else -> rawData
    }

    // Dessiner un graphique simplifié
    androidx.compose.foundation.Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
        .padding(16.dp)) {
        // Exemple de dessin de points pour chaque donnée filtrée
        filteredData.forEachIndexed { index, (_, value) ->
            val x = index * 60f // Espacement des points
            val y = value?.times(30) ?: 0f // Hauteur du point
            drawCircle(
                color = Color.Blue,
                radius = 4f,
                center = Offset(x, 200f - y) // Inverser y pour l'orientation
            )
        }
    }
}

package com.indicefossile.indiceapp.ui.components

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import coil.compose.rememberAsyncImagePainter
import com.google.android.material.color.MaterialColors
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
    val groupedProducts = groupProductsByDate(scannedProducts)
    val todayCO2 = getTodayCO2Total(scannedProducts)

    val today = Calendar.getInstance()
    val productsToday = scannedProducts.filter { product ->
        val productDate = Calendar.getInstance().apply { time = Date(product.timestamp) }
        productDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                productDate.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    }

    var showHistory by remember { mutableStateOf(true) }
    var selectedPeriod by remember { mutableStateOf("Semaine") }

    val co2Data = scannedProducts
        .filter { it.CO2_TOTAL != null }
        .map {
            Date(it.timestamp) to it.CO2_TOTAL!!.toFloat()
        }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
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
                    text = "CO₂ aujourd'hui : %.2f kg CO2e".format(todayCO2),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color(0xFF009900), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                )

                Text(
                    text = if (showHistory) "→ Voir les statistiques" else "← Voir l’historique",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth(Alignment.CenterHorizontally)
                        .clickable { showHistory = !showHistory }
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            if (showHistory) {
                if (scannedProducts.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxHeight()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Aucun produit scanné pour le moment.",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                } else {
                    groupedProducts.forEach { (dateTitle, productsOnDate) ->
                        item {
                            Text(
                                text = dateTitle,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .padding(top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(productsOnDate) { product ->
                            ScannedProductItem(product, onProductClick)
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "Statistiques",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Jour", "Semaine", "Mois", "Année").forEach { period ->
                            Button(
                                onClick = { selectedPeriod = period },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedPeriod == period)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Text(
                                    text = period,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Période sélectionnée: $selectedPeriod",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    SimpleLineChart(co2Data, selectedPeriod)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "CO₂ : —",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        "Énergie : —",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    Text(
                        "Note globale : —",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onScanClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006600)),
                border = BorderStroke(2.dp, Color.Black),
                modifier = Modifier
                    .defaultMinSize(minHeight = 0.dp, minWidth = 0.dp)
            ) {
                Text(
                    text = "Scanner",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            Button(
                onClick = onWebsiteClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006600)),
                border = BorderStroke(2.dp, Color.Black),
                modifier = Modifier
                    .defaultMinSize(minHeight = 0.dp, minWidth = 0.dp)
            ) {
                Text(
                    text = "Catalogue",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}


fun groupProductsByDate(products: List<ScannedProduct>): Map<String, List<ScannedProduct>> {
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    fun formatDate(date: Date): String {
        val cal = Calendar.getInstance().apply { time = date }
        return when {
            cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "AUJOURD'HUI"

            cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "HIER"

            else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
        }
    }

    return products
        .sortedByDescending { it.timestamp }
        .groupBy { formatDate(Date(it.timestamp)) }
}

@SuppressLint("DefaultLocale")
@Composable
fun ScannedProductItem(product: ScannedProduct, onProductClick: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onProductClick(product.barcode) }
            .border(1.dp, getEcoScoreColor(product.GreenScore), shape = RoundedCornerShape(8.dp)),
            color = Color.White,
        tonalElevation = 2.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(product.imageUrl),
                contentDescription = "Image du produit",
                modifier = Modifier
                    .size(80.dp)
                    .border(2.dp, Color.Black, shape = RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
                Text(
                    text = buildAnnotatedString {
                        val co2 = product.CO2_TOTAL
                        if (co2 != null) {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                append(String.format("%.2f", co2))
                            }
                            append(" kg Co2e")
                        } else {
                            append("— kg Co2e")
                        }
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = getEcoScoreColor(product.GreenScore)
                )

            }
        }
    }
}

fun getEcoScoreColor(ecoScore: String?): Color {
    return when (ecoScore?.lowercase()) {
        "a-plus" -> Color(0xFF166E25)
        "a" -> Color(0xFF80BD41)
        "b" -> Color(0xFFEBF13F)
        "c" -> Color(0xFFEECC3E)
        "d" -> Color(0xFFB74D4D)
        "e" -> Color(0xFFA12323)
        else -> Color(0xFFA12323) // couleur par défaut pour score inconnu
    }
}




@OptIn(UnstableApi::class) @Composable
fun SimpleLineChart(
    rawData: List<Pair<Date, Float?>>,
    period: String
) {
    fun groupDataByPeriod(data: List<Pair<Date, Float?>>): List<Pair<Any, Float?>> {

        return try {
            when (period) {
                "Jour" -> {
                    data.groupBy { SimpleDateFormat("dd", Locale.getDefault()).format(it.first) }
                        .map { Pair(it.key, it.value.mapNotNull { it.second }.sum()) }
                }
                "Semaine" -> {
                    data.groupBy { SimpleDateFormat("yyyy-ww", Locale.getDefault()).format(it.first) }
                        .map { Pair(it.key, it.value.mapNotNull { it.second }.sum()) }
                }
                "Mois" -> {
                    data.groupBy { SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(it.first) }
                        .map { Pair(it.key, it.value.mapNotNull { it.second }.sum()) }
                }
                "Année" -> {
                    data.groupBy { SimpleDateFormat("yyyy", Locale.getDefault()).format(it.first) }
                        .map { Pair(it.key, it.value.mapNotNull { it.second }.sum()) }
                }
                else -> data
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    val groupedData = groupDataByPeriod(rawData)

    groupedData.forEach { (key, value) ->
        Log.d("SimpleLineChart", "Période: $key, Valeur moyenne: $value")
    }

    rawData.forEach { (date, value) ->
        Log.d("SimpleLineChart", "Date brute: ${date}, CO2: $value")
    }


    if (groupedData.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aucune donnée à afficher", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    androidx.compose.foundation.Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(250.dp)
        .padding(16.dp)) {

        // Dessiner les axes et les valeurs en ordonnée
        drawLine(
            color = Color.Black,
            start = Offset(30f, 0f),
            end = Offset(30f, size.height),
            strokeWidth = 2f
        )

        drawLine(
            color = Color.Black,
            start = Offset(30f, size.height - 20f),
            end = Offset(size.width, size.height - 20f),
            strokeWidth = 2f
        )

        val maxValue = groupedData.maxOfOrNull { it.second ?: 0f } ?: 0f
        val step = maxValue / 5

        for (i in 0..5) {
            val yPosition = size.height - 20f - (i * (size.height - 40f) / 5)
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 16f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
                drawText(
                    "${(step * i).toInt()} kg CO₂",
                    20f, // Position X
                    yPosition, // Position Y
                    paint
                )
            }
        }

        groupedData.forEachIndexed { index, (periodKey, value) ->
            val x = index * (size.width / groupedData.size) + 40f
            val y = size.height - 20f - (value?.times((size.height - 40f) / maxValue) ?: 0f)
            drawCircle(
                color = Color.Blue,
                radius = 6f,
                center = Offset(x, y)
            )

            // Afficher les labels sous chaque point (jour, mois, année selon la période)
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 30f
                    textAlign = android.graphics.Paint.Align.CENTER // Centre le texte sous les points
                }
                val label = try {
                    when (period) {
                        "Jour" -> {
                            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(periodKey.toString())
                            SimpleDateFormat("dd/MM", Locale.getDefault()).format(date!!)
                        }
                        "Semaine" -> {
                            // Pour afficher par ex. "Semaine 18"
                            val parts = periodKey.toString().split("-")
                            if (parts.size == 2) "Sem. ${parts[1]}" else periodKey.toString()
                        }
                        "Mois" -> {
                            val date = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(periodKey.toString())
                            SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(date!!)
                        }
                        "Année" -> periodKey.toString()
                        else -> periodKey.toString()
                    }
                } catch (e: Exception) {
                    Log.e("SimpleLineChart", "Erreur de parsing label", e)
                    periodKey.toString()
                }



                drawText(
                    label,
                    x, // Position X
                    size.height - 5f, // Position Y (légèrement en dessous des points)
                    paint
                )
            }
        }
    }
}

fun getTodayCO2Total(products: List<ScannedProduct>): Double {
    val today = Calendar.getInstance()
    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val todayEnd = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    val todayProducts = products.filter {
        val productDate = Calendar.getInstance().apply { time = Date(it.timestamp) }
        productDate.after(todayStart) && productDate.before(todayEnd)
    }

    return todayProducts.sumOf { it.CO2_TOTAL ?: 0.0 }
}



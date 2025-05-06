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
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import coil.compose.rememberAsyncImagePainter
import com.google.android.material.color.MaterialColors
import com.indicefossile.indiceapp.R
import com.indicefossile.indiceapp.data.model.ScannedProduct
import com.indicefossile.indiceapp.ui.viewmodel.ScannedProductViewModel
import com.patrykandpatrick.vico.core.extension.sumOf
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

    var showHistory by remember { mutableStateOf(true) }
    var selectedPeriod by remember { mutableStateOf("Semaine") }
    var showPieChart by remember { mutableStateOf(false) }
    val co2ByGreenScore = calculateCO2ByGreenScore(scannedProducts)

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
                            modifier = Modifier.padding(0.dp, 150.dp)
                        ) {
                            Text(
                                "HISTORIQUE\nVIDE",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(125.dp, 0.dp, 0.dp, 0.dp)
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

                    Button(
                        onClick = { showPieChart = !showPieChart },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text(if (showPieChart) "Voir le graphique linéaire" else "Voir la répartition CO₂ par note")
                    }

                    if (showPieChart) {
                        PieChart(co2ByGreenScore)
                    } else {
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

                        Spacer(modifier = Modifier.height(16.dp))
                        SimpleLineChart(co2Data, selectedPeriod)

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
        else -> Color(0xFFA12323)
    }
}




@OptIn(UnstableApi::class)
@Composable
fun SimpleLineChart(
    rawData: List<Pair<Date, Float?>>,
    period: String
) {
    fun groupDataByPeriod(data: List<Pair<Date, Float?>>): List<Pair<Any, Float?>> {
        return try {
            when (period) {
                "Jour" -> {
                    data.groupBy { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(it.first) }
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

    if (groupedData.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize().padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Aucune donnée à afficher", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(horizontal = 8.dp, vertical = 16.dp)
    ) {
        val yAxisOffset = 70f
        val bottomOffset = 20f

        drawLine(
            color = Color.Black,
            start = Offset(yAxisOffset, 0f),
            end = Offset(yAxisOffset, size.height),
            strokeWidth = 2f
        )
        drawLine(
            color = Color.Black,
            start = Offset(yAxisOffset, size.height - bottomOffset),
            end = Offset(size.width, size.height - bottomOffset),
            strokeWidth = 2f
        )

        val maxValue = groupedData.maxOfOrNull { it.second ?: 0f } ?: 0f
        val step = maxValue / 5

        for (i in 0..5) {
            val yPosition = size.height - bottomOffset - (i * (size.height - 2 * bottomOffset) / 5)
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 20f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
                val decimalFormat = when {
                    maxValue < 1f -> "%.2f kg CO₂"
                    maxValue < 10f -> "%.1f kg CO₂"
                    else -> "%.0f kg CO₂"
                }
                drawText(
                    String.format(decimalFormat, step * i),
                    yAxisOffset - 5f,
                    yPosition,
                    paint
                )
            }
        }

        val spacing = (size.width - yAxisOffset - 20f) / (groupedData.size - 1).coerceAtLeast(1)
        val points = groupedData.mapIndexed { index, (_, value) ->
            val x = yAxisOffset + (index * spacing) + 50f
            val y = size.height - bottomOffset - (value?.times((size.height - 2 * bottomOffset) / maxValue) ?: 0f)
            Offset(x, y)
        }

        for (i in 0 until points.size - 1) {
            drawLine(
                color = Color.Blue,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 4f
            )
        }

        groupedData.forEachIndexed { index, (periodKey, _) ->
            val point = points[index]
            drawCircle(
                color = Color.Blue,
                radius = 10f,
                center = point
            )
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                val label = try {
                    when (period) {
                        "Jour" -> {
                            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(periodKey.toString())
                            SimpleDateFormat("dd/MM", Locale.getDefault()).format(date!!)
                        }
                        "Semaine" -> {
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
                    periodKey.toString()
                }
                drawText(
                    label,
                    point.x,
                    size.height + 20f,
                    paint
                )
            }
        }
    }
}


fun getTodayCO2Total(products: List<ScannedProduct>): Float {
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

    return todayProducts.sumOf { (it.CO2_TOTAL ?: 0.0).toFloat() }
}

fun calculateCO2ByGreenScore(products: List<ScannedProduct>): Map<String, Float> {
    return products
        .filter { it.CO2_TOTAL != null && it.GreenScore != null }
        .groupBy { it.GreenScore!!.lowercase() }
        .mapValues { (_, list) -> list.sumOf { it.CO2_TOTAL!!.toFloat() } }
}

@Composable
fun PieChart(data: Map<String, Float>) {
    val total = data.values.sum()
    val proportions = data.mapValues { it.value / total }

    val colors = mapOf(
        "a-plus" to Color(0xFF166E25),
        "a" to Color(0xFF80BD41),
        "b" to Color(0xFFEBF13F),
        "c" to Color(0xFFEECC3E),
        "d" to Color(0xFFB74D4D),
        "e" to Color(0xFFA12323)
    )

    val sortedData = proportions.toList().sortedByDescending { it.second }

    Canvas(modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
        .padding(16.dp)) {
        var startAngle = -90f
        sortedData.forEach { (score, proportion) ->
            val sweepAngle = 360 * proportion
            drawArc(
                color = colors[score] ?: Color.Gray,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true
            )
            startAngle += sweepAngle
        }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        sortedData.sortedBy { it.first.uppercase(Locale.ROOT) }.forEach { (score, proportion) ->
            val percent = "%.1f".format(proportion * 100)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(12.dp)
                        .background(colors[score] ?: Color.Gray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("${score.uppercase(Locale.ROOT)} : $percent%", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

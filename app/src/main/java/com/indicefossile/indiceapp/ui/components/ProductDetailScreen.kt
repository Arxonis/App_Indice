package com.indicefossile.indiceapp.ui.components

import android.annotation.SuppressLint
import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.indicefossile.indiceapp.R
import com.indicefossile.indiceapp.data.model.Product
import com.indicefossile.indiceapp.ui.utils.getProductImageUrl

// Fonctions d'aide pour mapper les scores aux images disponibles dans vos ressources.
@Composable
fun getNutriscoreImageResource(grade: String?): Int {
    return when (grade?.lowercase()) {
        "a" -> R.drawable.nutriscore_a
        "b" -> R.drawable.nutriscore_b
        "c" -> R.drawable.nutriscore_c
        "d" -> R.drawable.nutriscore_d
        "e" -> R.drawable.nutriscore_e
        "not-applicable" -> R.drawable.nutriscore_not_applicable
        else -> R.drawable.nutriscore_unknown
    }
}

@Composable
fun getEcoscoreImageResource(grade: String?): Int {
    return when (grade?.lowercase()) {
        "a" -> R.drawable.ecoscore_a
        "a+" -> R.drawable.ecoscore_a_plus
        "a-plus" -> R.drawable.ecoscore_a_plus
        "b" -> R.drawable.ecoscore_b
        "c" -> R.drawable.ecoscore_c
        "d" -> R.drawable.ecoscore_d
        "e" -> R.drawable.ecoscore_e
        "f" -> R.drawable.ecoscore_f
        "not-applicable" -> R.drawable.ecoscore_not_applicable
        else -> R.drawable.ecoscore_unknown
    }
}

@Composable
fun getNovaImageResource(novaTag: String?): Int {
    return when (novaTag) {
        "en:4-ultra-processed-food-and-drink-products" -> R.drawable.nova_4
        "en:3-ultra-processed-food-and-drink-products" -> R.drawable.nova_3
        "en:2-ultra-processed-food-and-drink-products" -> R.drawable.nova_2
        "en:1-ultra-processed-food-and-drink-products" -> R.drawable.nova_1
        "not-applicable" -> R.drawable.nova_not_applicable
        else -> R.drawable.nova_unknown
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailScreenWithTopBar(product: Product) {
    // Récupère l'activité pour fermer l'écran lors du clic sur le bouton retour
    val activity = LocalContext.current as? Activity
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Détails du produit") },
                navigationIcon = {
                    IconButton(onClick = { activity?.finish() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            ProductDetailScreen(product = product, modifier = Modifier.padding(paddingValues))
        }
    )
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ProductDetailScreen(product: Product, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Image du produit
            product.images?.front_fr?.let { frontImage ->
                val imageUrl = getProductImageUrl(
                    productBarcode = product.code,
                    imageType = "front_fr",
                    resolution = "400",
                    rev = frontImage.rev
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .clip(MaterialTheme.shapes.medium)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = imageUrl),
                        contentDescription = "Image du produit",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }


            // Titre du produit
            Text(
                text = product.product_name ?: "Nom non défini",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Carte Informations Générales (Marque, Quantité)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    product.brands?.let { brand ->
                        DetailItem(label = "Marque", value = brand)
                    }
                    product.quantity?.let { qty ->
                        DetailItem(label = "Quantité", value = qty)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))



            // Carte Détails CO₂ - Agribalyse (accès via ecoscore_data.agribalyse selon votre modèle)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Détails CO₂ (en kg)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    product.ecoscore_data?.agribalyse?.let { agr ->
                        DetailItem(label = "Total", value = agr.co2_total?.toString() ?: "?")
                    } ?: Text(
                        text = "Données Agribalyse non disponibles",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    product.ecoscore_data?.agribalyse?.let { agr ->
                        DetailItem(label = "Agriculture", value = agr.co2_agriculture?.toString() ?: "?")
                        DetailItem(label = "Consumption", value = agr.co2_consumption?.toString() ?: "?")
                        DetailItem(label = "Distribution", value = agr.co2_distribution?.toString() ?: "?")
                        DetailItem(label = "Packaging", value = agr.co2_packaging?.toString() ?: "?")
                        DetailItem(label = "Processing", value = agr.co2_processing?.toString() ?: "?")
                        DetailItem(label = "Transportation", value = agr.co2_transportation?.toString() ?: "?")
                    } ?: Text(
                        text = "Données Agribalyse non disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Carte Scores : affichage des icônes pour Nova, EcoScore et NutriScore
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Scores",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Nova Score
                        product.nova_groups_tags?.firstOrNull()?.let { novaTag ->
                            Icon(
                                painter = painterResource(id = getNovaImageResource(novaTag)),
                                contentDescription = "Nova Score",
                                modifier = Modifier.size(48.dp),
                                tint = Color.Unspecified

                            )
                        } ?: Icon(
                            painter = painterResource(id = R.drawable.nova_unknown),
                            contentDescription = "Nova Score inconnu",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                        // EcoScore
                        product.ecoscore_data?.grade?.let { ecoGrade ->
                            Icon(
                                painter = painterResource(id = getEcoscoreImageResource(ecoGrade)),
                                contentDescription = "EcoScore",
                                modifier = Modifier.size(48.dp),
                                tint = Color.Unspecified
                            )
                        } ?: Icon(
                            painter = painterResource(id = R.drawable.ecoscore_unknown),
                            contentDescription = "EcoScore inconnu",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                        // Nutriscore
                        product.nutrition_grades?.let { nutriGrade ->
                            Icon(
                                painter = painterResource(id = getNutriscoreImageResource(nutriGrade)),
                                contentDescription = "NutriScore",
                                modifier = Modifier.size(48.dp),
                                tint = Color.Unspecified
                            )
                        } ?: Icon(
                            painter = painterResource(id = R.drawable.nutriscore_unknown),
                            contentDescription = "NutriScore inconnu",
                            modifier = Modifier.size(48.dp),
                            tint = Color.Unspecified
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // Carte Détails NutriScore (déplacés sous les détails CO₂)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Détails NutriScore",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    product.nutriscore_data?.let { ns ->
                        DetailItem(
                            label = "Grade",
                            value = ns.grade?.uppercase() ?: "N/A"
                        )

                        DetailItem(label = "Score", value = ns.score?.toString() ?: "N/A")
                        DetailItem(label = "Énergie pts", value = ns.energy_points?.toString() ?: "N/A")
                        DetailItem(label = "Sucres (g)", value = ns.sugars_value?.toString() ?: "N/A")
                    } ?: Text(
                        text = "Données NutriScore non disponibles",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Carte Ingrédients
            product.ingredients_text?.takeIf { it.isNotBlank() }?.let { ingredients ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Ingrédients",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ingredients,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

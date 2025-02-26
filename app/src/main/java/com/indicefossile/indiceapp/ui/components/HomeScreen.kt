package com.indicefossile.indiceapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.indicefossile.indiceapp.R
import com.indicefossile.indiceapp.data.model.ScannedProduct
import com.indicefossile.indiceapp.ui.viewmodel.ScannedProductViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: ScannedProductViewModel, // Ajout du ViewModel
    onScanClick: () -> Unit,
    onWebsiteClick: () -> Unit,
) {
    val scannedProducts by viewModel.allProducts.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Permet le scroll si le contenu dépasse l'écran
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Logo de l'application
        Image(
            painter = painterResource(id = R.drawable.indice_logo),
            contentDescription = "Logo Indice Fossile",
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Titre de l'historique
        Text(
            text = "Produits scannés",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Affichage de la liste des produits scannés
        LazyColumn(
            modifier = Modifier.weight(1f) // Garde de la place pour les boutons en bas
        ) {
            items(scannedProducts) { product ->
                ScannedProductItem(product)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Boutons Scanner et Catalogue
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
                Text(text = "Scanner")
            }
            Button(
                onClick = onWebsiteClick,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(text = "Catalogue")
            }
        }
    }
}

@Composable
fun ScannedProductItem(product: ScannedProduct) {
    val Formated = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date(product.timestamp))
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Nom: ${product.name}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Code-barres: ${product.barcode}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Scanné le : $Formated", style = MaterialTheme.typography.bodySmall)
        }
    }
}

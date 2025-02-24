package com.indicefossile.indiceapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.indicefossile.indiceapp.R

@Composable
fun HomeScreen(
    onScanClick: () -> Unit,
    onWebsiteClick: () -> Unit,
) {

    // Contenu principal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // Permet le scroll si le contenu dépasse la hauteur de l'écran
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

        // Titre de la section des produits scannés
        Text(
            text = "Produits scannés",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))



        Spacer(modifier = Modifier.height(24.dp))

        // Boutons d'action (Scanner et Catalogue)
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

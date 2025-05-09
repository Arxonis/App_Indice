package com.indicefossile.indiceapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import com.indicefossile.indiceapp.R

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // Affiche le logo centré sur l'écran
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.indice_logo),
            contentDescription = "Logo Indice Fossile",
            modifier = Modifier.size(200.dp) // Ajustez la taille selon vos besoins
        )
    }

    // Après 2 secondes, on appelle onTimeout() pour passer à l'écran suivant
    LaunchedEffect(Unit) {
        delay(2000L)
        onTimeout()
    }
}

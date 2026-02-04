package com.example.illapus.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.illapus.R
import com.example.illapus.ui.navigation.AppDestinations
import com.example.illapus.ui.navigation.NavigationItems
import com.example.illapus.utils.TokenManager
import kotlinx.coroutines.delay

/**
 * Pantalla de inicio (Splash Screen) con animación de opacidad
 */
@Composable
fun SplashScreen(
    navController: NavController,
    onSplashFinished: () -> Unit = {
        // Verificar si el usuario ya está logueado
        if (TokenManager.isLoggedIn()) {
            // Si ya está logueado, ir directamente a la pantalla principal
            navController.navigate(NavigationItems.Activities.route) {
                popUpTo(AppDestinations.SPLASH_ROUTE) { inclusive = true }
            }
        } else {
            // Si no está logueado, ir al login
            navController.navigate(AppDestinations.LOGIN_ROUTE) {
                popUpTo(AppDestinations.SPLASH_ROUTE) { inclusive = true }
            }
        }
    }
) {
    // Iniciamos con opacidad completa para evitar la pantalla negra
    val alphaAnim = remember {
        Animatable(initialValue = 1f)
    }

    // Efecto para manejar la animación y navegación automática
    LaunchedEffect(key1 = true) {
        // Solo una ligera pulsación en la opacidad para dar efecto visual
        alphaAnim.animateTo(
            targetValue = 0.8f,
            animationSpec = tween(
                durationMillis = 400,
                easing = LinearEasing
            )
        )

        alphaAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 600,
                easing = LinearEasing
            )
        )

        // Espera mínima para que el usuario vea el logo
        delay(400)

        // Animación de salida con fade out
        alphaAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(
                durationMillis = 400,
                easing = LinearEasing
            )
        )

        // Navegar a la siguiente pantalla cuando termine la animación
        onSplashFinished()
    }

    // Diseño de la pantalla - fondo blanco para evitar la pantalla negra
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        // Logo con animación de opacidad
        Image(
            painter = painterResource(id = R.drawable.illapus_02),
            contentDescription = "Logo",
            modifier = Modifier
                .size(200.dp)
                .alpha(alphaAnim.value)
        )
    }
}

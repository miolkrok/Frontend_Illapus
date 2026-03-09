package com.example.illapus.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp

/**
 * Carrusel de imágenes con indicador de página
 * Soporta tanto URLs como imágenes Base64
 *
 * @param images Lista de URLs o cadenas Base64 de las imágenes
 * @param onPageChange Callback invocado cuando cambia la página
 * @param modifier Modificador opcional para personalizar el componente
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageCarousel(
    images: List<String>,
    onPageChange: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (images.isEmpty()) return

    android.util.Log.d("ImageCarousel", "Renderizando carrusel con ${images.size} imágenes: ${images.map { if (it.length > 50) it.take(50) + "..." else it }}")

    val pagerState = rememberPagerState(pageCount = { images.size })
    // ... resto igual

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            pageSpacing = 8.dp,
        ) { page ->
            // Usar AdaptiveImage directamente sin Card para bordes cuadrados
            AdaptiveImage(
                imageData = images[page],
                contentDescription = "Imagen ${page + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Contador de página
        Box(
            modifier = Modifier
                .padding(16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .align(Alignment.BottomEnd)
        ) {
            Text(
                text = "${pagerState.currentPage + 1}/${images.size}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Efecto cuando cambia la página
        LaunchedEffectWithLifecycle(pagerState) {
            snapshotFlow { pagerState.currentPage }.collect { page ->
                onPageChange(page)
            }
        }
    }
}

/**
 * Función utilitaria que aplica un LaunchedEffect pero respeta el ciclo de vida
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LaunchedEffectWithLifecycle(
    pagerState: PagerState,
    block: suspend () -> Unit
) {
    androidx.compose.runtime.LaunchedEffect(pagerState) {
        block()
    }
}

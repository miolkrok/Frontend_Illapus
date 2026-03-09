package com.example.illapus.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.illapus.R
import com.example.illapus.utils.ImageUtils

/**
 * Componente que puede mostrar imágenes desde URL o desde Base64
 */
@Composable
fun AdaptiveImage(
    imageData: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    when {
        imageData.isNullOrEmpty() -> {
            // Mostrar icono de imagen no disponible
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ImageNotSupported,
                    contentDescription = "Imagen no disponible",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        imageData.startsWith("http") -> {
            // Es una URL, usar AsyncImage de Coil
            AsyncImage(
                model = imageData,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale,
                onError = {
                    android.util.Log.e("AdaptiveImage", "Error cargando URL: $imageData", it.result.throwable)
                },
                onLoading = {
                    android.util.Log.d("AdaptiveImage", "Cargando URL: $imageData")
                },
                onSuccess = {
                    android.util.Log.d("AdaptiveImage", "Imagen cargada OK: $imageData")
                }
            )
        }
        else -> {
            // Es Base64, convertir a bitmap y mostrar
            val bitmap = remember(imageData) {
                ImageUtils.base64ToBitmap(imageData)
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = contentDescription,
                    modifier = modifier,
                    contentScale = contentScale
                )
            } else {
                // Error al decodificar Base64, mostrar icono de error
                Box(
                    modifier = modifier,
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ImageNotSupported,
                        contentDescription = "Error al cargar imagen",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

package com.example.illapus.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Componente que muestra una calificación en forma de estrellas
 *
 * @param rating Valor de la calificación (0-5)
 * @param maxRating Valor máximo de la calificación
 * @param starSize Tamaño de las estrellas
 * @param starColor Color de las estrellas
 * @param showRatingValue Si se debe mostrar el valor numérico junto a las estrellas
 * @param modifier Modificador para personalizar el componente
 */
@Composable
fun StarRatingBar(
    rating: Float,
    maxRating: Int = 5,
    starSize: Int = 24,
    starColor: Color = Color(0xFFFFC107),
    showRatingValue: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val fullStars = floor(rating).toInt()
        val hasHalfStar = (rating - fullStars) >= 0.5

        // Dibuja estrellas completas
        repeat(fullStars) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(starSize.dp),
                tint = starColor
            )
        }

        // Dibuja estrella media si corresponde
        if (hasHalfStar) {
            Icon(
                imageVector = Icons.Default.StarHalf,
                contentDescription = null,
                modifier = Modifier.size(starSize.dp),
                tint = starColor
            )
        }

        // Dibuja estrellas vacías para completar el total
        val emptyStars = maxRating - fullStars - if (hasHalfStar) 1 else 0
        repeat(emptyStars) {
            Icon(
                imageVector = Icons.Default.StarBorder,
                contentDescription = null,
                modifier = Modifier.size(starSize.dp),
                tint = starColor
            )
        }

        // Mostrar valor numérico si se solicita
        if (showRatingValue) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = rating.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

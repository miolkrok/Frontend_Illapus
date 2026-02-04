package com.example.illapus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.illapus.ui.theme.*

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    onPrimary = OnPrimary,
    secondary = BlueSecondary,
    onSecondary = OnSecondary,
    background = BluePrimary,
    surface = BluePrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    onPrimary = OnPrimary,
    secondary = BlueSecondary,
    onSecondary = OnSecondary,
    background = White,
    surface = White,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer
)

@Composable
fun IllapusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

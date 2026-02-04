package com.example.illapus.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat

/**
 * Comprueba si los permisos de ubicación están concedidos
 * @param context Contexto de la aplicación
 * @return Boolean indicando si los permisos están concedidos
 */
fun hasLocationPermissions(context: Context): Boolean {
    val fineLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    val coarseLocationPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    return fineLocationPermission == PackageManager.PERMISSION_GRANTED ||
           coarseLocationPermission == PackageManager.PERMISSION_GRANTED
}

/**
 * Composable que proporciona un launcher para solicitar permisos de ubicación
 * @return Par con función para solicitar permisos y booleano indicando si están concedidos
 */
@Composable
fun rememberLocationPermissionState(
    context: Context,
    onPermissionResult: (Boolean) -> Unit
): Pair<() -> Unit, Boolean> {
    val hasPermissions = remember(context) {
        hasLocationPermissions(context)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.any { it.value }
        onPermissionResult(granted)
    }

    val requestPermissions: () -> Unit = {
        launcher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    return Pair(requestPermissions, hasPermissions)
}

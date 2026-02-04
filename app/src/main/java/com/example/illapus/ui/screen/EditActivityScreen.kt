package com.example.illapus.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.illapus.ui.viewmodel.GenericViewModel

@Composable
fun EditActivityScreen(
    activityId: String,
    onBackPressed: () -> Unit,
    navController: NavController = rememberNavController(),
    viewModel: GenericViewModel = viewModel()
) {
    // Inicializar modo edición cuando se carga la pantalla
    LaunchedEffect(activityId) {
        val id = activityId.toIntOrNull()
        if (id != null) {
            viewModel.initEditMode(id)
        }
    }

    // Usar el GenericScreen existente, que ahora soporta modo edición
    GenericScreen(
        navController = navController,
        viewModel = viewModel
    )
}

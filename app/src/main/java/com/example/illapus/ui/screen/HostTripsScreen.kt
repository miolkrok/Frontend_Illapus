package com.example.illapus.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.illapus.ui.components.ActivityCard
import com.example.illapus.ui.navigation.AppDestinations
import com.example.illapus.ui.viewmodel.HostTripsViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun HostTripsScreen(
    modifier: Modifier = Modifier,
    viewModel: HostTripsViewModel = viewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(uiState.isLoading)
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar Snackbar cuando hay mensaje de éxito
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Encabezado
                Text(
                    text = "Mis Actividades",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                // Contenido principal con SwipeRefresh
                SwipeRefresh(
                    state = swipeRefreshState,
                    onRefresh = { viewModel.refresh() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    when {
                        uiState.isLoading && uiState.activities.isEmpty() -> {
                            // Mostrar indicador de carga solo si no hay datos previos
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        uiState.error != null && uiState.activities.isEmpty() -> {
                            // Mostrar mensaje de error solo si no hay datos previos
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = uiState.error ?: "Error desconocido",
                                        color = MaterialTheme.colorScheme.error,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { viewModel.loadHostActivities() }
                                    ) {
                                        Text("Reintentar")
                                    }
                                }
                            }
                        }
                        uiState.activities.isEmpty() && !uiState.isLoading -> {
                            // Mostrar mensaje cuando no hay actividades
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "No tienes actividades creadas",
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Crea tu primera actividad para comenzar",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                        else -> {
                            // Mostrar lista de actividades
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(uiState.activities) { activity ->
                                    ActivityCard(
                                        activity = activity,
                                        onClick = {
                                            // Al hacer clic en una actividad, navegar a la pantalla de detalles
                                            navController.navigate(
                                                AppDestinations.ACTIVITY_DETAILS_ROUTE.replace(
                                                    "{activityId}",
                                                    activity.id.toString()
                                                )
                                            )
                                        },
                                        onDelete = { activityId ->
                                            viewModel.deleteActivity(activityId)
                                        },
                                        onEdit = { activityId ->
                                            // Navegar a la pantalla de edición
                                            navController.navigate(
                                                AppDestinations.EDIT_ACTIVITY_ROUTE.replace(
                                                    "{activityId}",
                                                    activityId.toString()
                                                )
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.illapus.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.illapus.data.model.ReserveCreationResponse
import com.example.illapus.ui.components.ReservationCard
import com.example.illapus.ui.viewmodel.ActivityViewModel
import com.example.illapus.ui.viewmodel.ReservationsViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationsScreen(
    viewModel: ReservationsViewModel = viewModel(),
    activityViewModel: ActivityViewModel = viewModel(),
    onNavigateToComments: ((actividadId: Int) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = uiState.isLoading)
    val activityUiState by activityViewModel.uiState.collectAsState()

    // Crear mapa de títulos de actividades (actividadId -> título)
    val activityTitlesMap = remember(activityUiState.properties) {
        activityUiState.properties.associate { it.id to it.title }
    }

    // Mostrar Snackbar cuando hay mensaje de éxito
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccessMessage()
        }
    }

    // Mostrar Snackbar cuando hay error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
                .padding(paddingValues)
        ) {
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = { viewModel.loadReservations() }
            ) {
                when {
                    uiState.isLoading && uiState.reservations.isEmpty() -> {
                        LoadingContent()
                    }
                    uiState.reservations.isEmpty() -> {
                        EmptyContent()
                    }
                    else -> {
                        ReservationsList(
                            reservations = uiState.reservations,
                            onDeleteReservation = { reservationId ->
                                viewModel.deleteReservation(reservationId)
                            },
                            isDeletingReservation = uiState.isDeletingReservation,
                            onNavigateToComments = onNavigateToComments,
                            activityTitlesMap = activityTitlesMap
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Text(
                text = "Cargando reservas...",
                fontSize = 16.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Error",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
            Text(
                text = error,
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun EmptyContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = "Sin reservas",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Text(
                text = "No tienes reservas realizadas aún.",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ReservationsList(
    reservations: List<ReserveCreationResponse>,
    onDeleteReservation: (Int) -> Unit,
    isDeletingReservation: Int?,
    onNavigateToComments: ((Int) -> Unit)? = null,
    activityTitlesMap: Map<Int, String> = emptyMap()
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(reservations, key = { it.id }) { reservation ->
            val activityTitle = activityTitlesMap[reservation.actividadId] ?: "Actividad ${reservation.actividadId}"
            ReservationCard(
                reservation = reservation,
                activityTitle = activityTitle,
                onDeleteReservation = onDeleteReservation,
                isDeleting = isDeletingReservation == reservation.id,
                status = reservation.estado,
                onStatusSelected = {},
                onOpinar = { actividadId ->
                    onNavigateToComments?.invoke(actividadId)
                }
            ) {
                // StatusChip
                StatusChip(status = reservation.estado)

                // Detalles de la reserva
                ReservationDetailRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Fecha de actividad",
                    value = formatDate(reservation.fechaActividad)
                )

                ReservationDetailRow(
                    icon = Icons.Default.Group,
                    label = "Cantidad de personas",
                    value = "${reservation.cantidadPersonas} persona${if (reservation.cantidadPersonas > 1) "s" else ""}"
                )

                ReservationDetailRow(
                    icon = Icons.Default.MonetizationOn,
                    label = "Costo total",
                    value = "S/ ${String.format("%.2f", reservation.costoTotal)}"
                )

                ReservationDetailRow(
                    icon = Icons.Default.AccessTime,
                    label = "Fecha de reserva",
                    value = formatDateTime(reservation.fechaReserva)
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (backgroundColor, textColor) = when (status.uppercase()) {
        "PENDIENTE" -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.secondary
        "CONFIRMADA" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.tertiary
        "CANCELADA" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun ReservationDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val date = LocalDateTime.parse(dateString, inputFormatter)
        date.format(outputFormatter)
    } catch (e: Exception) {
        dateString.substring(0, 10).replace("-", "/")
    }
}

private fun formatDateTime(dateString: String): String {
    return try {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
        val date = LocalDateTime.parse(dateString, inputFormatter)
        date.format(outputFormatter)
    } catch (e: Exception) {
        try {
            val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
            val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            val date = LocalDateTime.parse(dateString, inputFormatter)
            date.format(outputFormatter)
        } catch (e2: Exception) {
            dateString.replace("T", " ").substring(0, 16)
        }
    }
}
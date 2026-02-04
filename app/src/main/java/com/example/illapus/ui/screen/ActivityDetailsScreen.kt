package com.example.illapus.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.illapus.ui.components.GenericMap
import com.example.illapus.ui.components.ImageCarousel
import com.example.illapus.ui.components.StarRatingBar
import com.example.illapus.ui.viewmodel.ActivityDetailsViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailsScreen(
    activityId: String,
    onBackPressed: () -> Unit,
    onReservationRequested: () -> Unit,
    viewModel: ActivityDetailsViewModel = viewModel()
) {
    // Cargamos los detalles de la actividad cuando se compone la pantalla
    LaunchedEffect(activityId) {
        viewModel.loadActivityDetails(activityId)
    }

    val activityDetails by viewModel.activityDetails.collectAsState()
    val currentImageIndex by viewModel.currentImageIndex.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isBottomSheetVisible by viewModel.isBottomSheetVisible.collectAsState()
    val guestCount by viewModel.guestCount.collectAsState()
    val totalPrice by viewModel.totalPrice.collectAsState()
    val selectedDate by viewModel.selectedReservationDate.collectAsState()
    val showDatePicker by viewModel.showDatePicker.collectAsState()
    val isCreatingReservation by viewModel.isCreatingReservation.collectAsState()
    val reservationResult by viewModel.reservationResult.collectAsState()

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar Snackbar para resultado de reserva
    LaunchedEffect(reservationResult) {
        reservationResult?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
            viewModel.clearReservationResult()
            // Navegar después de mostrar el mensaje
            kotlinx.coroutines.delay(1000)
            onReservationRequested()
        }
    }

    // Mostrar Snackbar para errores de reserva
    LaunchedEffect(error) {
        error?.let { errorMessage ->
            if (errorMessage.contains("reserva", ignoreCase = true)) {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Long
                )
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (error != null) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = error ?: "Error desconocido",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.retryLoad() }
                    ) {
                        Text("Reintentar")
                    }
                }
            } else {
                activityDetails?.let { details ->
                    // Contenido principal
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            // Carrusel de imágenes
                            ImageCarousel(
                                images = details.images,
                                onPageChange = { viewModel.updateCurrentImageIndex(it) }
                            )
                        }

                        // Información principal
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            // Título
                            Text(
                                text = details.title,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Descripción
                            Text(
                                text = details.description,
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Calificación
                            StarRatingBar(
                                rating = details.rating,
                                showRatingValue = true
                            )

                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            )

                            // Información del anfitrión
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("Anfitrión: ")
                                    }
                                    append(details.host.name)
                                },
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${details.host.yearsActive} años",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            )

                            // Información adicional de la actividad
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Tipo de Actividad",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = details.activityType,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Dificultad",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = details.difficultyLevel,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Duración y Disponibilidad
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Duración",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = details.duration,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Disponibilidad",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = details.availability,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Capacidad de personas
                            Text(
                                text = "Capacidad: ${details.minPeople} - ${details.maxPeople} personas",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )

                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            )

                            // Servicios incluidos
                            if (details.services.isNotEmpty()) {
                                Text(
                                    text = "Servicios Incluidos",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                details.services.forEach { service ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "• ",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = service,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                Divider(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp)
                                )
                            }

                            // Ubicaciones (Salida y Destino)
                            Text(
                                text = "Ubicaciones",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Información de ubicaciones
                            Column {
                                // Punto de salida
                                Text(
                                    text = "📍 Punto de Salida",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = details.departureLocation.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = details.departureLocation.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Punto de destino
                                Text(
                                    text = "🎯 Destino",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = details.destinationLocation.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = details.destinationLocation.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Mapa con ambas ubicaciones
                            GenericMap(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                departureLocation = LatLng(
                                    details.departureLocation.latitude,
                                    details.departureLocation.longitude
                                ),
                                destinationLocation = LatLng(
                                    details.destinationLocation.latitude,
                                    details.destinationLocation.longitude
                                ),
                                departureTitle = details.departureLocation.name,
                                destinationTitle = details.destinationLocation.name,
                                isInteractionEnabled = false,
                                showLocationInfo = false,
                                showCurrentLocationButton = false
                            )

                            // Espacio para que el footer no cubra contenido
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }

                    // Footer para reserva
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        shadowElevation = 8.dp,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "$${details.price} persona",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = details.availability,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textDecoration = TextDecoration.Underline
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.showReservationBottomSheet()
                                },
                                modifier = Modifier
                                    .height(48.dp)
                                    .wrapContentWidth()
                            ) {
                                Text(text = "Reservar")
                            }
                        }
                    }

                    // Botón de regreso flotante
                    FloatingActionButton(
                        onClick = onBackPressed,
                        modifier = Modifier
                            .padding(16.dp)
                            .size(48.dp)
                            .align(Alignment.TopStart),
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            }
        }
    }

    // BottomSheet para la selección de personas y fecha
    if (isBottomSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.hideReservationBottomSheet() },
            sheetState = bottomSheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Reservar Actividad",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Selección de número de personas
                Text(
                    text = "¿Cuántas personas?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Number Picker
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Botón para decrementar
                    IconButton(
                        onClick = { viewModel.decrementGuestCount() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        enabled = guestCount > (activityDetails?.minPeople ?: 1)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrementar",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Contador de personas
                    Text(
                        text = "$guestCount",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(48.dp),
                        textAlign = TextAlign.Center
                    )

                    // Botón para incrementar
                    IconButton(
                        onClick = { viewModel.incrementGuestCount() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        enabled = guestCount < (activityDetails?.maxPeople ?: 10)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Incrementar",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Selección de fecha
                Text(
                    text = "Fecha de la actividad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Botón para seleccionar fecha
                OutlinedCard(
                    onClick = { viewModel.toggleDatePicker() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedDate?.let {
                                it.format(DateTimeFormatter.ofPattern("dd 'de' MMMM, yyyy", Locale("es", "ES")))
                            } ?: "Seleccionar fecha",
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (selectedDate != null)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Calendario",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // DatePicker integrado usando DatePickerDialog
                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = selectedDate?.toEpochDay()?.times(24 * 60 * 60 * 1000)
                            ?: System.currentTimeMillis()
                    )

                    DatePickerDialog(
                        onDismissRequest = { viewModel.hideDatePicker() },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val date = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                                        viewModel.selectReservationDate(date)
                                    }
                                    viewModel.hideDatePicker()
                                }
                            ) {
                                Text("Confirmar")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { viewModel.hideDatePicker() }
                            ) {
                                Text("Cancelar")
                            }
                        }
                    ) {
                        DatePicker(
                            state = datePickerState,
                            showModeToggle = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Precio total
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Precio total:",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$${"%.2f".format(totalPrice)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.hideReservationBottomSheet() },
                        modifier = Modifier.weight(1f),
                        enabled = !isCreatingReservation
                    ) {
                        Text(text = "Cancelar")
                    }

                    Button(
                        onClick = { viewModel.createReservation() },
                        modifier = Modifier.weight(1f),
                        enabled = !isCreatingReservation && selectedDate != null
                    ) {
                        if (isCreatingReservation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(text = "Confirmar")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

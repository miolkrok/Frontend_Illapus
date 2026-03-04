package com.example.illapus.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.illapus.data.model.OpinionResponse
import com.example.illapus.ui.components.GenericMap
import com.example.illapus.ui.components.ImageCarousel
import com.example.illapus.ui.components.StarRatingBar
import com.example.illapus.ui.viewmodel.ActivityDetailsViewModel
import com.example.illapus.ui.viewmodel.OpinionViewModel
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
    onViewAllComments: ((String, String) -> Unit)? = null,
    viewModel: ActivityDetailsViewModel = viewModel(),
    opinionViewModel: OpinionViewModel = viewModel()
) {
    // Cargar detalles y opiniones
    LaunchedEffect(activityId) {
        viewModel.loadActivityDetails(activityId)
        val id = activityId.toIntOrNull()
        if (id != null) {
            opinionViewModel.loadOpiniones(id)
        }
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

    // Estado de opiniones
    val opinionUiState by opinionViewModel.uiState.collectAsState()

    // Estado para el flujo de pago con comprobante
    var createdReservationId by rememberSaveable { mutableStateOf<Int?>(null) }
    var showPaymentSheet by rememberSaveable { mutableStateOf(false) }
    var receiptUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var receiptName by rememberSaveable { mutableStateOf<String?>(null) }

    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        receiptUri = uri
        receiptName = uri?.lastPathSegment
    }

    val pickFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        receiptUri = uri
        receiptName = uri?.lastPathSegment
    }

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val paymentSheetState = rememberModalBottomSheetState(
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
                    Button(onClick = { viewModel.retryLoad() }) {
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
                            ImageCarousel(
                                images = details.images,
                                onPageChange = { viewModel.updateCurrentImageIndex(it) }
                            )
                        }

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

                            // Calificación con promedio real de opiniones
                            StarRatingBar(
                                rating = opinionUiState.promedioCalificacion.toFloat(),
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

                            // Ubicaciones
                            Text(
                                text = "Ubicaciones",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Column {
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

                            // Mapa
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

                            // ══════════════════════════════════════════════
                            // SECCIÓN DE OPINIONES (datos reales del backend)
                            // ══════════════════════════════════════════════
                            Divider(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            )

                            // Encabezado estilo Airbnb
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFF222222),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.2f", opinionUiState.promedioCalificacion),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF222222)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("·", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${opinionUiState.totalOpiniones} evaluaciones",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF222222)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (opinionUiState.isLoading) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            } else if (opinionUiState.opiniones.isEmpty()) {
                                Text(
                                    text = "Aún no hay opiniones sobre esta actividad",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                // Preview de las primeras 2 opiniones
                                opinionUiState.opiniones.take(2).forEach { opinion ->
                                    OpinionPreviewItem(opinion = opinion)
                                }
                            }

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
                                onClick = { viewModel.showReservationBottomSheet() },
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

                    Text(
                        text = "$guestCount",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(48.dp),
                        textAlign = TextAlign.Center
                    )

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

                // DatePicker
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
                        onClick = {
                            // Primero crea la reserva en el backend
                            // TODO: Reemplazar con la respuesta real del backend
                            // viewModel.createReservation() y obtener el ID de la respuesta
                            createdReservationId = 999 // Quemado por ahora

                            viewModel.hideReservationBottomSheet()
                            showPaymentSheet = true
                        },
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

    // BottomSheet para subir comprobante de pago
    if (showPaymentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaymentSheet = false },
            sheetState = paymentSheetState,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Comprobante de pago",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (receiptUri != null) {
                    Text(
                        text = "Archivo seleccionado:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = receiptName ?: "comprobante",
                        fontWeight = FontWeight.Medium
                    )
                    TextButton(onClick = {
                        receiptUri = null
                        receiptName = null
                    }) {
                        Text("Quitar archivo")
                    }
                } else {
                    Text(
                        text = "Sube una imagen o PDF del comprobante para finalizar la reserva.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { pickImageLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Imagen")
                    }

                    OutlinedButton(
                        onClick = { pickFileLauncher.launch(arrayOf("application/pdf", "image/*")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("PDF")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showPaymentSheet = false
                            receiptUri = null
                            receiptName = null
                            viewModel.showReservationBottomSheet()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Atrás")
                    }

                    Button(
                        onClick = {
                            if (receiptUri == null) {
                                scope.launch { snackbarHostState.showSnackbar("Adjunta el comprobante.") }
                                return@Button
                            }

                            if (createdReservationId == null) {
                                scope.launch { snackbarHostState.showSnackbar("No se encontró el ID de la reserva.") }
                                return@Button
                            }

                            // TODO: Aquí va la lógica para subir el comprobante al backend
                            // viewModel.uploadReceipt(createdReservationId!!, receiptUri!!)

                            scope.launch { snackbarHostState.showSnackbar("Reserva finalizada") }

                            showPaymentSheet = false
                            receiptUri = null
                            receiptName = null
                            createdReservationId = null
                        },
                        modifier = Modifier.weight(1f),
                        enabled = receiptUri != null
                    ) {
                        Text("Finalizar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ── Preview de opinión para ActivityDetailsScreen ──
@Composable
private fun OpinionPreviewItem(opinion: OpinionResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (opinion.nombreUsuario ?: "U").first().uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = opinion.nombreUsuario ?: "Usuario",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(5) { index ->
                Icon(
                    imageVector = if (index < (opinion.calificacion ?: 0)) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = if (index < (opinion.calificacion ?: 0)) Color(0xFF222222) else Color(0xFFDDDDDD),
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "· ${OpinionViewModel.formatTimeAgo(opinion.fechaCreacion)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = opinion.comentario ?: "",
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = Color(0xFFF0F0F0))
    }
}

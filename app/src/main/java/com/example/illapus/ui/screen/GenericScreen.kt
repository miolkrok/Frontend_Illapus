package com.example.illapus.ui.screen

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.illapus.data.model.ActivityCreationStep
import com.example.illapus.data.model.LocationData
import com.example.illapus.ui.components.GenericMap
import com.example.illapus.ui.viewmodel.GenericViewModel
import com.example.illapus.ui.viewmodel.MapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericScreen(
    navController: NavController = rememberNavController(),
    viewModel: GenericViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentStep by viewModel.currentStep.collectAsState()
    val activityData by viewModel.activityData.collectAsState()
    val localImages by viewModel.localImages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val error by viewModel.error.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    // Mostrar indicador de carga cuando está cargando datos para edición
    if (isLoading && isEditMode) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Cargando actividad...",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        return
    }

    // Manejar éxito
    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            navController.popBackStack()
        }
    }

    // Pantalla completa con estructura head, body, footer
    Scaffold(
        topBar = {
            // Head con botón "Guardar y salir"
            TopAppBar(
                title = {
                    Text(getStepTitle(currentStep))
                },
                actions = {
                    // Solo mostrar botón "Guardar" en el último paso
                    if (currentStep == ActivityCreationStep.SERVICES) {
                        Button(
                            onClick = {
                                viewModel.saveAndExit(context)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(end = 16.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Guardar",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Guardar")
                            }
                        }
                    } else {
                        // Botón "Salir" en otros pasos
                        TextButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Text("Salir")
                        }
                    }
                }
            )
        },
        bottomBar = {
            // Footer con botones "Atrás" y "Siguiente"
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Atrás
                    if (currentStep != ActivityCreationStep.BASIC_INFO) {
                        TextButton(
                            onClick = { viewModel.goBack() },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Atrás"
                            )
                            Text(
                                text = "Atrás",
                                textDecoration = TextDecoration.Underline
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    // Botón Siguiente
                    if (currentStep != ActivityCreationStep.SERVICES) {
                        val isStepComplete by remember {
                            derivedStateOf {
                                when (currentStep) {
                                    ActivityCreationStep.BASIC_INFO ->
                                        activityData.titulo.isNotBlank() && activityData.descripcion.isNotBlank()

                                    ActivityCreationStep.DEPARTURE_LOCATION ->
                                        activityData.ubicacionSalida.isNotBlank()

                                    ActivityCreationStep.DESTINATION_LOCATION ->
                                        activityData.ubicacionDestino.isNotBlank()

                                    ActivityCreationStep.ACTIVITY_TYPE ->
                                        activityData.tipoActividad.isNotBlank() && activityData.nivelDificultad.isNotBlank()

                                    ActivityCreationStep.DETAILS ->
                                        activityData.precio > 0 &&
                                                activityData.duracion.isNotBlank() &&
                                                activityData.maximoPersonas > 0 &&
                                                activityData.diasDisponibles.isNotEmpty() &&
                                                activityData.fechaInicioDisponible.isNotBlank() &&
                                                activityData.fechaFinDisponible.isNotBlank()

                                    ActivityCreationStep.GALLERY -> true
                                    ActivityCreationStep.SERVICES -> true
                                }
                            }
                        }

                        Button(
                            onClick = { viewModel.goToNext() },
                            enabled = isStepComplete,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Siguiente")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        // Body (contenido principal)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentStep) {
                ActivityCreationStep.BASIC_INFO -> BasicInfoContent(
                    activityData = activityData,
                    onUpdateBasicInfo = { titulo, descripcion ->
                        viewModel.updateBasicInfo(titulo, descripcion)
                    }
                )

                ActivityCreationStep.DEPARTURE_LOCATION -> LocationContent(
                    title = "Ubicación de Salida",
                    onLocationSelected = { location ->
                        viewModel.updateDepartureLocation(location)
                    }
                )

                ActivityCreationStep.DESTINATION_LOCATION -> LocationContent(
                    title = "Ubicación de Destino",
                    onLocationSelected = { location ->
                        viewModel.updateDestinationLocation(location)
                    }
                )

                ActivityCreationStep.ACTIVITY_TYPE -> ActivityTypeContent(
                    activityData = activityData,
                    onUpdateActivityType = { tipo, nivel ->
                        viewModel.updateActivityType(tipo, nivel)
                    }
                )

                ActivityCreationStep.DETAILS -> DetailsContent(
                    activityData = activityData,
                    onUpdateDetails = { precio, duracion, maxPersonas, minPersonas ->
                        viewModel.updateDetails(precio, duracion, maxPersonas, minPersonas)
                    },
                    onUpdateAvailability = { diasDisponibles, fechaInicio, fechaFin ->
                        viewModel.updateAvailability(diasDisponibles, fechaInicio, fechaFin)
                    },
                    onUpdateCuentaBancaria = { cuentaBancaria ->
                        viewModel.updateCuentaBancaria(cuentaBancaria)
                    }
                )

                ActivityCreationStep.GALLERY -> GalleryContent(
                    localImages = localImages,
                    onAddImage = { uri ->
                        viewModel.addGalleryImage(context, uri)
                    },
                    onRemoveImage = { index ->
                        viewModel.removeGalleryImage(index)
                    },
                    onSetMainImage = { index ->
                        viewModel.setMainImage(index)
                    }
                )

                ActivityCreationStep.SERVICES -> ServicesContent(
                    activityData = activityData,
                    onAddService = { servicio ->
                        viewModel.addService(servicio)
                    },
                    onRemoveService = { index ->
                        viewModel.removeService(index)
                    },
                    onSaveActivity = {
                        viewModel.saveActivityWithImages(context)
                    }
                )
            }
        }
    }
}

@Composable
fun BasicInfoContent(
    activityData: com.example.illapus.ui.viewmodel.ActivityData,
    onUpdateBasicInfo: (String, String) -> Unit
) {
    // Usar los valores directamente del activityData, no remember
    var titulo by remember(activityData.titulo) { mutableStateOf(activityData.titulo) }
    var descripcion by remember(activityData.descripcion) { mutableStateOf(activityData.descripcion) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Completa los datos básicos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            OutlinedTextField(
                value = titulo,
                onValueChange = {
                    titulo = it
                    onUpdateBasicInfo(titulo, descripcion)
                },
                label = { Text("Título de la actividad") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = descripcion,
                onValueChange = {
                    descripcion = it
                    onUpdateBasicInfo(titulo, descripcion)
                },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )
        }
    }
}

@Composable
fun LocationContent(
    title: String,
    onLocationSelected: (LocationData) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Título con padding adecuado
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(16.dp)
                .padding(top = 8.dp)
        )

        // Mapa que ocupa el resto del espacio disponible
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            GenericMap(
                modifier = Modifier.fillMaxSize(),
                onLocationSelected = { locationModel ->
                    onLocationSelected(
                        LocationData(
                            latitude = locationModel.latitude,
                            longitude = locationModel.longitude,
                            name = locationModel.name ?: "",
                            address = locationModel.address ?: ""
                        )
                    )
                },
                isInteractionEnabled = true,
                showLocationInfo = true,
                showCurrentLocationButton = true,
                showAddressSearchBar = true
            )
        }
    }
}

@Composable
fun ActivityTypeContent(
    activityData: com.example.illapus.ui.viewmodel.ActivityData,
    onUpdateActivityType: (String, String) -> Unit
) {
    var selectedTipo by remember { mutableStateOf(activityData.tipoActividad) }
    var selectedNivel by remember { mutableStateOf(activityData.nivelDificultad) }

    val tiposActividad = listOf("Deporte", "Cultura", "Aventura", "Gastronomía", "Naturaleza")
    val nivelesDificultad = listOf("Baja", "Media", "Alta")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Tipo de Actividad",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Text("Selecciona el tipo:", style = MaterialTheme.typography.titleMedium)
            tiposActividad.forEach { tipo ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedTipo == tipo,
                        onClick = {
                            selectedTipo = tipo
                            onUpdateActivityType(selectedTipo, selectedNivel)
                        }
                    )
                    Text(tipo)
                }
            }
        }

        item {
            Text("Nivel de Dificultad:", style = MaterialTheme.typography.titleMedium)
            nivelesDificultad.forEach { nivel ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    RadioButton(
                        selected = selectedNivel == nivel,
                        onClick = {
                            selectedNivel = nivel
                            onUpdateActivityType(selectedTipo, selectedNivel)
                        }
                    )
                    Text(nivel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsContent(
    activityData: com.example.illapus.ui.viewmodel.ActivityData,
    onUpdateDetails: (Double, String, Int, Int) -> Unit,
    onUpdateAvailability: (List<String>, String, String) -> Unit,
    onUpdateCuentaBancaria: (String) -> Unit
) {
    var precio by remember { mutableStateOf(activityData.precio.toString()) }
    var duracion by remember { mutableStateOf(activityData.duracion) }
    var maxPersonas by remember { mutableStateOf(activityData.maximoPersonas.toString()) }
    var minPersonas by remember { mutableStateOf(activityData.minimoPersonas.toString()) }
    var cuentaBancaria by remember { mutableStateOf(activityData.cuentaBancaria) }

    // Estados para los días de la semana
    val diasDeLaSemana =
        listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo")
    var diasSeleccionados by remember { mutableStateOf(activityData.diasDisponibles) }
    var fechaInicio by remember { mutableStateOf(activityData.fechaInicioDisponible) }
    var fechaFin by remember { mutableStateOf(activityData.fechaFinDisponible) }

    // Estados para los Date Pickers
    var showDatePickerInicio by remember { mutableStateOf(false) }
    var showDatePickerFin by remember { mutableStateOf(false) }
    val datePickerStateInicio = rememberDatePickerState()
    val datePickerStateFin = rememberDatePickerState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Detalles de la Actividad",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            OutlinedTextField(
                value = precio,
                onValueChange = {
                    precio = it
                    val precioDouble = precio.toDoubleOrNull() ?: 0.0
                    val maxPersonasInt = maxPersonas.toIntOrNull() ?: 1
                    val minPersonasInt = minPersonas.toIntOrNull() ?: 1
                    onUpdateDetails(precioDouble, duracion, maxPersonasInt, minPersonasInt)
                },
                label = { Text("Precio") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = duracion,
                onValueChange = {
                    duracion = it
                    val precioDouble = precio.toDoubleOrNull() ?: 0.0
                    val maxPersonasInt = maxPersonas.toIntOrNull() ?: 1
                    val minPersonasInt = minPersonas.toIntOrNull() ?: 1
                    onUpdateDetails(precioDouble, duracion, maxPersonasInt, minPersonasInt)
                },
                label = { Text("Duración (ej: 3h)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = maxPersonas,
                onValueChange = {
                    maxPersonas = it
                    val precioDouble = precio.toDoubleOrNull() ?: 0.0
                    val maxPersonasInt = maxPersonas.toIntOrNull() ?: 1
                    val minPersonasInt = minPersonas.toIntOrNull() ?: 1
                    onUpdateDetails(precioDouble, duracion, maxPersonasInt, minPersonasInt)
                },
                label = { Text("Máximo de personas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = minPersonas,
                onValueChange = {
                    minPersonas = it
                    val precioDouble = precio.toDoubleOrNull() ?: 0.0
                    val maxPersonasInt = maxPersonas.toIntOrNull() ?: 1
                    val minPersonasInt = minPersonas.toIntOrNull() ?: 1
                    onUpdateDetails(precioDouble, duracion, maxPersonasInt, minPersonasInt)
                },
                label = { Text("Mínimo de personas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        // Sección de disponibilidad
        item {
            Text(
                text = "Disponibilidad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        item {
            Text("Días disponibles:", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                diasDeLaSemana.chunked(2).forEach { chunk ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chunk.forEach { dia ->
                            val isSelected = diasSeleccionados.contains(dia)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    diasSeleccionados = if (isSelected) {
                                        diasSeleccionados - dia
                                    } else {
                                        diasSeleccionados + dia
                                    }
                                    onUpdateAvailability(diasSeleccionados, fechaInicio, fechaFin)
                                },
                                label = { Text(dia) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (chunk.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Date Picker para fecha de inicio
        item {
            Text("Fecha de inicio disponible:", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = fechaInicio,
                onValueChange = { },
                readOnly = true,
                label = { Text("Fecha de inicio") },
                placeholder = { Text("Selecciona fecha de inicio") },
                trailingIcon = {
                    IconButton(onClick = { showDatePickerInicio = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Date Picker para fecha de fin
        item {
            Text("Fecha de fin disponible:", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = fechaFin,
                onValueChange = { },
                readOnly = true,
                label = { Text("Fecha de fin") },
                placeholder = { Text("Selecciona fecha de fin") },
                trailingIcon = {
                    IconButton(onClick = { showDatePickerFin = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // SECCIÓN DE PAGOS - NUEVO
        item {
            Text(
                text = "Información de Pago",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        item {
            // Variables para campos separados - parsear del string guardado
            var banco by remember {
                mutableStateOf(
                    cuentaBancaria.lines().find { it.startsWith("Banco:") }
                        ?.removePrefix("Banco:")?.trim() ?: ""
                )
            }
            var nombreTitular by remember {
                mutableStateOf(
                    cuentaBancaria.lines().find { it.startsWith("Titular:") }
                        ?.removePrefix("Titular:")?.trim() ?: ""
                )
            }
            var cedula by remember {
                mutableStateOf(
                    cuentaBancaria.lines().find { it.startsWith("Cédula:") }
                        ?.removePrefix("Cédula:")?.trim() ?: ""
                )
            }
            var numeroCuenta by remember {
                mutableStateOf(
                    cuentaBancaria.lines().find { it.startsWith("Cuenta:") }
                        ?.removePrefix("Cuenta:")?.trim() ?: ""
                )
            }

            // Función para concatenar y guardar
            fun actualizarCuentaBancaria() {
                val datos = buildString {
                    if (banco.isNotBlank()) appendLine("Banco: $banco")
                    if (nombreTitular.isNotBlank()) appendLine("Titular: $nombreTitular")
                    if (cedula.isNotBlank()) appendLine("Cédula: $cedula")
                    if (numeroCuenta.isNotBlank()) appendLine("Cuenta: $numeroCuenta")
                }.trim()
                cuentaBancaria = datos
                onUpdateCuentaBancaria(datos)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Datos para recibir pagos",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )

                    // Banco
                    OutlinedTextField(
                        value = banco,
                        onValueChange = {
                            banco = it
                            actualizarCuentaBancaria()
                        },
                        label = { Text("Banco") },
                        placeholder = { Text("Ej: Banco Pichincha") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Nombre del titular
                    OutlinedTextField(
                        value = nombreTitular,
                        onValueChange = {
                            nombreTitular = it
                            actualizarCuentaBancaria()
                        },
                        label = { Text("Nombre del titular") },
                        placeholder = { Text("Ej: Juan Pérez") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Cédula
                    OutlinedTextField(
                        value = cedula,
                        onValueChange = {
                            cedula = it
                            actualizarCuentaBancaria()
                        },
                        label = { Text("Cédula / RUC") },
                        placeholder = { Text("Ej: 1712345678") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    // Número de cuenta
                    OutlinedTextField(
                        value = numeroCuenta,
                        onValueChange = {
                            numeroCuenta = it
                            actualizarCuentaBancaria()
                        },
                        label = { Text("Número de cuenta") },
                        placeholder = { Text("Ej: 2201234567") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    Text(
                        text = "Los clientes verán estos datos para realizar el depósito",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Date Picker Dialog para fecha de inicio
    if (showDatePickerInicio) {
        DatePickerDialog(
            onDateSelected = { selectedDate ->
                fechaInicio = selectedDate
                onUpdateAvailability(diasSeleccionados, fechaInicio, fechaFin)
                showDatePickerInicio = false
            },
            onDismiss = { showDatePickerInicio = false }
        )
    }

    // Date Picker Dialog para fecha de fin
    if (showDatePickerFin) {
        DatePickerDialog(
            onDateSelected = { selectedDate ->
                fechaFin = selectedDate
                onUpdateAvailability(diasSeleccionados, fechaInicio, fechaFin)
                showDatePickerFin = false
            },
            onDismiss = { showDatePickerFin = false }
        )
    }
}

@Composable
fun GalleryContent(
    localImages: List<com.example.illapus.data.model.LocalImageData>,
    onAddImage: (Uri) -> Unit,
    onRemoveImage: (Int) -> Unit,
    onSetMainImage: (Int) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onAddImage(it) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Galería de Fotos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Text(
                text = "La primera imagen será la principal automáticamente. Puedes tocar la estrella para cambiar cuál es la imagen principal.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Button(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar Foto")
            }
        }

        if (localImages.isNotEmpty()) {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(localImages) { index, imageData ->
                        Card(
                            modifier = Modifier.size(120.dp)
                        ) {
                            Box {
                                AsyncImage(
                                    model = Uri.parse(imageData.uri),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Indicador de imagen principal
                                IconButton(
                                    onClick = { onSetMainImage(index) },
                                    modifier = Modifier.align(Alignment.TopStart)
                                ) {
                                    Icon(
                                        imageVector = if (imageData.esImagenPrincipal) Icons.Filled.Star else Icons.Outlined.Star,
                                        contentDescription = if (imageData.esImagenPrincipal) "Imagen principal" else "Hacer principal",
                                        tint = if (imageData.esImagenPrincipal) Color.Yellow else Color.White
                                    )
                                }

                                // Botón eliminar
                                IconButton(
                                    onClick = { onRemoveImage(index) },
                                    modifier = Modifier.align(Alignment.TopEnd)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Eliminar",
                                        tint = Color.White
                                    )
                                }

                                // Texto indicativo si es imagen principal
                                if (imageData.esImagenPrincipal) {
                                    Card(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(4.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primary.copy(
                                                alpha = 0.8f
                                            )
                                        )
                                    ) {
                                        Text(
                                            text = "Principal",
                                            modifier = Modifier.padding(
                                                horizontal = 8.dp,
                                                vertical = 2.dp
                                            ),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary
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
}

@Composable
fun ServicesContent(
    activityData: com.example.illapus.ui.viewmodel.ActivityData,
    onAddService: (String) -> Unit,
    onRemoveService: (Int) -> Unit,
    onSaveActivity: () -> Unit
) {
    var nuevoServicio by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Servicios Incluidos",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Text(
                text = "Agrega los servicios que incluye tu actividad (opcional).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = nuevoServicio,
                    onValueChange = { nuevoServicio = it },
                    label = { Text("Nuevo servicio") },
                    placeholder = { Text("Ej: Guía, Transporte, Alimentación") },
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        if (nuevoServicio.isNotBlank()) {
                            onAddService(nuevoServicio)
                            nuevoServicio = ""
                        }
                    },
                    enabled = nuevoServicio.isNotBlank()
                ) {
                    Text("Agregar")
                }
            }
        }

        itemsIndexed(activityData.servicioEvento) { index, servicio ->
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = servicio.listaServicio,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { onRemoveService(index) }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "¡Listo para publicar!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Has completado todos los pasos. Puedes usar el botón 'Guardar' en la parte superior para publicar tu actividad.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// Función auxiliar para obtener el título del paso
fun getStepTitle(step: ActivityCreationStep): String {
    return when (step) {
        ActivityCreationStep.BASIC_INFO -> "Información Básica"
        ActivityCreationStep.DEPARTURE_LOCATION -> "Ubicación de Salida"
        ActivityCreationStep.DESTINATION_LOCATION -> "Ubicación de Destino"
        ActivityCreationStep.ACTIVITY_TYPE -> "Tipo de Actividad"
        ActivityCreationStep.DETAILS -> "Detalles"
        ActivityCreationStep.GALLERY -> "Galería"
        ActivityCreationStep.SERVICES -> "Servicios"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val date = java.time.Instant.ofEpochMilli(millis)
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate()
                    onDateSelected(date.toString())
                }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

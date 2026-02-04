package com.example.illapus.ui.components

import android.location.Geocoder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.illapus.data.model.LocationModel
import com.example.illapus.ui.viewmodel.MapViewModel
import com.example.illapus.utils.rememberLocationPermissionState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import java.util.*

/**
 * Componente genérico de mapa que permite mostrar ubicaciones duales o una sola ubicación
 *
 * @param modifier Modificador para personalizar el componente
 * @param initialLocation Ubicación inicial para centrar el mapa (opcional)
 * @param departureLocation Ubicación de salida para actividades (opcional)
 * @param destinationLocation Ubicación de destino para actividades (opcional)
 * @param departureTitle Título para el marcador de salida
 * @param destinationTitle Título para el marcador de destino
 * @param onLocationSelected Callback que se llama cuando se selecciona una ubicación
 * @param viewModel ViewModel para manejar la lógica del mapa (opcional)
 * @param mapStyleOptions Opciones de estilo para el mapa (opcional)
 * @param showCurrentLocationButton Si se debe mostrar el botón para ir a la ubicación actual
 * @param markerTitle Título para el marcador único (opcional)
 * @param markerSnippet Descripción para el marcador único (opcional)
 * @param isInteractionEnabled Si se permite interacción con el mapa para seleccionar ubicación
 * @param showLocationInfo Si se debe mostrar la información de la ubicación seleccionada
 * @param showAddressSearchBar Si se debe mostrar la barra de búsqueda por dirección
 * @param contentHeight Altura del contenido del mapa
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GenericMap(
    modifier: Modifier = Modifier,
    initialLocation: LatLng? = null,
    departureLocation: LatLng? = null,
    destinationLocation: LatLng? = null,
    departureTitle: String = "Punto de Salida",
    destinationTitle: String = "Destino",
    onLocationSelected: ((LocationModel) -> Unit)? = null,
    viewModel: MapViewModel = viewModel(),
    mapStyleOptions: MapStyleOptions? = null,
    showCurrentLocationButton: Boolean = true,
    markerTitle: String? = null,
    markerSnippet: String? = null,
    isInteractionEnabled: Boolean = true,
    showLocationInfo: Boolean = true,
    showAddressSearchBar: Boolean = false,
    contentHeight: Dp = 400.dp
) {
    val context = LocalContext.current
    val geocoder = remember { Geocoder(context, Locale.getDefault()) }
    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = showCurrentLocationButton && isInteractionEnabled,
            mapToolbarEnabled = false,
            scrollGesturesEnabled = isInteractionEnabled,
            zoomGesturesEnabled = isInteractionEnabled,
            rotationGesturesEnabled = isInteractionEnabled
        )
    }

    // Gestión de permisos
    val (requestPermissions, hasPermissions) = rememberLocationPermissionState(context) { granted ->
        if (granted && isInteractionEnabled) {
            viewModel.getCurrentLocation(context)
        }
    }

    val properties = remember(hasPermissions) {
        MapProperties(
            isMyLocationEnabled = hasPermissions,
            mapStyleOptions = mapStyleOptions
        )
    }

    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by remember { mutableStateOf(viewModel.isLoading) }

    // Si se proporciona una ubicación inicial, la usamos; de lo contrario, usamos la ubicación por defecto
    val defaultLocation = initialLocation ?: LatLng(4.570868, -74.297333) // Bogotá, Colombia como default

    // Si hay una ubicación inicial, establecemos la ubicación seleccionada
    LaunchedEffect(initialLocation) {
        initialLocation?.let {
            viewModel.updateSelectedLocation(it)
            // Solo obtenemos la dirección si estamos mostrando la información de ubicación
            if (showLocationInfo) {
                viewModel.getAddressFromLocation(geocoder, it)
            }
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        when {
            // Si hay ubicaciones duales, centrar entre ambas
            departureLocation != null && destinationLocation != null -> {
                val centerLat = (departureLocation.latitude + destinationLocation.latitude) / 2
                val centerLng = (departureLocation.longitude + destinationLocation.longitude) / 2
                position = CameraPosition.fromLatLngZoom(LatLng(centerLat, centerLng), 10f)
            }
            // Si hay una ubicación inicial, usarla
            initialLocation != null -> {
                position = CameraPosition.fromLatLngZoom(initialLocation, 15f)
            }
            // Si hay ubicación actual, usarla
            else -> {
                position = CameraPosition.fromLatLngZoom(defaultLocation, 10f)
            }
        }
    }

    // Observar cambios en la posición de la cámara desde el ViewModel
    val cameraPosition by viewModel.cameraPosition.collectAsState()
    LaunchedEffect(cameraPosition) {
        cameraPosition?.let {
            cameraPositionState.position = it
        }
    }

    // Si no tenemos permisos pero queremos mostrar el botón de ubicación, solicitamos permisos
    LaunchedEffect(showCurrentLocationButton, hasPermissions) {
        if (showCurrentLocationButton && !hasPermissions && isInteractionEnabled) {
            requestPermissions()
        }
    }

    // Efecto para actualizar la ubicación cuando cambia la cámara (solo si la interacción está habilitada)
    if (isInteractionEnabled) {
        LaunchedEffect(cameraPositionState.isMoving) {
            if (!cameraPositionState.isMoving) {
                val position = cameraPositionState.position.target
                viewModel.updateSelectedLocation(position)
                if (showLocationInfo) {
                    viewModel.getAddressFromLocation(geocoder, position)
                }
            }
        }
    }

    // Efecto para notificar cuando se selecciona una ubicación
    LaunchedEffect(selectedLocation) {
        selectedLocation?.let { location ->
            onLocationSelected?.invoke(location)
        }
    }

    Column(modifier = modifier) {
        // Barra de búsqueda de dirección (solo si showAddressSearchBar es true)
        if (showAddressSearchBar) {
            var addressQuery by remember { mutableStateOf("") }
            val keyboardController = LocalSoftwareKeyboardController.current

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 66.dp, top = 10.dp)
                        .align(Alignment.TopCenter),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TextField(
                        value = addressQuery,
                        onValueChange = { addressQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        placeholder = {
                            Text("Buscar dirección")
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            if (addressQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        addressQuery = ""
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Borrar"
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search,
                            keyboardType = KeyboardType.Text
                        ),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                if (addressQuery.isNotEmpty()) {
                                    viewModel.searchLocationByAddress(context, addressQuery)
                                    keyboardController?.hide()
                                }
                            }
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        ),
                        singleLine = true
                    )

                    // Botón de búsqueda que aparece cuando hay texto
                    if (addressQuery.isNotEmpty()) {
                        Button(
                            onClick = {
                                if (addressQuery.isNotEmpty()) {
                                    viewModel.searchLocationByAddress(context, addressQuery)
                                    keyboardController?.hide()
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Buscar",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Buscar")
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(contentHeight)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapStyleOptions = mapStyleOptions,
                    isMyLocationEnabled = hasPermissions
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false,
                    scrollGesturesEnabled = isInteractionEnabled,
                    zoomGesturesEnabled = isInteractionEnabled,
                    tiltGesturesEnabled = isInteractionEnabled,
                    rotationGesturesEnabled = isInteractionEnabled
                ),
                onMapClick = if (isInteractionEnabled) { latLng ->
                    viewModel.updateSelectedLocation(latLng)
                } else null
            ) {
                // Marcador para ubicación única (funcionalidad original)
                if (departureLocation == null && destinationLocation == null) {
                    // Si hay una ubicación inicial, usarla
                    initialLocation?.let { location ->
                        Marker(
                            state = MarkerState(position = location),
                            title = markerTitle ?: "Ubicación seleccionada",
                            snippet = markerSnippet,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                    }

                    // Si hay una ubicación seleccionada por el usuario, mostrarla también
                    selectedLocation?.let { location ->
                        val latLng = LatLng(location.latitude, location.longitude)
                        Marker(
                            state = MarkerState(position = latLng),
                            title = location.name.ifEmpty { "Ubicación seleccionada" },
                            snippet = location.address,
                            icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)
                        )
                    }
                }

                // Marcadores para ubicaciones duales (nueva funcionalidad)
                departureLocation?.let { departure ->
                    Marker(
                        state = MarkerState(position = departure),
                        title = "📍 $departureTitle",
                        snippet = "Punto de salida",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)
                    )
                }

                destinationLocation?.let { destination ->
                    Marker(
                        state = MarkerState(position = destination),
                        title = "🎯 $destinationTitle",
                        snippet = "Destino",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                    )
                }
            }

            // Indicador central para ayudar a ubicar el punto exacto (solo visible si la interacción está habilitada)
            if (isInteractionEnabled) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                        .align(Alignment.Center)
                )
            }

            // Botones flotantes para funcionalidades del mapa
            if (isInteractionEnabled) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón para obtener ubicación actual
                    if (showCurrentLocationButton) {
                        FloatingActionButton(
                            onClick = {
                                if (hasPermissions) {
                                    viewModel.getCurrentLocation(context)
                                } else {
                                    requestPermissions()
                                }
                            },
                            modifier = Modifier.size(48.dp),
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Mi ubicación"
                            )
                        }
                    }
                }
            }

            // Área de información (solo visible si showLocationInfo es true)
            if (showLocationInfo) {
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                            .padding(16.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                } else {
                    selectedLocation?.let { location ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                            shadowElevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = location.name.ifEmpty { "Ubicación seleccionada" },
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }

                                Text(
                                    text = location.address,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // Mostrar mensajes de error
            errorMessage?.let { error ->
                Snackbar(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("OK")
                        }
                    }
                ) {
                    Text(text = error)
                }
            }
        }
    }
}

package com.example.illapus.ui.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.illapus.data.model.LocationModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.model.Place
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Locale

/**
 * ViewModel para el manejo de la lógica del mapa
 */
class MapViewModel : ViewModel() {

    // Estado para la ubicación seleccionada actualmente
    private val _selectedLocation = MutableStateFlow<LocationModel?>(null)
    val selectedLocation: StateFlow<LocationModel?> = _selectedLocation.asStateFlow()

    // Estado para la posición de la cámara
    private val _cameraPosition = MutableStateFlow<CameraPosition?>(null)
    val cameraPosition: StateFlow<CameraPosition?> = _cameraPosition.asStateFlow()

    // Estado para mostrar si está cargando
    var isLoading by mutableStateOf(false)
        private set

    // Estado para manejar errores
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Actualiza la ubicación seleccionada
     * @param latLng Coordenadas LatLng de la ubicación
     */
    fun updateSelectedLocation(latLng: LatLng) {
        val location = LocationModel(
            latitude = latLng.latitude,
            longitude = latLng.longitude
        )
        _selectedLocation.value = location
    }

    /**
     * Establece la posición de la cámara
     * @param position Nueva posición de la cámara
     */
    fun setCameraPosition(position: CameraPosition) {
        _cameraPosition.value = position
    }

    /**
     * Obtiene la dirección a partir de las coordenadas usando Geocoder
     * @param geocoder Instancia de Geocoder para hacer la consulta
     * @param latLng Coordenadas para las que buscar la dirección
     */
    fun getAddressFromLocation(geocoder: Geocoder, latLng: LatLng) {
        viewModelScope.launch {
            try {
                isLoading = true
                val location = _selectedLocation.value ?: LocationModel(
                    latitude = latLng.latitude,
                    longitude = latLng.longitude
                )

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]
                            val addressText = address.getAddressLine(0) ?: ""
                            val locationName = address.featureName ?: ""

                            _selectedLocation.value = location.copy(
                                address = addressText,
                                name = locationName
                            )
                        }
                        isLoading = false
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocation(
                        latLng.latitude,
                        latLng.longitude,
                        1
                    )
                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val addressText = address.getAddressLine(0) ?: ""
                        val locationName = address.featureName ?: ""

                        _selectedLocation.value = location.copy(
                            address = addressText,
                            name = locationName
                        )
                    }
                    isLoading = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al obtener la dirección: ${e.message}"
                isLoading = false
            }
        }
    }

    /**
     * Obtiene la ubicación actual del dispositivo usando FusedLocationProviderClient
     * @param context Contexto necesario para acceder al servicio de ubicación
     */
    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context) {
        viewModelScope.launch {
            isLoading = true
            try {
                val fusedLocationClient: FusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(context)

                // Crear token de cancelación para la solicitud de ubicación
                val cancellationTokenSource = CancellationTokenSource()

                // Solicitar la ubicación actual con prioridad alta
                val task = fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cancellationTokenSource.token
                )

                // Utilizamos la extensión await() proporcionada por kotlinx-coroutines-play-services
                val location: Location? = task.await()

                location?.let {
                    val latLng = LatLng(it.latitude, it.longitude)

                    // Actualizar la ubicación seleccionada
                    updateSelectedLocation(latLng)

                    // Actualizar la posición de la cámara
                    val newPosition = CameraPosition.fromLatLngZoom(latLng, 15f)
                    setCameraPosition(newPosition)

                    // Obtener la dirección para esta ubicación
                    val geocoder = Geocoder(context, Locale.getDefault())
                    getAddressFromLocation(geocoder, latLng)
                } ?: run {
                    _errorMessage.value = "No se pudo obtener la ubicación actual"
                    isLoading = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al obtener la ubicación: ${e.message}"
                isLoading = false
            }
        }
    }

    /**
     * Busca una ubicación a partir de una dirección ingresada como texto
     * @param context Contexto necesario para crear el Geocoder
     * @param addressText Texto de la dirección a buscar
     */
    fun searchLocationByAddress(context: Context, addressText: String) {
        if (addressText.isBlank()) {
            _errorMessage.value = "Por favor ingresa una dirección para buscar"
            return
        }

        viewModelScope.launch {
            try {
                isLoading = true
                val geocoder = Geocoder(context, Locale.getDefault())

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocationName(addressText, 1) { addresses ->
                        if (addresses.isNotEmpty()) {
                            val address = addresses[0]
                            val latLng = LatLng(address.latitude, address.longitude)

                            // Actualizar la ubicación seleccionada
                            updateSelectedLocation(latLng)

                            // Actualizar la posición de la cámara
                            val newPosition = CameraPosition.fromLatLngZoom(latLng, 15f)
                            setCameraPosition(newPosition)

                            // Obtener los detalles completos de la dirección
                            getAddressFromLocation(geocoder, latLng)
                        } else {
                            _errorMessage.value = "No se encontraron ubicaciones para la dirección ingresada"
                            isLoading = false
                        }
                    }
                } else {
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(addressText, 1)

                    if (!addresses.isNullOrEmpty()) {
                        val address = addresses[0]
                        val latLng = LatLng(address.latitude, address.longitude)

                        // Actualizar la ubicación seleccionada
                        updateSelectedLocation(latLng)

                        // Actualizar la posición de la cámara
                        val newPosition = CameraPosition.fromLatLngZoom(latLng, 15f)
                        setCameraPosition(newPosition)

                        // Obtener los detalles completos de la dirección
                        getAddressFromLocation(geocoder, latLng)
                    } else {
                        _errorMessage.value = "No se encontraron ubicaciones para la dirección ingresada"
                        isLoading = false
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al buscar la ubicación: ${e.message}"
                isLoading = false
            }
        }
    }

    /**
     * Maneja el resultado de Places Autocomplete
     * @param place El lugar seleccionado del autocompletado
     */
    fun handlePlaceResult(place: Place) {
        viewModelScope.launch {
            try {
                isLoading = true

                // Obtener la latitud/longitud del lugar
                val latLng = place.latLng

                if (latLng != null) {
                    // Actualizar la ubicación seleccionada
                    updateSelectedLocation(latLng)

                    // Actualizar la posición de la cámara
                    val newPosition = CameraPosition.fromLatLngZoom(latLng, 15f)
                    setCameraPosition(newPosition)

                    // Crear/actualizar el LocationModel con la información del lugar
                    val location = LocationModel(
                        latitude = latLng.latitude,
                        longitude = latLng.longitude,
                        name = place.name ?: "",
                        address = place.address ?: ""
                    )

                    _selectedLocation.value = location
                } else {
                    _errorMessage.value = "No se pudo obtener la ubicación del lugar seleccionado"
                }
                isLoading = false
            } catch (e: Exception) {
                _errorMessage.value = "Error al procesar el lugar seleccionado: ${e.message}"
                isLoading = false
            }
        }
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Reporta un mensaje de error
     * @param message Mensaje de error a mostrar
     */
    fun reportError(message: String) {
        _errorMessage.value = message
    }
}

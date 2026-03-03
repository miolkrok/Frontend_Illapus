package com.example.illapus.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.illapus.data.api.ApiClient
import com.example.illapus.data.model.ActivityDetailsModel
import com.example.illapus.data.model.HostModel
import com.example.illapus.data.model.LocationModel
import com.example.illapus.data.model.PropertyDetailsModel
import com.example.illapus.data.model.ReserveDTO
import com.example.illapus.data.repository.ActivityRepository
import com.example.illapus.data.repository.ReservationRepository
import com.example.illapus.utils.LocationUtils
import com.example.illapus.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ActivityDetailsViewModel : BaseViewModel() {
    // Estado para los detalles de la actividad
    private val _activityDetails = MutableStateFlow<PropertyDetailsModel?>(null)
    val activityDetails: StateFlow<PropertyDetailsModel?> = _activityDetails.asStateFlow()

    // Estado para controlar el índice currentImageIndex del carrusel de imágenes
    private val _currentImageIndex = MutableStateFlow(0)
    val currentImageIndex: StateFlow<Int> = _currentImageIndex.asStateFlow()

    // Estado para controlar si está cargando
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Estado para manejar errores
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Estado para controlar la visibilidad del BottomSheet
    private val _isBottomSheetVisible = MutableStateFlow(false)
    val isBottomSheetVisible: StateFlow<Boolean> = _isBottomSheetVisible.asStateFlow()

    // Estado para controlar la cantidad de personas
    private val _guestCount = MutableStateFlow(1)
    val guestCount: StateFlow<Int> = _guestCount.asStateFlow()

    // Estado para el precio total
    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice.asStateFlow()

    // Estados para la funcionalidad de reserva
    private val _selectedReservationDate = MutableStateFlow<LocalDate?>(null)
    val selectedReservationDate: StateFlow<LocalDate?> = _selectedReservationDate.asStateFlow()

    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker: StateFlow<Boolean> = _showDatePicker.asStateFlow()

    private val _isCreatingReservation = MutableStateFlow(false)
    val isCreatingReservation: StateFlow<Boolean> = _isCreatingReservation.asStateFlow()

    // Estado para controlar el resultado de la reserva
    private val _reservationResult = MutableStateFlow<String?>(null)
    val reservationResult: StateFlow<String?> = _reservationResult.asStateFlow()

    private val activityRepository = ActivityRepository(ApiClient.activityService)
    private val reservationRepository = ReservationRepository(ApiClient.reservaService)

    /**
     * Carga los detalles de una actividad específica desde la API
     * @param activityId El ID de la actividad a cargar
     */
    fun loadActivityDetails(activityId: String) {
        val id = activityId.toIntOrNull()
        if (id == null) {
            _error.value = "ID de actividad inválido"
            _isLoading.value = false
            return
        }

        _isLoading.value = true
        _error.value = null

        // Usar el método del BaseViewModel para manejo automático de errores
        launchApiCall(
            apiCall = { activityRepository.getActivityDetails(id) },
            onSuccess = { activityDetails ->
                val uiModel = mapActivityToPropertyDetails(activityDetails)
                _activityDetails.value = uiModel

                // Inicializar el precio total con el precio base por una persona
                updateTotalPrice()

                _isLoading.value = false
            },
            onError = { exception ->
                _error.value = "Error al cargar los detalles: ${exception.message}"
                _isLoading.value = false
            }
        )
    }

    /**
     * Función para reintentar la carga
     */
    fun retryLoad() {
        _activityDetails.value?.let { details ->
            loadActivityDetails(details.id)
        }
    }

    /**
     * Convierte el modelo de la API al modelo de la UI
     */
    private fun mapActivityToPropertyDetails(activity: ActivityDetailsModel): PropertyDetailsModel {
        // Parsear las ubicaciones JSON usando LocationUtils
        val departureLocation = LocationUtils.parseLocationFromJson(activity.departureLocationJson)
            ?: LocationModel(0.0, 0.0, "Ubicación no disponible", "")

        val destinationLocation = LocationUtils.parseLocationFromJson(activity.destinationLocationJson)
            ?: LocationModel(0.0, 0.0, "Destino no disponible", "")

        // Ordenar imágenes: principal primero
        val sortedImages = activity.galeria
            .sortedByDescending { it.isPrimaryImage }
            .map { it.displayImage ?: ""  }

        // Extraer nombres de servicios
        val services = activity.services.map { it.serviceName }

        return PropertyDetailsModel(
            id = activity.id.toString(),
            title = activity.title,
            description = activity.description,
            images = sortedImages,
            rating = 0f, // Por ahora no viene en la API
            price = activity.price,
            departureLocation = departureLocation,
            destinationLocation = destinationLocation,
            host = HostModel(
                name = "Carlos García", // Mantenemos quemado por ahora
                yearsActive = 9
            ),
            duration = activity.duration,
            availability = activity.availability,
            activityType = activity.activityType,
            difficultyLevel = activity.difficultyLevel,
            minPeople = activity.minPeople,
            maxPeople = activity.maxPeople,
            services = services
        )
    }

    /**
     * Actualiza el índice currentImageIndex del carrusel
     * @param index El nuevo índice
     */
    fun updateCurrentImageIndex(index: Int) {
        _currentImageIndex.value = index
    }

    /**
     * Limpia el mensaje de error
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Muestra el BottomSheet de reserva
     */
    fun showReservationBottomSheet() {
        _isBottomSheetVisible.value = true
    }

    /**
     * Oculta el BottomSheet de reserva
     */
    fun hideReservationBottomSheet() {
        _isBottomSheetVisible.value = false
    }

    /**
     * Incrementa la cantidad de personas en la reserva
     */
    fun incrementGuestCount() {
        val maxGuests = _activityDetails.value?.maxPeople ?: 10
        if (_guestCount.value < maxGuests) {
            _guestCount.value += 1
            updateTotalPrice()
        }
    }

    /**
     * Decrementa la cantidad de personas en la reserva
     */
    fun decrementGuestCount() {
        val minGuests = _activityDetails.value?.minPeople ?: 1
        if (_guestCount.value > minGuests) {
            _guestCount.value -= 1
            updateTotalPrice()
        }
    }

    /**
     * Actualiza el precio total en base a la cantidad de personas
     */
    private fun updateTotalPrice() {
        _activityDetails.value?.let { activity ->
            _totalPrice.value = activity.price * _guestCount.value
        }
    }

    /**
     * Selecciona una fecha para la reserva
     * @param date La fecha seleccionada
     */
    fun selectReservationDate(date: LocalDate) {
        _selectedReservationDate.value = date
    }

    /**
     * Muestra el DatePicker
     */
    fun showDatePicker() {
        _showDatePicker.value = true
    }

    /**
     * Oculta el DatePicker
     */
    fun hideDatePicker() {
        _showDatePicker.value = false
    }

    /**
     * Muestra u oculta el DatePicker
     */
    fun toggleDatePicker() {
        _showDatePicker.value = !_showDatePicker.value
    }

    /**
     * Crea una reserva para la actividad actual
     */
    fun createReservation() {
        viewModelScope.launch {
            _isCreatingReservation.value = true
            _error.value = null

            val activity = _activityDetails.value
            val date = _selectedReservationDate.value

            if (activity == null) {
                _error.value = "Actividad no válida"
                _isCreatingReservation.value = false
                return@launch
            }

            if (date == null) {
                _error.value = "Por favor selecciona una fecha para la reserva"
                _isCreatingReservation.value = false
                return@launch
            }

            val userId = TokenManager.getUserId()
            if (userId <= 0) {
                _error.value = "Usuario no válido. Por favor inicia sesión nuevamente"
                _isCreatingReservation.value = false
                return@launch
            }

            val reserva = ReserveDTO(
                actividadId = activity.id.toInt(),
                usuarioId = userId,
                fechaActividad = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                fechaReserva = date.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                cantidadPersonas = _guestCount.value,
                costoTotal = BigDecimal.valueOf(_totalPrice.value)
            )

            try {
                val response = reservationRepository.createReservation(reserva)

                if (response.isSuccessful) {
                    _reservationResult.value = "¡Reserva creada con éxito!"
                    _isBottomSheetVisible.value = false
                    // Limpiar la fecha seleccionada
                    _selectedReservationDate.value = null
                } else {
                    _error.value = "Error al crear la reserva: ${response.message()}"
                }
            } catch (exception: Exception) {
                _error.value = "Error al crear la reserva: ${exception.message}"
            } finally {
                _isCreatingReservation.value = false
            }
        }
    }

    /**
     * Limpia el resultado de la reserva
     */
    fun clearReservationResult() {
        _reservationResult.value = null
    }
}

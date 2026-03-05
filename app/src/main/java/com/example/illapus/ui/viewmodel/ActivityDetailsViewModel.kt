package com.example.illapus.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.example.illapus.data.api.ApiClient
import com.example.illapus.data.model.ActivityDetailsModel
import com.example.illapus.data.model.HostModel
import com.example.illapus.data.model.LocationModel
import com.example.illapus.data.model.PagoRequest
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
import java.io.ByteArrayOutputStream
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

    private val _comprobanteUri = MutableStateFlow<Uri?>(null)
    val comprobanteUri: StateFlow<Uri?> = _comprobanteUri.asStateFlow()

    private val _comprobanteBase64 = MutableStateFlow<String?>(null)
    val comprobanteBase64: StateFlow<String?> = _comprobanteBase64.asStateFlow()

    private val _comprobanteError = MutableStateFlow<String?>(null)
    val comprobanteError: StateFlow<String?> = _comprobanteError.asStateFlow()

    private val _comprobanteTexto = MutableStateFlow("")
    val comprobanteTexto: StateFlow<String> = _comprobanteTexto.asStateFlow()

    fun setComprobanteTexto(texto: String) {
        _comprobanteTexto.value = texto
    }

    // Estado de disponibilidad
    private val _cupoDisponible = MutableStateFlow<Int?>(null)
    val cupoDisponible: StateFlow<Int?> = _cupoDisponible.asStateFlow()

    private val _cupoInfo = MutableStateFlow<String?>(null)
    val cupoInfo: StateFlow<String?> = _cupoInfo.asStateFlow()

    private val _isCheckingCupo = MutableStateFlow(false)
    val isCheckingCupo: StateFlow<Boolean> = _isCheckingCupo.asStateFlow()

    private val _fechaBloqueada = MutableStateFlow(false)
    val fechaBloqueada: StateFlow<Boolean> = _fechaBloqueada.asStateFlow()

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
        // Verificar disponibilidad al seleccionar fecha
        checkDisponibilidad(date)
    }

    /**
     * Consulta al backend si hay cupo disponible para la fecha seleccionada
     */
    private fun checkDisponibilidad(date: LocalDate) {
        val activity = _activityDetails.value ?: return
        val actividadId = activity.id.toIntOrNull() ?: return

        viewModelScope.launch {
            _isCheckingCupo.value = true
            _cupoInfo.value = null
            _fechaBloqueada.value = false

            try {
                val fechaStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE) // yyyy-MM-dd
                val response = ApiClient.reservaService.getDisponibilidad(actividadId, fechaStr)

                if (response.isSuccessful) {
                    val disponibilidad = response.body()
                    if (disponibilidad != null) {
                        val cupo = disponibilidad.cupoDisponible ?: 0
                        val reservadas = disponibilidad.personasReservadas ?: 0
                        val max = disponibilidad.maxPeople ?: activity.maxPeople

                        _cupoDisponible.value = cupo

                        if (cupo <= 0) {
                            _fechaBloqueada.value = true
                            _cupoInfo.value = "⛔ No hay cupo para esta fecha ($reservadas/$max personas reservadas)"
                        } else if (cupo < _guestCount.value) {
                            _cupoInfo.value = "⚠️ Solo quedan $cupo cupos. Reduce el número de personas."
                        } else {
                            _cupoInfo.value = "✅ Disponible: $cupo cupos restantes ($reservadas/$max)"
                        }

                        Log.d("ActivityDetailsVM", "Disponibilidad: $reservadas/$max reservadas, $cupo disponibles")
                    }
                } else {
                    Log.e("ActivityDetailsVM", "Error al consultar disponibilidad: ${response.code()}")
                    // No bloquear si falla la consulta, el backend validará al crear
                    _cupoInfo.value = null
                }
            } catch (e: Exception) {
                Log.e("ActivityDetailsVM", "Error de conexión al consultar cupo: ${e.message}")
                // No bloquear, dejar que el backend valide
                _cupoInfo.value = null
            } finally {
                _isCheckingCupo.value = false
            }
        }
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
     * Guarda la URI de la imagen seleccionada y la convierte a base64
     */
    fun setComprobanteImage(uri: Uri, context: Context) {
        _comprobanteUri.value = uri
        _comprobanteError.value = null

        viewModelScope.launch {
            try {
                val base64 = convertUriToBase64(uri, context)
                _comprobanteBase64.value = base64
                Log.d("ActivityDetailsVM", "Comprobante convertido a base64 (${base64.length} chars)")
            } catch (e: Exception) {
                Log.e("ActivityDetailsVM", "Error al convertir imagen: ${e.message}")
                _comprobanteError.value = "Error al procesar la imagen"
                _comprobanteUri.value = null
                _comprobanteBase64.value = null
            }
        }
    }

    /**
     * Limpia la imagen del comprobante
     */
    fun clearComprobante() {
        _comprobanteUri.value = null
        _comprobanteBase64.value = null
        _comprobanteError.value = null
        _comprobanteTexto.value = ""
    }

    /**
     * Convierte una URI de imagen a base64
     */
    private fun convertUriToBase64(uri: Uri, context: Context): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("No se pudo abrir la imagen")

        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()

        // Comprimir la imagen para no enviar archivos muy grandes
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val bytes = outputStream.toByteArray()

        val base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)
        return "data:image/jpeg;base64,$base64String"
    }

    /**
     * Crea una reserva y luego el pago con comprobante.
     * Flujo: Validar → Crear reserva → Crear pago con imagen → Éxito
     */
    fun createReservation() {
        viewModelScope.launch {
            _isCreatingReservation.value = true
            _error.value = null

            val activity = _activityDetails.value
            val date = _selectedReservationDate.value
            val comprobanteBase64 = _comprobanteBase64.value

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

            if (comprobanteBase64.isNullOrEmpty()) {
                _error.value = "Debes subir el comprobante de pago para reservar"
                _isCreatingReservation.value = false
                return@launch
            }

            val userId = TokenManager.getUserId()
            if (userId <= 0) {
                _error.value = "Usuario no válido. Por favor inicia sesión nuevamente"
                _isCreatingReservation.value = false
                return@launch
            }

            // Validar que no esté bloqueada la fecha
            if (_fechaBloqueada.value) {
                _error.value = "No hay cupo disponible para la fecha seleccionada. Elige otra fecha."
                _isCreatingReservation.value = false
                return@launch
            }

            // Validar cupo si lo tenemos
            val cupo = _cupoDisponible.value
            if (cupo != null && _guestCount.value > cupo) {
                _error.value = "Solo quedan $cupo cupos disponibles. Reduce el número de personas."
                _isCreatingReservation.value = false
                return@launch
            }

            try {
                // ── PASO 1: Crear la reserva ──
                val reserva = ReserveDTO(
                    actividadId = activity.id.toInt(),
                    usuarioId = userId,
                    fechaActividad = date.atStartOfDay().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    fechaReserva = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    cantidadPersonas = _guestCount.value,
                    costoTotal = BigDecimal.valueOf(_totalPrice.value)
                )

                Log.d("ActivityDetailsVM", "Creando reserva para actividad ${activity.id}...")
                val reservaResponse = reservationRepository.createReservation(reserva)

                if (!reservaResponse.isSuccessful) {
                    val errorBody = reservaResponse.errorBody()?.string() ?: ""
                    if (reservaResponse.code() == 409) {
                        // Error de cupo
                        _error.value = "No hay cupo suficiente para esta fecha. Intenta con otra fecha o menos personas."
                        // Recargar disponibilidad
                        date?.let { checkDisponibilidad(it) }
                    } else {
                        _error.value = "Error al crear la reserva: ${reservaResponse.message()}"
                    }
                    _isCreatingReservation.value = false
                    return@launch
                }

                val reservaCreada = reservaResponse.body()
                if (reservaCreada == null) {
                    _error.value = "Error: respuesta vacía del servidor al crear reserva"
                    _isCreatingReservation.value = false
                    return@launch
                }

                Log.d("ActivityDetailsVM", "Reserva creada con ID: ${reservaCreada.id}")

                // ── PASO 2: Crear el pago con comprobante ──
                val pagoRequest = PagoRequest(
                    reservaId = reservaCreada.id,
                    monto = BigDecimal.valueOf(_totalPrice.value),
                    metodoPago = "COMPROBANTE",
                    estado = "PENDIENTE",
                    comprobante = _comprobanteTexto.value,
                    imagenComprobante = comprobanteBase64
                )

                Log.d("ActivityDetailsVM", "Creando pago con comprobante para reserva ${reservaCreada.id}...")
                val pagoResponse = ApiClient.pagoService.createPago(pagoRequest)

                if (pagoResponse.isSuccessful) {
                    Log.d("ActivityDetailsVM", "Pago creado exitosamente")
                    _reservationResult.value = "¡Reserva creada con éxito! Comprobante enviado."
                    _isBottomSheetVisible.value = false
                    // Limpiar estados
                    _selectedReservationDate.value = null
                    clearComprobante()
                } else {
                    val errorBody = pagoResponse.errorBody()?.string() ?: "Error desconocido"
                    Log.e("ActivityDetailsVM", "Error al crear pago: $errorBody")
                    // La reserva se creó pero el pago falló
                    _reservationResult.value = "Reserva creada, pero hubo un error al subir el comprobante. Por favor contacta al anfitrión."
                    _isBottomSheetVisible.value = false
                }

            } catch (exception: Exception) {
                Log.e("ActivityDetailsVM", "Error: ${exception.message}")
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

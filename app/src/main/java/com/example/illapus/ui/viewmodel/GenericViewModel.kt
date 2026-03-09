package com.example.illapus.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.illapus.data.api.ApiClient
import com.example.illapus.data.model.*
import com.example.illapus.data.repository.ActivityRepository
import com.example.illapus.utils.TokenManager
import com.example.illapus.utils.ImageUtils
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class GenericViewModel : ViewModel() {
    private val activityRepository = ActivityRepository(ApiClient.activityService)

    // Estado del flujo de creación de actividades
    private val _currentStep = MutableStateFlow(ActivityCreationStep.BASIC_INFO)
    val currentStep: StateFlow<ActivityCreationStep> = _currentStep.asStateFlow()

    // Datos de la actividad
    private val _activityData = MutableStateFlow(ActivityData())
    val activityData: StateFlow<ActivityData> = _activityData.asStateFlow()

    // Lista local de imágenes antes de convertir a base64
    private val _localImages = MutableStateFlow<List<LocalImageData>>(emptyList())
    val localImages: StateFlow<List<LocalImageData>> = _localImages.asStateFlow()

    // Estados para modo edición
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    private val _editingActivityId = MutableStateFlow<Int?>(null)
    val editingActivityId: StateFlow<Int?> = _editingActivityId.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Estado de error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Estado de éxito
    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    // Guardar servicios originales para detectar nuevos en edición
    private val _originalServices = MutableStateFlow<List<String>>(emptyList())

    // IDs de servicios a eliminar en modo edición
    private val _serviceIdsToDelete = MutableStateFlow<List<Int>>(emptyList())

    // Lista original de ServicioResponse para obtener el id real
    private val _originalServiceResponses = MutableStateFlow<List<ServicioResponse>>(emptyList())

    // Lista original de imágenes de galería para obtener el id real
    private val _originalGalleryResponses = MutableStateFlow<List<GaleriaResponse>>(emptyList())
    // IDs de imágenes a eliminar en modo edición
    private val _galleryIdsToDelete = MutableStateFlow<List<Int>>(emptyList())

    // Función para ir al siguiente paso
    fun goToNext() {
        when (_currentStep.value) {
            ActivityCreationStep.BASIC_INFO -> _currentStep.value = ActivityCreationStep.DEPARTURE_LOCATION
            ActivityCreationStep.DEPARTURE_LOCATION -> _currentStep.value = ActivityCreationStep.DESTINATION_LOCATION
            ActivityCreationStep.DESTINATION_LOCATION -> _currentStep.value = ActivityCreationStep.ACTIVITY_TYPE
            ActivityCreationStep.ACTIVITY_TYPE -> _currentStep.value = ActivityCreationStep.DETAILS
            ActivityCreationStep.DETAILS -> _currentStep.value = ActivityCreationStep.GALLERY
            ActivityCreationStep.GALLERY -> _currentStep.value = ActivityCreationStep.SERVICES
            ActivityCreationStep.SERVICES -> {
                // Último paso, aquí no se avanza más
            }
        }
    }

    // Función para retroceder
    fun goBack() {
        when (_currentStep.value) {
            ActivityCreationStep.BASIC_INFO -> { /* Ya es el primer paso */ }
            ActivityCreationStep.DEPARTURE_LOCATION -> _currentStep.value = ActivityCreationStep.BASIC_INFO
            ActivityCreationStep.DESTINATION_LOCATION -> _currentStep.value = ActivityCreationStep.DEPARTURE_LOCATION
            ActivityCreationStep.ACTIVITY_TYPE -> _currentStep.value = ActivityCreationStep.DESTINATION_LOCATION
            ActivityCreationStep.DETAILS -> _currentStep.value = ActivityCreationStep.ACTIVITY_TYPE
            ActivityCreationStep.GALLERY -> _currentStep.value = ActivityCreationStep.DETAILS
            ActivityCreationStep.SERVICES -> _currentStep.value = ActivityCreationStep.GALLERY
        }
    }

    // Función para guardar y salir
    fun saveAndExit(context: Context) {
        if (_currentStep.value == ActivityCreationStep.SERVICES) {
            saveActivityWithImages(context)
        }
        // Si no es el último paso, simplemente salir
    }

    /**
     * Inicializar modo edición cargando datos de actividad existente
     */
    fun initEditMode(activityId: Int) {
        _isEditMode.value = true
        _editingActivityId.value = activityId
        loadActivityForEdit(activityId)
    }

    /**
     * Cargar datos de actividad para edición
     */
    private fun loadActivityForEdit(activityId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val response = activityRepository.getActivityDetails(activityId)
                if (response.isSuccessful && response.body() != null) {
                    mapActivityDetailsToData(response.body()!!)
                } else {
                    _error.value = "Error al cargar la actividad"
                }
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Error al cargar la actividad: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Mapear ActivityDetailsModel a ActivityData para edición
     */
    private fun mapActivityDetailsToData(details: ActivityDetailsModel) {
        _activityData.value = ActivityData(
            titulo = details.title,
            descripcion = details.description,
            ubicacionDestino = details.destinationLocationJson,
            ubicacionSalida = details.departureLocationJson,
            tipoActividad = details.activityType,
            nivelDificultad = details.difficultyLevel,
            precio = details.price,
            duracion = details.duration,
            maximoPersonas = details.maxPeople,
            minimoPersonas = details.minPeople,
            servicioEvento = (details.services ?: emptyList()).map { ServicioItem(it.serviceName) },
            diasDisponibles = details.availability.split(", ").filter { it.isNotBlank() },
            fechaInicioDisponible = details.startDate ?: "",
            fechaFinDisponible = details.endDate ?: ""
        )

        // Convertir galería a imágenes locales (para mostrar en UI)
        val localImages = (details.galeria ?: emptyList()).map { galeriaItem ->
            LocalImageData(
                uri = "",
                nombreArchivo = galeriaItem.fileName,
                tipoContenido = galeriaItem.contentType,
                esImagenPrincipal = galeriaItem.isPrimaryImage,
                base64Image = galeriaItem.displayImage
            )
        }
        _localImages.value = localImages

        // Guardar galería original y limpiar eliminaciones previas
        _originalGalleryResponses.value = (details.galeria ?: emptyList()).map {
            GaleriaResponse(
                id = it.id,
                urlFoto = it.urlFoto,
                imagenBinaria = it.imageBinary,
                nombreArchivo = it.fileName,
                tipoContenido = it.contentType,
                tamanoArchivo = 0L,
                esImagenPrincipal = it.isPrimaryImage
            )
        }
        _galleryIdsToDelete.value = emptyList()

        // Guardar servicios originales (nombres)
        _originalServices.value = (details.services ?: emptyList()).map { it.serviceName }
        _serviceIdsToDelete.value = emptyList() // Limpiar eliminaciones previas

        // Corrige aquí: si details.services NO es List<ServicioResponse>, mapea manualmente
        // Si es List<ActivityService> o similar, mapea a ServicioResponse
        _originalServiceResponses.value = (details.services ?: emptyList()).map {
            ServicioResponse(
                id = it.id,
                listaServicio = it.serviceName // o it.listaServicio según el modelo
            )
        }
    }

    /**
     * Actualizar actividad existente
     */
    private fun updateActivityWithImages(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val activityId = _editingActivityId.value ?: return@launch
                val userId = TokenManager.getUserId() ?: 1
                val currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                // Convertir imágenes (tanto nuevas como existentes) a formato API
                val galeriaItems = convertLocalImagesToApiFormatForEdit(context)

                val activityRequest = ActivityRequest(
                    usuarioId = userId,
                    titulo = _activityData.value.titulo,
                    descripcion = _activityData.value.descripcion,
                    ubicacionDestino = _activityData.value.ubicacionDestino,
                    ubicacionSalida = _activityData.value.ubicacionSalida,
                    tipoActividad = _activityData.value.tipoActividad.uppercase(Locale.getDefault()),
                    nivelDificultad = _activityData.value.nivelDificultad.uppercase(Locale.getDefault()),
                    precio = _activityData.value.precio,
                    duracion = _activityData.value.duracion,
                    disponibilidad = _activityData.value.diasDisponibles.joinToString(", "),
                    maximoPersonas = _activityData.value.maximoPersonas,
                    minimoPersonas = _activityData.value.minimoPersonas,
                    estadoActividad = "ACTIVA",
                    fechaCreacion = currentTime, // Este será ignorado por el backend en modo edición
                    fechaActualizacion = currentTime,
                    fechaInicioDisponible = _activityData.value.fechaInicioDisponible,
                    fechaFinDisponible = _activityData.value.fechaFinDisponible,
                    galeria = galeriaItems,
                    servicioEvento = _activityData.value.servicioEvento
                )

                val response = activityRepository.updateActivity(activityId, activityRequest)

                if (response.isSuccessful) {
                    _isSuccess.value = true
                    clearData()
                } else {
                    _error.value = "Error al actualizar la actividad: ${response.message()}"
                }

            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Convertir imágenes para modo edición (maneja tanto nuevas como existentes)
     */
    private fun convertLocalImagesToApiFormatForEdit(context: Context): List<GaleriaItem> {
        return _localImages.value.map { localImage ->
            if (localImage.uri.isNotEmpty()) {
                // Imagen nueva - convertir desde URI
                val uri = Uri.parse(localImage.uri)
                val base64Image = ImageUtils.uriToBase64(context, uri)
                val fileSize = ImageUtils.getFileSize(context, uri)

                GaleriaItem(
                    imagenBinaria = base64Image,
                    nombreArchivo = localImage.nombreArchivo,
                    tipoContenido = localImage.tipoContenido,
                    tamanoArchivo = fileSize,
                    esImagenPrincipal = localImage.esImagenPrincipal
                )
            } else {
                // Imagen existente - usar base64 guardado
                GaleriaItem(
                    imagenBinaria = localImage.base64Image,
                    nombreArchivo = localImage.nombreArchivo,
                    tipoContenido = localImage.tipoContenido,
                    tamanoArchivo = 0L, // No calculamos tamaño para imágenes existentes
                    esImagenPrincipal = localImage.esImagenPrincipal
                )
            }
        }
    }

    /**
     * Eliminar servicios marcados en el backend
     */
    private suspend fun deleteMarkedServices(activityId: Int) {
        val idsToDelete = _serviceIdsToDelete.value
        for (serviceId in idsToDelete) {
            activityRepository.deleteService(activityId, serviceId)
        }
        _serviceIdsToDelete.value = emptyList()
    }

    /**
     * Eliminar imagen de galería (soporta modo edición)
     */
    fun removeGalleryImage(index: Int) {
        val currentImages = _localImages.value.toMutableList()
        if (index in currentImages.indices) {
            val localImage = currentImages[index]
            // Solo marcar para eliminar si es una imagen existente (sin uri local)
            if (localImage.uri.isEmpty()) {
                val galleryId = _originalGalleryResponses.value.firstOrNull {
                    it.nombreArchivo == localImage.nombreArchivo
                }?.id
                galleryId?.let {
                    val idsToDelete = _galleryIdsToDelete.value.toMutableList()
                    if (!idsToDelete.contains(it)) {
                        idsToDelete.add(it)
                        _galleryIdsToDelete.value = idsToDelete
                    }
                }
            }
            val wasMainImage = localImage.esImagenPrincipal
            currentImages.removeAt(index)
            // Si eliminamos la imagen principal y hay otras, hacer principal la primera
            if (wasMainImage && currentImages.isNotEmpty()) {
                currentImages[0] = currentImages[0].copy(esImagenPrincipal = true)
            }
            _localImages.value = currentImages
        }
    }

    /**
     * Eliminar imágenes de galería marcadas en el backend
     */
    private suspend fun deleteMarkedGalleryImages(activityId: Int) {
        val idsToDelete = _galleryIdsToDelete.value
        for (galleryId in idsToDelete) {
            activityRepository.deleteGalleryImage(activityId, galleryId)
        }
        _galleryIdsToDelete.value = emptyList()
    }

    /**
     * Limpiar datos y resetear modo edición
     */
    private fun clearData() {
        _activityData.value = ActivityData()
        _localImages.value = emptyList()
        _currentStep.value = ActivityCreationStep.BASIC_INFO
        _isEditMode.value = false
        _editingActivityId.value = null
        _serviceIdsToDelete.value = emptyList()
        _galleryIdsToDelete.value = emptyList()
    }

    // Funciones para actualizar datos
    fun updateBasicInfo(titulo: String, descripcion: String) {
        _activityData.value = _activityData.value.copy(
            titulo = titulo,
            descripcion = descripcion
        )
    }

    fun updateDepartureLocation(location: LocationData) {
        _activityData.value = _activityData.value.copy(
            ubicacionSalida = locationDataToJson(location)
        )
    }

    fun updateDestinationLocation(location: LocationData) {
        _activityData.value = _activityData.value.copy(
            ubicacionDestino = locationDataToJson(location)
        )
    }

    fun updateActivityType(tipo: String, nivelDificultad: String) {
        _activityData.value = _activityData.value.copy(
            tipoActividad = tipo,
            nivelDificultad = nivelDificultad
        )
    }

    fun updateDetails(precio: Double, duracion: String, maximoPersonas: Int, minimoPersonas: Int = 1) {
        _activityData.value = _activityData.value.copy(
            precio = precio,
            duracion = duracion,
            maximoPersonas = maximoPersonas,
            minimoPersonas = minimoPersonas
        )
    }

    fun updateAvailability(diasDisponibles: List<String>, fechaInicio: String, fechaFin: String) {
        _activityData.value = _activityData.value.copy(
            diasDisponibles = diasDisponibles,
            fechaInicioDisponible = fechaInicio,
            fechaFinDisponible = fechaFin
        )
    }

    fun addGalleryImage(context: Context, uri: Uri) {
        val fileName = ImageUtils.getFileName(context, uri)
        val mimeType = ImageUtils.getMimeType(context, uri)
        val isFirst = _localImages.value.isEmpty()

        val localImage = LocalImageData(
            uri = uri.toString(),
            nombreArchivo = fileName,
            tipoContenido = mimeType,
            esImagenPrincipal = isFirst
        )

        val currentImages = _localImages.value.toMutableList()
        currentImages.add(localImage)
        _localImages.value = currentImages
    }

//    fun removeGalleryImage(index: Int) {
//        val currentImages = _localImages.value.toMutableList()
//        if (index in currentImages.indices) {
//            val wasMainImage = currentImages[index].esImagenPrincipal
//            currentImages.removeAt(index)
//
//            // Si eliminamos la imagen principal y hay otras, hacer principal la primera
//            if (wasMainImage && currentImages.isNotEmpty()) {
//                currentImages[0] = currentImages[0].copy(esImagenPrincipal = true)
//            }
//
//            _localImages.value = currentImages
//        }
//    }

    fun setMainImage(index: Int) {
        val currentImages = _localImages.value.toMutableList()
        if (index in currentImages.indices) {
            // Quitar el flag de principal de todas las imágenes
            for (i in currentImages.indices) {
                currentImages[i] = currentImages[i].copy(esImagenPrincipal = false)
            }
            // Establecer la imagen seleccionada como principal
            currentImages[index] = currentImages[index].copy(esImagenPrincipal = true)
            _localImages.value = currentImages
        }
    }

    fun addService(servicio: String) {
        val currentServices = _activityData.value.servicioEvento.toMutableList()
        currentServices.add(ServicioItem(servicio))
        _activityData.value = _activityData.value.copy(servicioEvento = currentServices)
    }

    // Eliminar servicio (soporta modo edición)
    fun removeService(index: Int) {
        val currentServices = _activityData.value.servicioEvento.toMutableList()
        if (index in currentServices.indices) {
            val serviceName = currentServices[index].listaServicio
            // Buscar el id real en la lista original de ServicioResponse
            val serviceId = _originalServiceResponses.value.firstOrNull { it.listaServicio == serviceName }?.id

            serviceId?.let {
                val idsToDelete = _serviceIdsToDelete.value.toMutableList()
                if (!idsToDelete.contains(it)) {
                    idsToDelete.add(it)
                    _serviceIdsToDelete.value = idsToDelete
                }
            }
            currentServices.removeAt(index)
            _activityData.value = _activityData.value.copy(servicioEvento = currentServices)
        }
    }

    fun convertLocalImagesToApiFormat(context: Context): List<GaleriaItem> {
        return _localImages.value.map { localImage ->
            val uri = Uri.parse(localImage.uri)
            val base64Image = ImageUtils.uriToBase64(context, uri)
            val fileSize = ImageUtils.getFileSize(context, uri)

            GaleriaItem(
                imagenBinaria = base64Image,
                nombreArchivo = localImage.nombreArchivo,
                tipoContenido = localImage.tipoContenido,
                tamanoArchivo = fileSize,
                esImagenPrincipal = localImage.esImagenPrincipal
            )
        }
    }

    /**
     * Actualizar actividad existente (sin galería, solo datos)
     */
    private suspend fun updateActivityDataOnly(activityId: Int, activityRequest: ActivityRequest): Boolean {
        // Llama al endpoint de actualización sin galería
        val response = activityRepository.updateActivity(
            activityId,
            activityRequest.copy(galeria = emptyList()) // No enviar galería aquí
        )
        return response.isSuccessful
    }

    /**
     * Agregar imágenes nuevas a la galería usando el endpoint específico
     */
    private suspend fun addNewGalleryImages(context: Context, activityId: Int) {
        val newImages = _localImages.value.filter { it.uri.isNotEmpty() }
        for (localImage in newImages) {
            val uri = Uri.parse(localImage.uri)
            var base64Image = ImageUtils.uriToBase64(context, uri)
            // Limpiar saltos de línea y espacios
            base64Image = base64Image?.replace("\n", "")?.replace("\r", "")?.replace(" ", "")
            // Puedes agregar un log temporal para depuración
            // Log.d("Base64Debug", "Base64 length: ${base64Image.length}, startsWith: ${base64Image.take(10)}")

            val fileSize = ImageUtils.getFileSize(context, uri)
            val galeriaItem = GaleriaItem(
                imagenBinaria = base64Image,
                nombreArchivo = localImage.nombreArchivo,
                tipoContenido = localImage.tipoContenido,
                tamanoArchivo = fileSize,
                esImagenPrincipal = localImage.esImagenPrincipal
            )
            activityRepository.addGalleryImage(activityId, galeriaItem)
        }
    }

    /**
     * Agregar servicios nuevos usando el endpoint específico en modo edición
     */
    private suspend fun addNewServices(activityId: Int) {
        val currentServices = _activityData.value.servicioEvento.mapNotNull { it.listaServicio }
        val originalServices = _originalServices.value.filterNotNull()
        val newServices = currentServices.filter { it.isNotBlank() && it !in originalServices }
        for (serviceName in newServices) {
            val serviceItem = ServicioItem(listaServicio = serviceName)
            activityRepository.addService(activityId, serviceItem)
        }
    }

    fun saveActivityWithImages(context: Context) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // EXTRAER CIUDAD Y PROVINCIA DEL DESTINO
            var ciudad = ""
            var provincia = ""
            try {
                val destinoJson = com.google.gson.JsonParser.parseString(
                    _activityData.value.ubicacionDestino
                ).asJsonObject
                val address = destinoJson.get("address")?.asString ?: ""
                // Intentar extraer ciudad de la dirección (formato: "calle, ciudad, provincia, país")
                val parts = address.split(",").map { it.trim() }
                if (parts.size >= 3) {
                    ciudad = parts[parts.size - 3]  // Ciudad suele ser el antepenúltimo
                    provincia = parts[parts.size - 2] // Provincia el penúltimo
                } else if (parts.size >= 2) {
                    ciudad = parts[0]
                    provincia = parts[1]
                }
            } catch (e: Exception) {
                // Si falla, dejar vacío
            }

            try {
                val userId = TokenManager.getUserId() ?: 1
                val currentTime = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                if (_isEditMode.value) {
                    val activityId = _editingActivityId.value ?: return@launch

                    // Eliminar servicios marcados antes de actualizar/agregar
                    deleteMarkedServices(activityId)
                    // Eliminar imágenes de galería marcadas antes de actualizar/agregar
                    deleteMarkedGalleryImages(activityId)

                    // Actualizar solo los datos de la actividad (sin galería)
                    val activityRequest = ActivityRequest(
                        usuarioId = userId,
                        titulo = _activityData.value.titulo,
                        descripcion = _activityData.value.descripcion,
                        ubicacionDestino = _activityData.value.ubicacionDestino,
                        ubicacionSalida = _activityData.value.ubicacionSalida,
                        tipoActividad = _activityData.value.tipoActividad.uppercase(Locale.getDefault()),
                        nivelDificultad = _activityData.value.nivelDificultad.uppercase(Locale.getDefault()),
                        precio = _activityData.value.precio,
                        duracion = _activityData.value.duracion,
                        disponibilidad = _activityData.value.diasDisponibles.joinToString(", "),
                        maximoPersonas = _activityData.value.maximoPersonas,
                        minimoPersonas = _activityData.value.minimoPersonas,
                        estadoActividad = "ACTIVA",
                        fechaCreacion = currentTime,
                        fechaActualizacion = currentTime,
                        fechaInicioDisponible = _activityData.value.fechaInicioDisponible,
                        fechaFinDisponible = _activityData.value.fechaFinDisponible,
                        galeria = emptyList(),
                        servicioEvento = _activityData.value.servicioEvento,
                        ciudad = ciudad,
                        provincia = provincia,
                        cuentaBancaria = _activityData.value.cuentaBancaria
                    )

                    val success = updateActivityDataOnly(activityId, activityRequest)
                    if (success) {
                        // Agregar solo imágenes nuevas
                        addNewGalleryImages(context, activityId)
                        // Agregar solo servicios nuevos
                        addNewServices(activityId)
                        _isSuccess.value = true
                        clearData()
                    } else {
                        _error.value = "Error al actualizar la actividad."
                    }
                } else {
                    // CREACIÓN: flujo original, enviar galería completa
                    val galeriaItems = convertLocalImagesToApiFormat(context)
                    val activityRequest = ActivityRequest(
                        usuarioId = userId,
                        titulo = _activityData.value.titulo,
                        descripcion = _activityData.value.descripcion,
                        ubicacionDestino = _activityData.value.ubicacionDestino,
                        ubicacionSalida = _activityData.value.ubicacionSalida,
                        tipoActividad = _activityData.value.tipoActividad.uppercase(Locale.getDefault()),
                        nivelDificultad = _activityData.value.nivelDificultad.uppercase(Locale.getDefault()),
                        precio = _activityData.value.precio,
                        duracion = _activityData.value.duracion,
                        disponibilidad = _activityData.value.diasDisponibles.joinToString(", "),
                        maximoPersonas = _activityData.value.maximoPersonas,
                        minimoPersonas = _activityData.value.minimoPersonas,
                        estadoActividad = "ACTIVA",
                        fechaCreacion = currentTime,
                        fechaActualizacion = currentTime,
                        fechaInicioDisponible = _activityData.value.fechaInicioDisponible,
                        fechaFinDisponible = _activityData.value.fechaFinDisponible,
                        galeria = galeriaItems,
                        servicioEvento = _activityData.value.servicioEvento,
                        ciudad = ciudad,
                        provincia = provincia,
                        cuentaBancaria = _activityData.value.cuentaBancaria
                    )
                    val response = activityRepository.createActivity(activityRequest)
                    if (response.isSuccessful) {
                        _isSuccess.value = true
                        clearData()
                    } else {
                        _error.value = "Error al crear la actividad: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Función auxiliar para convertir LocationData a JSON
    private fun locationDataToJson(location: LocationData): String {
        return Gson().toJson(location)
    }

    // Verificar si el paso current está completo
    fun isCurrentStepComplete(): Boolean {
        return when (_currentStep.value) {
            ActivityCreationStep.BASIC_INFO -> _activityData.value.titulo.isNotBlank() && _activityData.value.descripcion.isNotBlank()
            ActivityCreationStep.DEPARTURE_LOCATION -> _activityData.value.ubicacionSalida.isNotBlank()
            ActivityCreationStep.DESTINATION_LOCATION -> _activityData.value.ubicacionDestino.isNotBlank()
            ActivityCreationStep.ACTIVITY_TYPE -> _activityData.value.tipoActividad.isNotBlank() && _activityData.value.nivelDificultad.isNotBlank()
            ActivityCreationStep.DETAILS -> _activityData.value.precio > 0 &&
                    _activityData.value.duracion.isNotBlank() &&
                    _activityData.value.maximoPersonas > 0 &&
                    _activityData.value.diasDisponibles.isNotEmpty() &&
                    _activityData.value.fechaInicioDisponible.isNotBlank() &&
                    _activityData.value.fechaFinDisponible.isNotBlank()
            ActivityCreationStep.GALLERY -> true // Opcional
            ActivityCreationStep.SERVICES -> true // Opcional
        }
    }

    // Limpiar errores
    fun clearError() {
        _error.value = null
    }

    // Reiniciar formulario
    fun resetForm() {
        _activityData.value = ActivityData()
        _currentStep.value = ActivityCreationStep.BASIC_INFO
        _isSuccess.value = false
        _error.value = null
    }

    fun updateCuentaBancaria(cuentaBancaria: String) {
        _activityData.value = _activityData.value.copy(
            cuentaBancaria = cuentaBancaria
        )
    }
}

// Clase de datos para mantener el estado de la actividad
data class ActivityData(
    val titulo: String = "",
    val descripcion: String = "",
    val ubicacionDestino: String = "",
    val ubicacionSalida: String = "",
    val tipoActividad: String = "",
    val nivelDificultad: String = "",
    val precio: Double = 0.0,
    val duracion: String = "",
    val cuentaBancaria: String = "",
    val maximoPersonas: Int = 1,
    val minimoPersonas: Int = 1,
    val galeria: List<GaleriaItem> = emptyList(),
    val servicioEvento: List<ServicioItem> = emptyList(),
    val diasDisponibles: List<String> = emptyList(),
    val fechaInicioDisponible: String = "",
    val fechaFinDisponible: String = ""
)
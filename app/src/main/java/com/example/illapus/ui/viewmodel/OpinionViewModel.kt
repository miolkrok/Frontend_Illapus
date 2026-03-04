package com.example.illapus.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.illapus.data.api.ApiClient
import com.example.illapus.data.model.OpinionRequest
import com.example.illapus.data.model.OpinionResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class OpinionUiState(
    val opiniones: List<OpinionResponse> = emptyList(),
    val promedioCalificacion: Double = 0.0,
    val totalOpiniones: Int = 0,
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class OpinionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(OpinionUiState())
    val uiState: StateFlow<OpinionUiState> = _uiState.asStateFlow()

    // Estado del formulario
    private val _commentText = MutableStateFlow("")
    val commentText: StateFlow<String> = _commentText.asStateFlow()

    private val _selectedRating = MutableStateFlow(0)
    val selectedRating: StateFlow<Int> = _selectedRating.asStateFlow()

    private val _showError = MutableStateFlow(false)
    val showError: StateFlow<Boolean> = _showError.asStateFlow()

    fun onCommentTextChange(text: String) {
        _commentText.value = text
        _showError.value = false
    }

    fun onRatingChange(rating: Int) {
        _selectedRating.value = rating
        _showError.value = false
    }

    /**
     * Cargar opiniones de una actividad desde el backend
     */
    fun loadOpiniones(actividadId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Cargar opiniones y promedio en paralelo
                val opiniones = ApiClient.opinionService.getOpinionesByActividad(actividadId)
                val promedio = ApiClient.opinionService.getPromedioByActividad (actividadId)

                _uiState.value = _uiState.value.copy(
                    opiniones = opiniones,
                    promedioCalificacion = promedio.promedioPuntuacion ?: 0.0,
                    totalOpiniones = promedio.totalOpiniones ?: 0,
                    isLoading = false
                )
                Log.d("OpinionViewModel", "Cargadas ${opiniones.size} opiniones para actividad $actividadId")
            } catch (e: Exception) {
                Log.e("OpinionViewModel", "Error al cargar opiniones: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar opiniones: ${e.message}"
                )
            }
        }
    }

    /**
     * Enviar una nueva opinión al backend
     */
    fun submitOpinion(actividadId: Int) {
        val text = _commentText.value.trim()
        val rating = _selectedRating.value

        if (text.isBlank() || rating == 0) {
            _showError.value = true
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, error = null)
            try {
                val request = OpinionRequest(
                    actividadId = actividadId,
                    calificacion = rating,
                    comentario = text
                )

                val response = ApiClient.opinionService.createOpinion(request)

                if (response.isSuccessful) {
                    Log.d("OpinionViewModel", "Opinión creada exitosamente")
                    // Limpiar formulario
                    _commentText.value = ""
                    _selectedRating.value = 0
                    _showError.value = false
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        successMessage = "¡Opinión enviada exitosamente!"
                    )
                    // Recargar opiniones para mostrar la nueva
                    loadOpiniones(actividadId)
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("OpinionViewModel", "Error al crear opinión: $errorBody")

                    // Manejar error de duplicado
                    val errorMessage = if (response.code() == 409) {
                        "Ya has opinado sobre esta actividad"
                    } else {
                        "Error al enviar opinión: $errorBody"
                    }

                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        error = errorMessage
                    )
                }
            } catch (e: Exception) {
                Log.e("OpinionViewModel", "Error al enviar opinión: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    error = "Error de conexión: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    companion object {
        /**
         * Formatea la fecha del backend a texto relativo (Hace X días, etc.)
         */
        fun formatTimeAgo(fechaString: String?): String {
            if (fechaString.isNullOrEmpty()) return "Reciente"

            return try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                val fecha = LocalDateTime.parse(fechaString.take(19), formatter)
                val now = LocalDateTime.now()

                val days = ChronoUnit.DAYS.between(fecha, now)

                when {
                    days < 1 -> "Hoy"
                    days < 2 -> "Ayer"
                    days < 7 -> "Hace $days días"
                    days < 30 -> "Hace ${days / 7} semanas"
                    days < 365 -> "Hace ${days / 30} meses"
                    else -> "Hace ${days / 365} años"
                }
            } catch (e: Exception) {
                "Reciente"
            }
        }
    }
}
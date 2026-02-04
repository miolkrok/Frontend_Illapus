package com.example.illapus.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.illapus.data.api.ApiClient
import com.example.illapus.data.model.ReserveCreationResponse
 import com.example.illapus.data.repository.ReservationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReservationsViewModel : BaseViewModel() {

    private val _uiState = MutableStateFlow(ReservationsUiState())
    val uiState: StateFlow<ReservationsUiState> = _uiState.asStateFlow()

    private val reservationRepository = ReservationRepository(ApiClient.reservaService)

    init {
        loadReservations()
    }

    fun loadReservations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            executeApiCall(
                apiCall = { reservationRepository.getMyReservations() },
                onSuccess = { response ->
                    if (response.isSuccessful) {
                        _uiState.value = _uiState.value.copy(
                            reservations = response.body() ?: emptyList(),
                            isLoading = false,
                            error = null
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Error al cargar las reservas"
                        )
                    }
                },
                onError = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error de conexión: ${exception.message}"
                    )
                }
            )
        }
    }

    fun deleteReservation(reservationId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeletingReservation = reservationId)

            executeApiCall(
                apiCall = { reservationRepository.deleteReservation(reservationId) },
                onSuccess = { response ->
                    if (response.isSuccessful) {
                        // Eliminar la reserva de la lista local
                        val updatedReservations = _uiState.value.reservations.filter { it.id != reservationId }
                        _uiState.value = _uiState.value.copy(
                            reservations = updatedReservations,
                            isDeletingReservation = null,
                            successMessage = "Reserva eliminada correctamente"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isDeletingReservation = null,
                            error = "Error al eliminar la reserva"
                        )
                    }
                },
                onError = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isDeletingReservation = null,
                        error = "Error de conexión: ${exception.message}"
                    )
                }
            )
        }
    }

    fun refreshReservations() {
        loadReservations()
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ReservationsUiState(
    val reservations: List<ReserveCreationResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isDeletingReservation: Int? = null,
    val successMessage: String? = null
)

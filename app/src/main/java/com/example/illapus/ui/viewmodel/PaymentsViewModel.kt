package com.example.illapus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.illapus.data.api.ApiClient
import com.example.illapus.utils.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PaymentsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PaymentsUiState())
    val uiState: StateFlow<PaymentsUiState> = _uiState.asStateFlow()

    fun loadAllHostPayments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val userId = TokenManager.getUserId()
                if (userId == null || userId <= 0) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Usuario no autenticado"
                    )
                    return@launch
                }

                // Aquí llamas a tu API para obtener todos los pagos de las actividades del anfitrión
                val response = ApiClient.pagoService.getHostPayments(userId)

                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        payments = response.body() ?: emptyList()
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al cargar pagos: ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error de conexión: ${e.message}"
                )
            }
        }
    }

    /**
     * Cambiar estado del pago (PENDIENTE, APROBADO, RECHAZADO)
     */
    fun updatePaymentStatus(paymentId: Int, newStatus: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = ApiClient.pagoService.updatePagoEstado(
                    paymentId,
                    mapOf("estado" to newStatus)
                )
                if (response.isSuccessful) {
                    val currentPayments = _uiState.value.payments.toMutableList()
                    val index = currentPayments.indexOfFirst { it.id == paymentId }
                    if (index != -1) {
                        val updatedPayment = currentPayments[index].copy(
                            estado = newStatus,
                            estadoPago = newStatus
                        )
                        currentPayments[index] = updatedPayment
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            payments = currentPayments,
                            error = null
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al actualizar estado: ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al actualizar estado: ${e.message}"
                )
            }
        }
    }

    fun refresh() {
        loadAllHostPayments()
    }
}

data class PaymentsUiState(
    val isLoading: Boolean = false,
    val payments: List<Payment> = emptyList(),
    val error: String? = null
)

// Modelo de datos para Payment
data class Payment(
    val id: Int,
    val reservaId: Int,
    val actividadId: Int,
    val actividadTitulo: String?,
    val usuarioId: Int,
    val nombreUsuario: String?,
    val emailUsuario: String?,
    val cantidadPersonas: Int,
    val monto: Double,
    val fechaReserva: String?,
    val estado: String?,
    val estadoPago: String?,
    val imagenComprobante: String?,
    val metodoPago: String?
)
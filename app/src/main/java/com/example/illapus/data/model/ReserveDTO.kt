package com.example.illapus.data.model

import java.math.BigDecimal

data class ReserveDTO(
    val id: Int? = null,
    val actividadId: Int,
    val usuarioId: Int,
    val estado: String = "PENDIENTE",
    val estadoPago: String? = "PENDIENTE",
    val fechaReserva: String, // ISO 8601 format
    val fechaActividad: String, // ISO 8601 format
    val cantidadPersonas: Int,
    val costoTotal: BigDecimal,
    val fechaCreacion: String? = null,
    val fechaActualizacion: String? = null
)

// Modelo para la respuesta del servidor
data class ReserveCreationResponse(
    val id: Int,
    val actividadId: Int,
    val usuarioId: Int,
    val estado: String,
    val fechaReserva: String,
    val estadoPago: String?,
    val fechaActividad: String,
    val cantidadPersonas: Int,
    val costoTotal: BigDecimal,
    val fechaCreacion: String,
    val fechaActualizacion: String
)

data class ReserveCreationWrapper(
    val message: String?,
    val reserva: ReserveCreationResponse?,
    val validacionesExternas: Boolean?
)

data class ReserveApiResponse (
    val message: String,
    val reserva: ReserveCreationResponse,
    val validacionesExternas: Boolean
)

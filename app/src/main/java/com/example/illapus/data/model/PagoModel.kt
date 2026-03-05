package com.example.illapus.data.model

import java.math.BigDecimal

/**
 * Modelo para CREAR un pago
 * El campo imagenComprobante es la imagen en base64.
 */
data class PagoRequest(
    val reservaId: Int,
    val monto: BigDecimal,
    val metodoPago: String = "COMPROBANTE",
    val estado: String = "PENDIENTE",
    val comprobante: String = "",
    val imagenComprobante: String // Imagen en base64 (data:image/jpeg;base64,...)
)

/**
 * Respuesta del backend al crear/obtener un pago.
 */
data class PagoResponse(
    val id: Int?,
    val reservaId: Int?,
    val usuarioId: Int?,
    val monto: BigDecimal?,
    val metodoPago: String?,
    val estado: String?,
    val comprobante: String?,
    val fechaTransaccion: String?,
    val fechaActualizacion: String?
)
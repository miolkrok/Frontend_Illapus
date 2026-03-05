package com.example.illapus.data.model

/**
 * Modelo de datos para la ubicación
 * @property latitude Latitud de la ubicación
 * @property longitude Longitud de la ubicación
 * @property name Nombre opcional de la ubicación
 * @property address Dirección opcional de la ubicación
 */

/**
 * Modelo que representa una opiniÃ³n tal como viene del backend.
 * Coincide con OpinionDTO del backend (app-opiniones).
 */
data class OpinionResponse(
    val id: Int?,
    val actividadId: Int?,
    val usuarioId: Int?,
    val calificacion: Int?,
    val comentario: String?,
    val fechaCreacion: String?,
    val fechaActualizacion: String?,
    val nombreUsuario: String?,
    val tituloActividad: String?
)

/**
 * Modelo para CREAR una opiniÃ³n (lo que se envÃ­a al backend).
 * El backend obtiene el usuarioId del JWT automÃ¡ticamente.
 */
data class OpinionRequest(
    val actividadId: Int,
    val calificacion: Int,
    val comentario: String
)

/**
 * Respuesta del backend al crear una opiniÃ³n exitosamente.
 */
data class OpinionCreateResponse(
    val message: String?,
    val opinion: OpinionResponse?,
    val validacionesExternas: Boolean?
)

/**
 * Respuesta del endpoint de promedio de calificaciÃ³n.
 */
data class PromedioOpinionResponse(
    val actividadId: Int?,
    val promedioPuntuacion: Double?,
    val totalOpiniones: Int?
)
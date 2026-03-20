package com.example.illapus.data.model

import com.google.android.gms.maps.model.LatLng

/**
 * Modelo que representa los detalles de una propiedad o alojamiento
 *
 * @property id Identificador único
 * @property title Título o nombre de la propiedad
 * @property description Descripción detallada
 * @property images Lista de URLs de las imágenes
 * @property rating Calificación en estrellas (0-5)
 * @property price Precio por persona
 * @property location Ubicación de la propiedad
 * @property host Información del anfitrión
 * @property startingPoint Punto de partida o información adicional
 * @property availableDate Fecha disponible para reserva
 */
data class PropertyDetailsModel(
    val id: String,
    val title: String,
    val description: String,
    val providerId: Int = 0,
    val images: List<String>, // Lista de imágenes Base64
    val rating: Float = 0f, // Por ahora no viene en la API
    val price: Double,
    val departureLocation: LocationModel, // Ubicación de salida
    val destinationLocation: LocationModel, // Ubicación de destino
    val host: HostModel,
    val duration: String,
    val cuentaBancaria: String = "",
    val availability: String,
    val activityType: String,
    val difficultyLevel: String,
    val minPeople: Int,
    val maxPeople: Int,
    val services: List<String>, // Lista de servicios incluidos
    val startDate: String? = null,
    val endDate: String? = null
)

/**
 * Modelo que representa la información del anfitrión
 *
 * @property name Nombre del anfitrión
 * @property yearsActive Años como anfitrión
 */
data class HostModel(
    val name: String,
    val yearsActive: Int
)

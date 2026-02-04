package com.example.illapus.data.model

/**
 * Modelo de datos para la ubicación
 * @property latitude Latitud de la ubicación
 * @property longitude Longitud de la ubicación
 * @property name Nombre opcional de la ubicación
 * @property address Dirección opcional de la ubicación
 */
data class LocationModel(
    val latitude: Double,
    val longitude: Double,
    val name: String = "",
    val address: String = ""
)

package com.example.illapus.utils

import android.util.Log
import com.example.illapus.data.model.LocationModel
import com.google.gson.Gson
import com.google.gson.JsonObject

/**
 * Utilidades para el manejo de ubicaciones y datos geográficos
 */
object LocationUtils {

    /**
     * Parsea un JSON string de ubicación y lo convierte a LocationModel
     */
    fun parseLocationFromJson(locationJson: String?): LocationModel? {
        return try {
            if (locationJson.isNullOrEmpty()) return null

            val gson = Gson()
            val jsonObject = gson.fromJson(locationJson, JsonObject::class.java)

            LocationModel(
                latitude = jsonObject.get("latitude")?.asDouble ?: 0.0,
                longitude = jsonObject.get("longitude")?.asDouble ?: 0.0,
                name = jsonObject.get("name")?.asString ?: "",
                address = jsonObject.get("address")?.asString ?: "",
                ciudad = jsonObject.get("ciudad")?.asString ?: "",
                provincia = jsonObject.get("provincia")?.asString ?: ""
            )
        } catch (e: Exception) {
            Log.e("LocationUtils", "Error parsing location JSON: $e")
            null
        }
    }

    /**
     * Calcula la distancia entre dos ubicaciones en kilómetros usando la fórmula de Haversine
     */
    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val R = 6371 // Radio de la Tierra en kilómetros
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }

    /**
     * Valida si las coordenadas son válidas
     */
    fun isValidCoordinate(latitude: Double, longitude: Double): Boolean {
        return latitude in -90.0..90.0 && longitude in -180.0..180.0
    }

    /**
     * Formatea coordenadas para mostrar en UI
     */
    fun formatCoordinates(latitude: Double, longitude: Double): String {
        val latDirection = if (latitude >= 0) "N" else "S"
        val lonDirection = if (longitude >= 0) "E" else "W"
        return "${String.format("%.4f", Math.abs(latitude))}°$latDirection, ${String.format("%.4f", Math.abs(longitude))}°$lonDirection"
    }
}

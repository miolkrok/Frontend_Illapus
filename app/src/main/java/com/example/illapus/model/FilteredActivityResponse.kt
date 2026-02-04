package com.example.illapus.model

import com.google.gson.annotations.SerializedName

data class FilteredActivityResponse(
    val id: Int,
    @SerializedName("actividadId")
    val actividadId: Int,
    @SerializedName("titulo")
    val titulo: String,
    @SerializedName("descripcion")
    val descripcion: String,
    @SerializedName("ubicacion")
    val ubicacion: String,
    @SerializedName("categoria")
    val categoria: String,
    @SerializedName("precio")
    val precio: Double,
    @SerializedName("capacidad")
    val capacidad: Int?,
    @SerializedName("duracion")
    val duracion: String,
    @SerializedName("tipoActividad")
    val tipoActividad: String,
    @SerializedName("nivelDificultad")
    val nivelDificultad: String,
    @SerializedName("proveedorId")
    val proveedorId: Int,
    @SerializedName("nombreProveedor")
    val nombreProveedor: String?,
    @SerializedName("puntuacionPromedio")
    val puntuacionPromedio: Double,
    @SerializedName("provincia")
    val provincia: String,
    @SerializedName("ciudad")
    val ciudad: String,
    @SerializedName("fechaInicioDisponible")
    val fechaInicioDisponible: String,
    @SerializedName("fechaFinDisponible")
    val fechaFinDisponible: String,
    @SerializedName("minimoPersonas")
    val minimoPersonas: Int,
    @SerializedName("maximoPersonas")
    val maximoPersonas: Int,
    @SerializedName("latitud")
    val latitud: Double?,
    @SerializedName("longitud")
    val longitud: Double?,
    @SerializedName("estadoActividad")
    val estadoActividad: String,
    @SerializedName("fechaIndexacion")
    val fechaIndexacion: String,
    @SerializedName("numeroReservas")
    val numeroReservas: Int,
    @SerializedName("numeroOpiniones")
    val numeroOpiniones: Int
) {
    // Función para mapear a Property
    fun toProperty(): Property {
        return Property(
            id = actividadId,
            title = titulo,
            image = null, // Se deja en blanco como se solicitó
            price = precio,
            rating = puntuacionPromedio
        )
    }
}

package com.example.illapus.data.model

import com.google.gson.annotations.SerializedName

data class ActivityRequest(
    @SerializedName("usuarioId") val usuarioId: Int,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("ubicacionDestino") val ubicacionDestino: String,
    @SerializedName("ubicacionSalida") val ubicacionSalida: String,
    @SerializedName("tipoActividad") val tipoActividad: String,
    @SerializedName("nivelDificultad") val nivelDificultad: String,
    @SerializedName("precio") val precio: Double,
    @SerializedName("duracion") val duracion: String,
    @SerializedName("disponibilidad") val disponibilidad: String,
    @SerializedName("maximoPersonas") val maximoPersonas: Int,
    @SerializedName("minimoPersonas") val minimoPersonas: Int,
    @SerializedName("estadoActividad") val estadoActividad: String,
    @SerializedName("fechaCreacion") val fechaCreacion: String,
    @SerializedName("fechaActualizacion") val fechaActualizacion: String,
    @SerializedName("fechaInicioDisponible") val fechaInicioDisponible: String,
    @SerializedName("fechaFinDisponible") val fechaFinDisponible: String,
    @SerializedName("galeria") val galeria: List<GaleriaItem>,
    @SerializedName("servicioEvento") val servicioEvento: List<ServicioItem>,

    @SerializedName("ciudad") val ciudad: String = "",
    @SerializedName("provincia") val provincia: String = "",

    @SerializedName("cuentaBancaria") val cuentaBancaria: String = ""
)

data class ActivityResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("usuarioId") val usuarioId: Int,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("ubicacionDestino") val ubicacionDestino: String,
    @SerializedName("ubicacionSalida") val ubicacionSalida: String,
    @SerializedName("tipoActividad") val tipoActividad: String,
    @SerializedName("nivelDificultad") val nivelDificultad: String,
    @SerializedName("precio") val precio: Double,
    @SerializedName("duracion") val duracion: String,
    @SerializedName("disponibilidad") val disponibilidad: String,
    @SerializedName("maximoPersonas") val maximoPersonas: Int,
    @SerializedName("minimoPersonas") val minimoPersonas: Int,
    @SerializedName("estadoActividad") val estadoActividad: String,
    @SerializedName("fechaCreacion") val fechaCreacion: String,
    @SerializedName("fechaActualizacion") val fechaActualizacion: String,
    @SerializedName("fechaInicioDisponible") val fechaInicioDisponible: String,
    @SerializedName("fechaFinDisponible") val fechaFinDisponible: String,
    @SerializedName("galeria") val galeria: List<GaleriaResponse>,
    @SerializedName("servicioEvento") val servicioEvento: List<ServicioResponse>,
    @SerializedName("cuentaBancaria") val cuentaBancaria: String? = null,
)

data class GaleriaItem(
    @SerializedName("imagenBinaria") val imagenBinaria: String?, // Base64 encoded image
    @SerializedName("urlFoto") val urlFoto: String? = null,
    @SerializedName("nombreArchivo") val nombreArchivo: String,
    @SerializedName("tipoContenido") val tipoContenido: String,
    @SerializedName("tamanoArchivo") val tamanoArchivo: Long,
    @SerializedName("esImagenPrincipal") val esImagenPrincipal: Boolean
)

data class GaleriaResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("urlFoto") val urlFoto: String?,
    @SerializedName("imagenBinaria") val imagenBinaria: String?,
    @SerializedName("nombreArchivo") val nombreArchivo: String,
    @SerializedName("tipoContenido") val tipoContenido: String,
    @SerializedName("tamanoArchivo") val tamanoArchivo: Long,
    @SerializedName("esImagenPrincipal") val esImagenPrincipal: Boolean
)

data class ServicioItem(
    @SerializedName("listaServicio") val listaServicio: String
)

data class ServicioResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("listaServicio") val listaServicio: String
)

// Clase para manejar imágenes localmente antes de enviar
data class LocalImageData(
    val uri: String,
    val nombreArchivo: String,
    val tipoContenido: String,
    val esImagenPrincipal: Boolean = false,
    val base64Image: String? = null // Para imágenes existentes en modo edición
)

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val address: String
)

// Estados del flujo de creación de actividades
enum class ActivityCreationStep {
    BASIC_INFO,        // Título y descripción
    DEPARTURE_LOCATION, // Ubicación de salida
    DESTINATION_LOCATION, // Ubicación de destino
    ACTIVITY_TYPE,     // Tipo de actividad y nivel
    DETAILS,          // Precio, duración, etc.
    GALLERY,          // Fotos
    SERVICES          // Servicios adicionales
}

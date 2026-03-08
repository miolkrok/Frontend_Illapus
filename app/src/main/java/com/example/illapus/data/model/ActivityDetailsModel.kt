package com.example.illapus.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo para los detalles completos de una actividad
 */
data class ActivityDetailsModel(
    val id: Int,
    @SerializedName("proveedorId")
    val providerId: Int,
    @SerializedName("titulo")
    val title: String,
    @SerializedName("descripcion")
    val description: String,
    @SerializedName("ubicacionDestino")
    val destinationLocationJson: String,
    @SerializedName("ubicacionSalida")
    val departureLocationJson: String,
    @SerializedName("tipoActividad")
    val activityType: String,
    @SerializedName("nivelDificultad")
    val difficultyLevel: String,
    @SerializedName("precio")
    val price: Double,
    @SerializedName("duracion")
    val duration: String,
    @SerializedName("disponibilidad")
    val availability: String,
    @SerializedName("fechaInicioDisponible")
    val startDate: String,
    @SerializedName("fechaFinDisponible")
    val endDate: String,
    @SerializedName("minimoPersonas")
    val minPeople: Int,
    @SerializedName("maximoPersonas")
    val maxPeople: Int,
    val provincia: String,
    val ciudad: String,
    @SerializedName("estadoActividad")
    val activityStatus: String,
    @SerializedName("fechaCreacion")
    val createdAt: String,
    @SerializedName("fechaActualizacion")
    val updatedAt: String,
    val galeria: List<GalleryImage>,
    @SerializedName("servicioEvento")
    val services: List<ActivityService>,


    // Información del usuario (nuevos campos)
    @SerializedName("nombreUsuario")
    val nombreUsuario: String? = null,
    @SerializedName("apellidoUsuario")
    val apellidoUsuario: String? = null,
    @SerializedName("emailUsuario")
    val emailUsuario: String? = null,
    @SerializedName("fechaRegistroUsuario")
    val fechaRegistroUsuario: String? = null,

    // Información del proveedor
    @SerializedName("nombreProveedor")
    val nombreProveedor: String? = null,
    @SerializedName("descripcionProveedor")
    val descripcionProveedor: String? = null,

    @SerializedName("cuentaBancaria")
    val cuentaBancaria: String,


    )

/**
 * Modelo para las imágenes de la galería
 */
data class GalleryImage(
    val id: Int,
    @SerializedName("actividadId")
    val activityId: Int,
    @SerializedName("urlFoto")
    val urlFoto: String?,
    @SerializedName("imagenBinaria")
    val imageBinary: String?,
    @SerializedName("nombreArchivo")
    val fileName: String,
    @SerializedName("tipoContenido")
    val contentType: String,
    @SerializedName("tamanoArchivo")
    val fileSize: Long,
    @SerializedName("esImagenPrincipal")
    val isPrimaryImage: Boolean
){
    val displayImage: String?
        get() = if (!urlFoto.isNullOrEmpty())
            urlFoto else imageBinary
}


/**
 * Modelo para los servicios incluidos en la actividad
 */
data class ActivityService(
    val id: Int,
    @SerializedName("listaServicio")
    val serviceName: String,
    @SerializedName("actividadId")
    val activityId: Int
)

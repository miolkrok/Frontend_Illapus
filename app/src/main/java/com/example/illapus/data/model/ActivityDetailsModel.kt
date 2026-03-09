package com.example.illapus.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo para los detalles completos de una actividad
 */
data class ActivityDetailsModel(
    val id: Int = 0,
    @SerializedName("proveedorId")
    val providerId: Int = 0,
    @SerializedName("titulo")
    val title: String = "",
    @SerializedName("descripcion")
    val description: String = "",
    @SerializedName("ubicacionDestino")
    val destinationLocationJson: String = "",
    @SerializedName("ubicacionSalida")
    val departureLocationJson: String = "",
    @SerializedName("tipoActividad")
    val activityType: String = "",
    @SerializedName("nivelDificultad")
    val difficultyLevel: String = "",
    @SerializedName("precio")
    val price: Double = 0.0,
    @SerializedName("duracion")
    val duration: String = "",
    @SerializedName("disponibilidad")
    val availability: String = "",
    @SerializedName("fechaInicioDisponible")
    val startDate: String? = null,
    @SerializedName("fechaFinDisponible")
    val endDate: String? = null,
    @SerializedName("minimoPersonas")
    val minPeople: Int = 0,
    @SerializedName("maximoPersonas")
    val maxPeople: Int = 0,
    val provincia: String = "",
    val ciudad: String = "",
    @SerializedName("estadoActividad")
    val activityStatus: String = "",
    @SerializedName("fechaCreacion")
    val createdAt: String? = null,
    @SerializedName("fechaActualizacion")
    val updatedAt: String? = null,
    val galeria: List<GalleryImage>? = null,
    @SerializedName("servicioEvento")
    val services: List<ActivityService>? = null,


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
    val cuentaBancaria: String = "",


    )

/**
 * Modelo para las imágenes de la galería
 */
data class GalleryImage(
    val id: Int = 0,
    @SerializedName("actividadId")
    val activityId: Int = 0,
    @SerializedName("urlFoto")
    val urlFoto: String? = null,
    @SerializedName("imagenBinaria")
    val imageBinary: String? = null,
    @SerializedName("nombreArchivo")
    val fileName: String = "",
    @SerializedName("tipoContenido")
    val contentType: String = "",
    @SerializedName("tamanoArchivo")
    val fileSize: Long = 0L,
    @SerializedName("esImagenPrincipal")
    val isPrimaryImage: Boolean = false
){
    val displayImage: String?
        get() = if (!urlFoto.isNullOrEmpty())
            urlFoto else imageBinary
}


/**
 * Modelo para los servicios incluidos en la actividad
 */
data class ActivityService(
    val id: Int = 0,
    @SerializedName("listaServicio")
    val serviceName: String = "",
    @SerializedName("actividadId")
    val activityId: Int = 0
)

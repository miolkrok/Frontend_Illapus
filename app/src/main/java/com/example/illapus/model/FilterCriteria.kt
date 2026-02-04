package com.example.illapus.model

data class FilterCriteria(
    val ubicacion: String = "",
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val personas: Int = 0,
    val tipo: String = "",
    val precioMin: Double = 0.0,
    val precioMax: Double = 0.0,
    val lat: Double? = null,
    val lng: Double? = null,
    val radio: Double = 0.0
) {
    fun isEmpty(): Boolean {
        return ubicacion.isBlank() &&
                fechaInicio.isBlank() &&
                fechaFin.isBlank() &&
                tipo.isBlank() &&
                precioMin == 0.0 &&
                precioMax == 0.0 &&
                personas == 0 &&
                radio == 0.0
    }

    fun hasAnyFilter(): Boolean {
        return !isEmpty()
    }

    fun toQueryString(): String {
        val params = mutableListOf<String>()

        if (ubicacion.isNotBlank()) params.add("ubicacion=$ubicacion")
        if (fechaInicio.isNotBlank()) params.add("fechaInicio=$fechaInicio")
        if (fechaFin.isNotBlank()) params.add("fechaFin=$fechaFin")
        if (personas != 0) params.add("personas=$personas")
        if (tipo.isNotBlank()) params.add("tipo=$tipo")
        if (precioMin != 0.0) params.add("precioMin=$precioMin")
        if (precioMax != 0.0) params.add("precioMax=$precioMax")
        if (lat != null) params.add("lat=$lat")
        if (lng != null) params.add("lng=$lng")
        if (radio != 0.0) params.add("radio=$radio")

        return if (params.isNotEmpty()) "?${params.joinToString("&")}" else ""
    }
}

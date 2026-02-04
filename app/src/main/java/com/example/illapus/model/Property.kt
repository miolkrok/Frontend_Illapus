package com.example.illapus.model

import com.google.gson.annotations.SerializedName

data class Property(
    val id: Int,
    @SerializedName("titulo")
    val title: String,
    @SerializedName("imagen")
    val image: String?, // Ahora puede ser null
    @SerializedName("precio")
    val price: Double, // Cambiar a Double para coincidir con el JSON
    val rating: Double
)

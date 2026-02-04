package com.example.illapus.data.model

data class UserInfoResponse(
    val userId: Int,
    val email: String,
    val nombre: String,
    val apellido: String,
    val direccion: String?,
    val imagenPerfil: String?,
    val roles: List<String>
)

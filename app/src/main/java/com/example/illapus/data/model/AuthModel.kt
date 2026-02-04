package com.example.illapus.data.model

data class LoginRequest(
    val email: String,
    val password: String
)

data class Usuario(
    val id: Int = 0,
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val password: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    val rol: String = "",
    val fechaCreacion: String? = null,
    val fechaActualizacion: String? = null,
    val proveedor: String? = null
)

// Esta clase representa la respuesta de la API al iniciar sesión.
data class LoginResponse(
    val accessToken: String = "",
    val refreshToken: String = "",
    val tokenType: String = "Bearer",
    val expiresIn: Int = 0,
    val usuario: Usuario = Usuario()
)

// Modelo para la solicitud de registro
data class RegisterRequest(
    val nombre: String,
    val apellido: String,
    val email: String,
    val password: String,
    val rol: String = "CLIENTE" // Los roles válidos son CLIENTE, PROVEEDOR o ADMIN
)

// Respuesta del registro
data class RegisterResponse(
    val id: Int? = null,
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val rol: String = "",
    val success: Boolean = true,
    val message: String = "Registro exitoso"
)

package com.example.illapus.data.repository

import android.util.Log
import com.example.illapus.data.api.ApiClient
import com.example.illapus.data.model.LoginRequest
import com.example.illapus.data.model.LoginResponse
import com.example.illapus.data.model.RegisterRequest
import com.example.illapus.data.model.RegisterResponse
import com.example.illapus.utils.TokenManager
import retrofit2.HttpException

class AuthRepository {
    // Método para login con la API
    suspend fun login(email: String, password: String): Result<LoginResponse> {
        return try {
            Log.d("AuthRepository", "Intentando login con usuario: $email")
            val response = ApiClient.authApiService.login(LoginRequest(email = email, password = password))
            Log.d("AuthRepository", "Respuesta de API recibida: $response")

            // Guardar token de autenticación
            TokenManager.saveAuthToken(response.accessToken)
            TokenManager.saveRefreshToken(response.refreshToken)
            TokenManager.saveTokenType(response.tokenType)

            Result.success(response)
        } catch (e: HttpException) {
            // Captura específicamente errores HTTP para logear más información
            Log.e("AuthRepository", "Error HTTP en login: ${e.code()} - ${e.message()}")
            if (e.response()?.errorBody() != null) {
                try {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("AuthRepository", "Cuerpo del error: $errorBody")
                } catch (ex: Exception) {
                    Log.e("AuthRepository", "No se pudo leer el cuerpo del error: ${ex.message}")
                }
            }
            Result.failure(Exception("Error al iniciar sesión: ${e.message()}"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en login API", e)
            Result.failure(Exception("Error al conectar con el servidor: ${e.message}"))
        }
    }

    // Método para registrar un nuevo usuario
    suspend fun register(nombre: String, apellido: String, email: String, password: String, rol: String = "CLIENTE"): Result<RegisterResponse> {
        return try {
            Log.d("AuthRepository", "Intentando registrar usuario: $email")
            val response = ApiClient.authApiService.register(
                RegisterRequest(
                    nombre = nombre,
                    apellido = apellido,
                    email = email,
                    password = password,
                    rol = rol
                )
            )
            Log.d("AuthRepository", "Respuesta de API recibida: $response")
            Result.success(response)
        } catch (e: HttpException) {
            // Captura específicamente errores HTTP para logear más información
            Log.e("AuthRepository", "Error HTTP en registro: ${e.code()} - ${e.message()}")
            if (e.response()?.errorBody() != null) {
                try {
                    val errorBody = e.response()?.errorBody()?.string()
                    Log.e("AuthRepository", "Cuerpo del error: $errorBody")
                } catch (ex: Exception) {
                    Log.e("AuthRepository", "No se pudo leer el cuerpo del error: ${ex.message}")
                }
            }
            Result.failure(Exception("Error al registrar usuario: ${e.message()}"))
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error en registro API", e)
            Result.failure(Exception("Error al conectar con el servidor: ${e.message}"))
        }
    }
}

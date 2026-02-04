package com.example.illapus.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.illapus.data.api.ApiClient
import com.example.illapus.utils.TokenManager
import com.example.illapus.utils.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ProfileViewModel : BaseViewModel() {

    // Estado UI para la pantalla de perfil
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val authApiService = ApiClient.authApiService

    init {
        // Cargamos los datos del usuario desde la API
        loadUserProfile()
    }

    private fun loadUserProfile() {
        // Verificar que existe un token de autenticación
        if (TokenManager.getAuthToken().isNullOrEmpty()) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "No hay sesión activa. Por favor inicie sesión nuevamente."
                )
            }
            // Activar la expiración de sesión si no hay token
            AuthManager.handleSessionExpired()
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        // Usar el método del BaseViewModel para manejo automático de errores
        launchApiCall(
            apiCall = { authApiService.getUserInfo() },
            onSuccess = { userInfo ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        username = "${userInfo.nombre} ${userInfo.apellido}",
                        email = userInfo.email,
                        location = userInfo.direccion ?: "Sin dirección especificada",
                        profileImageUrl = userInfo.imagenPerfil,
                        isProveedor = userInfo.roles.contains("PROVEEDOR"),
                        errorMessage = null
                    )
                }
            },
            onError = { exception ->
                val errorMsg = when (exception) {
                    is HttpException -> when (exception.code()) {
                        401 -> "Sesión expirada. Redirigiendo al login..."
                        403 -> "No tiene permisos para acceder a esta información."
                        404 -> "Información de usuario no encontrada."
                        else -> "Error al cargar los datos del perfil: ${exception.message()}"
                    }
                    is IOException -> "Error de conexión. Verifique su conexión a internet."
                    else -> "Error al cargar los datos: ${exception.localizedMessage}"
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = errorMsg
                    )
                }
            }
        )
    }

    // Función para reintentar la carga del perfil
    fun retryLoadProfile() {
        loadUserProfile()
    }

    fun showLogoutConfirmation() {
        _uiState.update { it.copy(showLogoutDialog = true) }
    }

    fun dismissLogoutConfirmation() {
        _uiState.update { it.copy(showLogoutDialog = false) }
    }

    fun logout(): Boolean {
        // Usar AuthManager para manejar el logout
        AuthManager.logout()
        return true // Devuelve true si el cierre de sesión fue exitoso
    }
}

// Clase que representa el estado de la UI para la pantalla de perfil
data class ProfileUiState(
    val isLoading: Boolean = false,
    val username: String = "",
    val email: String = "",
    val location: String = "",
    val profileImageUrl: String? = null,
    val showLogoutDialog: Boolean = false,
    val errorMessage: String? = null,
    val isProveedor: Boolean = false
)

package com.example.illapus.ui.viewmodel

import android.util.Log
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
                        location = userInfo.direccion ?: " ",
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

    // Convertir a operador turístico (PROVEEDOR)

    /**
     * Muestra el diálogo de confirmación para convertirse en operador turístico
     */
    fun showConvertirProveedorDialog() {
        _uiState.update { it.copy(showConvertirDialog = true) }
    }

    fun dismissConvertirProveedorDialog() {
        _uiState.update { it.copy(showConvertirDialog = false) }
    }

    /**
     * Llama al backend para cambiar el rol de CLIENTE a PROVEEDOR
     */
    fun convertirAProveedor() {
        _uiState.update { it.copy(isConvertingToProveedor = true, showConvertirDialog = false) }

        viewModelScope.launch {
            try {
                val response = ApiClient.usuarioService.convertirAProveedor()

                if (response.isSuccessful) {
                    Log.d("ProfileViewModel", "Usuario convertido a PROVEEDOR exitosamente")
                    _uiState.update {
                        it.copy(
                            isConvertingToProveedor = false,
                            requiresRelogin = true
                        )
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    Log.e("ProfileViewModel", "Error al convertir: $errorBody")

                    val errorMsg = if (response.code() == 409) {
                        "Ya eres operador turístico"
                    } else {
                        "Error al cambiar rol: $errorBody"
                    }

                    _uiState.update {
                        it.copy(
                            isConvertingToProveedor = false,
                            errorMessage = errorMsg
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error: ${e.message}")
                _uiState.update {
                    it.copy(
                        isConvertingToProveedor = false,
                        errorMessage = "Error de conexión: ${e.message}"
                    )
                }
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
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
    val isProveedor: Boolean = false,

    val requiresRelogin: Boolean = false,
    val showConvertirDialog: Boolean = false,
    val isConvertingToProveedor: Boolean = false,
    val successMessage: String? = null
)

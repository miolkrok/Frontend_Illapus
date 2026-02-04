package com.example.illapus.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.illapus.data.repository.AuthRepository
import com.example.illapus.utils.TokenManager
import com.example.illapus.utils.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    data class LoginUiState(
        val email: String = "admin3@admin.com",
        val password: String = "Password123*",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isLoggedIn: Boolean = false,
        val userInfo: UserInfo? = null
    )

    data class UserInfo(
        val id: Int = 0,
        val nombre: String = "",
        val apellido: String = "",
        val email: String = "",
        val rol: String = ""
    )

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.update { currentState ->
            currentState.copy(email = email)
        }
    }

    fun updatePassword(password: String) {
        _uiState.update { currentState ->
            currentState.copy(password = password)
        }
    }

    fun login() {
        // Validamos que haya un email y contraseña
        if (_uiState.value.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El correo no puede estar vacío") }
            return
        }

        if (_uiState.value.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "La contraseña no puede estar vacía") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            authRepository.login(_uiState.value.email, _uiState.value.password)
                .onSuccess { response ->
                    // Registrar la respuesta en el log para verificar
                    Log.d("LoginViewModel", "Login exitoso: $response")

                    // Guardar información adicional del usuario
                    val usuario = response.usuario
                    TokenManager.saveUserId(usuario.id)
                    TokenManager.saveUserRole(usuario.rol)

                    // Actualizar estado de autenticación en AuthManager
                    AuthManager.setAuthenticated(true)

                    // Actualizar el estado de la UI con la información del usuario
                    val userInfo = UserInfo(
                        id = usuario.id,
                        nombre = usuario.nombre,
                        apellido = usuario.apellido,
                        email = usuario.email,
                        rol = usuario.rol
                    )

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            errorMessage = null,
                            userInfo = userInfo
                        )
                    }
                }
                .onFailure { error ->
                    Log.e("LoginViewModel", "Error de login", error)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error al iniciar sesión"
                        )
                    }
                }
        }
    }

    fun loginWithFacebook() {
        // Simulación - se implementará en el futuro
        _uiState.update { it.copy(errorMessage = "Inicio de sesión con Facebook aún no implementado") }
    }

    fun loginWithGoogle() {
        // Simulación - se implementará en el futuro
        _uiState.update { it.copy(errorMessage = "Inicio de sesión con Google aún no implementado") }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

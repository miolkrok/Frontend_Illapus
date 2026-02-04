package com.example.illapus.ui.viewmodel

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.illapus.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    data class RegisterUiState(
        val nombre: String = "",
        val apellido: String = "",
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isRegistered: Boolean = false,
        val autoLoginSuccess: Boolean = false
    )

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun updateName(nombre: String) {
        _uiState.update { it.copy(nombre = nombre) }
    }

    fun updateLastName(apellido: String) {
        _uiState.update { it.copy(apellido = apellido) }
    }

    fun updateEmail(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword) }
    }

    fun register() {
        // Validar campos
        if (_uiState.value.nombre.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El nombre no puede estar vacío") }
            return
        }

        if (_uiState.value.apellido.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El apellido no puede estar vacío") }
            return
        }

        if (_uiState.value.email.isBlank()) {
            _uiState.update { it.copy(errorMessage = "El correo no puede estar vacío") }
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(_uiState.value.email).matches()) {
            _uiState.update { it.copy(errorMessage = "El formato del correo es inválido") }
            return
        }

        if (_uiState.value.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "La contraseña no puede estar vacía") }
            return
        }

        if (_uiState.value.password.length < 8) {
            _uiState.update { it.copy(errorMessage = "La contraseña debe tener al menos 8 caracteres") }
            return
        }

        if (_uiState.value.password != _uiState.value.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Las contraseñas no coinciden") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            authRepository.register(
                nombre = _uiState.value.nombre,
                apellido = _uiState.value.apellido,
                email = _uiState.value.email,
                password = _uiState.value.password
            ).onSuccess { response ->
                Log.d("RegisterViewModel", "Registro exitoso: $response")

                // Después de registrar exitosamente, intentar iniciar sesión automáticamente
                autoLoginAfterRegistration()

            }.onFailure { error ->
                Log.e("RegisterViewModel", "Error de registro", error)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al registrar usuario"
                    )
                }
            }
        }
    }

    // Método para iniciar sesión automáticamente después del registro
    private fun autoLoginAfterRegistration() {
        viewModelScope.launch {
            try {
                val email = _uiState.value.email
                val password = _uiState.value.password

                authRepository.login(email, password)
                    .onSuccess {
                        Log.d("RegisterViewModel", "Inicio de sesión automático exitoso")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRegistered = true,
                                autoLoginSuccess = true,
                                errorMessage = null
                            )
                        }
                    }
                    .onFailure { error ->
                        Log.e("RegisterViewModel", "Error en inicio de sesión automático", error)
                        // Aunque el login automático falle, el registro fue exitoso
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isRegistered = true,
                                autoLoginSuccess = false,
                                errorMessage = null
                            )
                        }
                    }
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Error inesperado en inicio de sesión automático", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRegistered = true,
                        autoLoginSuccess = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

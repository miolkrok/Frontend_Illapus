package com.example.illapus.utils

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Gestor global de autenticación que maneja el estado de la sesión
 * y proporciona callbacks para manejar la expiración automática
 */
object AuthManager {
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _shouldNavigateToLogin = MutableStateFlow<Boolean?>(null)
    val shouldNavigateToLogin: StateFlow<Boolean?> = _shouldNavigateToLogin.asStateFlow()

    // Callback que se ejecuta cuando la sesión expira
    private var onSessionExpiredCallback: (() -> Unit)? = null

    fun initialize() {
        // Verificar el estado inicial de autenticación
        _isAuthenticated.value = TokenManager.isLoggedIn()
        Log.d("AuthManager", "AuthManager inicializado. Autenticado: ${_isAuthenticated.value}")
    }

    fun setAuthenticated(isAuth: Boolean) {
        _isAuthenticated.value = isAuth
        Log.d("AuthManager", "Estado de autenticación cambiado a: $isAuth")
    }

    fun setOnSessionExpiredCallback(callback: () -> Unit) {
        onSessionExpiredCallback = callback
    }

    /**
     * Maneja la expiración de sesión automáticamente
     * Limpia tokens y notifica a la UI
     */
    fun handleSessionExpired() {
        Log.w("AuthManager", "Sesión expirada detectada")

        // Limpiar tokens
        TokenManager.clearAuth()

        // Actualizar estado
        _isAuthenticated.value = false

        // Notificar que debe navegar al login
        _shouldNavigateToLogin.value = true

        // Ejecutar callback si está definido
        onSessionExpiredCallback?.invoke()
    }

    /**
     * Marca que ya se procesó la navegación al login
     */
    fun onNavigatedToLogin() {
        _shouldNavigateToLogin.value = null
    }

    /**
     * Limpia la sesión manualmente (logout voluntario)
     */
    fun logout() {
        Log.d("AuthManager", "Logout manual ejecutado")
        TokenManager.clearAuth()
        _isAuthenticated.value = false
        _shouldNavigateToLogin.value = true
    }
}

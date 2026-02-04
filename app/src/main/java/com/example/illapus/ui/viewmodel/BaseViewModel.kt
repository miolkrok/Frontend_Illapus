package com.example.illapus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.illapus.utils.AuthManager
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * ViewModel base que maneja automáticamente la expiración de sesión
 * Todos los ViewModels que hagan llamadas a la API deben heredar de este
 */
abstract class BaseViewModel : ViewModel() {

    /**
     * Ejecuta una llamada a la API con manejo automático de errores de autenticación
     * Si recibe un 401, activa automáticamente la expiración de sesión
     */
    protected suspend fun <T> executeApiCall(
        apiCall: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        try {
            val result = apiCall()
            onSuccess(result)
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> {
                    // El interceptor ya manejó la expiración automática
                    // Solo necesitamos propagar el error para que la UI lo maneje
                    onError(e)
                }
                else -> {
                    onError(e)
                }
            }
        } catch (e: Exception) {
            onError(e)
        }
    }

    /**
     * Versión con corrutinas para usar en viewModelScope
     */
    protected fun <T> launchApiCall(
        apiCall: suspend () -> T,
        onSuccess: (T) -> Unit,
        onError: (Exception) -> Unit = {}
    ) {
        viewModelScope.launch {
            executeApiCall(apiCall, onSuccess, onError)
        }
    }
}

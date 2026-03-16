package com.example.illapus.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

/**
 * Clase utilitaria para manejar el token de autenticación.
 * Permite guardar y recuperar el token de autenticación.
 */
object TokenManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_REFRESH_TOKEN = "refresh_token"
    private const val KEY_TOKEN_TYPE = "token_type"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_ROLE = "user_role"

    private var sharedPreferences: SharedPreferences? = null

    /**
     * Inicializa el TokenManager con el contexto de la aplicación.
     * Debe ser llamado al inicio de la aplicación.
     */
    fun initialize(context: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            Log.d("TokenManager", "TokenManager inicializado")
        }
    }

    /**
     * Guarda el token de autenticación.
     */
    fun saveAuthToken(token: String) {
        sharedPreferences?.edit()?.apply {
            putString(KEY_AUTH_TOKEN, token)
            commit()
            //apply()
        } ?: Log.e("TokenManager", "Error: TokenManager no inicializado")
    }

    /**
     * Guarda el token de refresco.
     */
    fun saveRefreshToken(token: String) {
        sharedPreferences?.edit()?.apply {
            putString(KEY_REFRESH_TOKEN, token)
            commit()
            //apply()
        } ?: Log.e("TokenManager", "Error: TokenManager no inicializado")
    }

    /**
     * Guarda el tipo de token.
     */
    fun saveTokenType(tokenType: String) {
        sharedPreferences?.edit()?.apply {
            putString(KEY_TOKEN_TYPE, tokenType)
            commit()
            //apply()
        } ?: Log.e("TokenManager", "Error: TokenManager no inicializado")
    }

    /**
     * Guarda el ID del usuario.
     */
    fun saveUserId(userId: Int) {
        sharedPreferences?.edit()?.apply {
            putInt(KEY_USER_ID, userId)
            apply()
        } ?: Log.e("TokenManager", "Error: TokenManager no inicializado")
    }

    /**
     * Guarda el rol del usuario.
     */
    fun saveUserRole(role: String) {
        sharedPreferences?.edit()?.apply {
            putString(KEY_USER_ROLE, role)
            apply()
        } ?: Log.e("TokenManager", "Error: TokenManager no inicializado")
    }

    /**
     * Obtiene el token de autenticación.
     */
    fun getAuthToken(): String? {
        return sharedPreferences?.getString(KEY_AUTH_TOKEN, null)
            ?: run {
                Log.e("TokenManager", "Error: TokenManager no inicializado")
                null
            }
    }

    /**
     * Obtiene el token de refresco.
     */
    fun getRefreshToken(): String? {
        return sharedPreferences?.getString(KEY_REFRESH_TOKEN, null)
            ?: run {
                Log.e("TokenManager", "Error: TokenManager no inicializado")
                null
            }
    }

    /**
     * Obtiene el tipo de token.
     */
    fun getTokenType(): String? {
        return sharedPreferences?.getString(KEY_TOKEN_TYPE, null)
            ?: run {
                Log.e("TokenManager", "Error: TokenManager no inicializado")
                null
            }
    }

    /**
     * Obtiene el ID del usuario.
     */
    fun getUserId(): Int {
        return sharedPreferences?.getInt(KEY_USER_ID, -1)
            ?: run {
                Log.e("TokenManager", "Error: TokenManager no inicializado")
                -1
            }
    }

    /**
     * Obtiene el rol del usuario.
     */
    fun getUserRole(): String? {
        return sharedPreferences?.getString(KEY_USER_ROLE, null)
            ?: run {
                Log.e("TokenManager", "Error: TokenManager no inicializado")
                null
            }
    }

    /**
     * Verifica si existe un token de autenticación guardado.
     */
    fun isLoggedIn(): Boolean {
        return !getAuthToken().isNullOrEmpty()
    }

    /**
     * Limpia los datos de autenticación almacenados.
     */
    fun clearAuth() {
        sharedPreferences?.edit()?.apply {
            remove(KEY_AUTH_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_TOKEN_TYPE)
            remove(KEY_USER_ID)
            remove(KEY_USER_ROLE)
            apply()
        } ?: Log.e("TokenManager", "Error: TokenManager no inicializado")
    }
}

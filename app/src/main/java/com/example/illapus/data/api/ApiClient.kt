package com.example.illapus.data.api

import android.content.Context
import android.util.Log
import com.example.illapus.R
import com.example.illapus.utils.TokenManager
import com.example.illapus.utils.AuthManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private lateinit var retrofit: Retrofit
    private lateinit var baseUrl: String

    // Servicios API
    private var _authApiService: AuthApiService? = null
    private var _propertyService: PropertyApiService? = null
    private var _activityService: ActivityApiService? = null
    private var _reserveService: ReserveApiService? = null
    private var _searchService: SearchService? = null

    val authApiService: AuthApiService
        get() {
            if (_authApiService == null) {
                _authApiService = retrofit.create(AuthApiService::class.java)
            }
            return _authApiService!!
        }

    val propertyService: PropertyApiService
        get() {
            if (_propertyService == null) {
                _propertyService = retrofit.create(PropertyApiService::class.java)
            }
            return _propertyService!!
        }

    val activityService: ActivityApiService
        get() {
            if (_activityService == null) {
                _activityService = retrofit.create(ActivityApiService::class.java)
            }
            return _activityService!!
        }

    val reservaService: ReserveApiService
        get() {
            if (_reserveService == null) {
                _reserveService = retrofit.create(ReserveApiService::class.java)
            }
            return _reserveService!!
        }

    val searchService: SearchService
        get() {
            if (_searchService == null) {
                _searchService = retrofit.create(SearchService::class.java)
            }
            return _searchService!!
        }

    // Inicialización del cliente API
    fun initialize(context: Context) {
        baseUrl = getBaseUrlFromResources(context)

        // Configuración del interceptor de logging
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        // Interceptor para añadir el token de autenticación
        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val token = TokenManager.getAuthToken()
            val tokenType = TokenManager.getTokenType() ?: "Bearer"

            // Si hay un token, lo añadimos a la cabecera de autorización
            val requestBuilder: Request.Builder = if (!token.isNullOrEmpty()) {
                Log.d("ApiClient", "Añadiendo token de autenticación a la solicitud")
                original.newBuilder()
                    .header("Authorization", "$tokenType $token")
            } else {
                original.newBuilder()
            }

            val request = requestBuilder.build()
            chain.proceed(request)
        }

        // Interceptor para manejar errores de autenticación
        val authErrorInterceptor = Interceptor { chain ->
            val response: Response = chain.proceed(chain.request())

            // Si recibimos un 401, la sesión ha expirado
            if (response.code == 401) {
                Log.w("ApiClient", "Error 401 detectado - Sesión expirada")
                // Manejar expiración automática de sesión
                AuthManager.handleSessionExpired()
            }

            response
        }

        // Configuración del cliente OkHttp con los interceptores
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(authErrorInterceptor) // Añadir interceptor de errores de auth
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        // Creación de la instancia de Retrofit
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        Log.d("ApiClient", "ApiClient inicializado con URL: $baseUrl")
    }

    // Obtener la URL base según la configuración en strings.xml
    private fun getBaseUrlFromResources(context: Context): String {
        val urlType = context.getString(R.string.api_url_type)

        return when (urlType) {
            "emulator" -> context.getString(R.string.api_url_emulator)
            "device" -> context.getString(R.string.api_url_device)
            "production" -> context.getString(R.string.api_url_production)
            else -> context.getString(R.string.api_url_emulator) // URL por defecto
        }
    }

    // Cambiar la URL base dinámicamente (requiere reinicialización)
    fun changeBaseUrl(context: Context, urlType: String) {
        // Actualizar el tipo de URL a usar
        baseUrl = when (urlType) {
            "emulator" -> context.getString(R.string.api_url_emulator)
            "device" -> context.getString(R.string.api_url_device)
            "production" -> context.getString(R.string.api_url_production)
            else -> context.getString(R.string.api_url_emulator)
        }

        // Recrear la instancia de Retrofit con la nueva URL
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val original = chain.request()
            val token = TokenManager.getAuthToken()
            val tokenType = TokenManager.getTokenType() ?: "Bearer"

            val requestBuilder: Request.Builder = if (!token.isNullOrEmpty()) {
                Log.d("ApiClient", "Añadiendo token de autenticación a la solicitud")
                original.newBuilder()
                    .header("Authorization", "$tokenType $token")
            } else {
                original.newBuilder()
            }

            val request = requestBuilder.build()
            chain.proceed(request)
        }

        // Interceptor para manejar errores de autenticación
        val authErrorInterceptor = Interceptor { chain ->
            val response: Response = chain.proceed(chain.request())

            if (response.code == 401) {
                Log.w("ApiClient", "Error 401 detectado - Sesión expirada")
                AuthManager.handleSessionExpired()
            }

            response
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(authErrorInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Reiniciar los servicios
        _authApiService = null
        _propertyService = null
        _activityService = null
        _reserveService = null
        _searchService = null

        Log.d("ApiClient", "ApiClient reinicializado con URL: $baseUrl")
    }
}

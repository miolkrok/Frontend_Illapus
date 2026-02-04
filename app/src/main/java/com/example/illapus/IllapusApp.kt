package com.example.illapus

import android.app.Application
import com.example.illapus.data.api.ApiClient
import com.example.illapus.utils.TokenManager
import com.example.illapus.utils.AuthManager

class IllapusApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // Inicializar TokenManager
        TokenManager.initialize(applicationContext)

        // Inicializar AuthManager
        AuthManager.initialize()

        // Inicializar ApiClient con las URLs configuradas en strings.xml
        ApiClient.initialize(applicationContext)
    }
}

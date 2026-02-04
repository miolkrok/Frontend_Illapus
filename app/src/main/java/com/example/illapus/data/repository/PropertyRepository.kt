package com.example.illapus.data.repository

import com.example.illapus.data.api.PropertyApiService
import com.example.illapus.model.Property
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PropertyRepository(private val apiService: PropertyApiService) {

    fun getProperties(): Flow<Result<List<Property>>> = flow {
        try {
            val response = apiService.getProperties()
            if (response.isSuccessful) {
                val properties = response.body() ?: emptyList()
                emit(Result.success(properties))
            } else {
                emit(Result.failure(Exception("Error en la respuesta del servidor: ${response.code()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}

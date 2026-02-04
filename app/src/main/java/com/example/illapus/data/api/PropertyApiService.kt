package com.example.illapus.data.api

import com.example.illapus.model.Property
import retrofit2.Response
import retrofit2.http.GET

interface PropertyApiService {
    @GET("busquedas/simple")
    suspend fun getProperties(): Response<List<Property>>

}

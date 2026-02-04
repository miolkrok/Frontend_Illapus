package com.example.illapus.data.api

import com.example.illapus.model.FilteredActivityResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchService {
    @GET("busquedas/filtros-avanzados")
    suspend fun searchWithFilters(
        @Query("ubicacion") ubicacion: String? = null,
        @Query("fechaInicio") fechaInicio: String? = null,
        @Query("fechaFin") fechaFin: String? = null,
        @Query("personas") personas: Int? = null,
        @Query("tipo") tipo: String? = null,
        @Query("precioMin") precioMin: Double? = null,
        @Query("precioMax") precioMax: Double? = null,
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("radio") radio: Double? = null
    ): Response<List<FilteredActivityResponse>>
}

package com.example.illapus.data.api

import com.example.illapus.data.model.OpinionCreateResponse;
import com.example.illapus.data.model.OpinionRequest;
import com.example.illapus.data.model.OpinionResponse;
import com.example.illapus.data.model.PromedioOpinionResponse;


import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
interface OpinionApiService {

    @GET("opiniones/actividad/{actividadId}")
    suspend fun getOpinionesByActividad(
        @Path("actividadId") actividadId: Int
    ): List<OpinionResponse>

    // Obtener promedio de calificación de una actividad (público)
    @GET("opiniones/promedio/actividad/{actividadId}")
    suspend fun getPromedioByActividad(
        @Path("actividadId") actividadId: Int
    ): PromedioOpinionResponse

    // Crear opinión (modo simple, sin validaciones externas)
    @POST("opiniones/simple")
    suspend fun createOpinion(
        @Body opinion: OpinionRequest
    ): Response<OpinionCreateResponse>

    // Obtener mis opiniones
    @GET("opiniones/mis-opiniones")
    suspend fun getMisOpiniones(): List<OpinionResponse>

    // Eliminar opinión
    @DELETE("opiniones/{id}")
    suspend fun deleteOpinion(
        @Path("id") id: Int
    ): Response<Any>

    // Actualizar opinión
    @PUT("opiniones/{id}")
    suspend fun updateOpinion(
        @Path("id") id: Int,
        @Body opinion: OpinionRequest
    ): Response<Any>
}
package com.example.illapus.data.api

import com.example.illapus.data.model.ActivityDetailsModel
import com.example.illapus.data.model.ActivityRequest
import com.example.illapus.data.model.GaleriaItem
import com.example.illapus.data.model.ServicioItem
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ActivityApiService {
    @POST("actividades")
    suspend fun createActivity(@Body activityRequest: ActivityRequest): Response<Any>

    @GET("actividades/{id}")
    suspend fun getActivityDetails(@Path("id") activityId: Int): ActivityDetailsModel

    @GET("actividades/mis-actividades")
    suspend fun getHostActivities(): List<ActivityDetailsModel>

    @DELETE("actividades/{id}")
    suspend fun deleteActivity(@Path("id") activityId: Int): Response<Unit>

    @PUT("actividades/{id}")
    suspend fun updateActivity(
        @Path("id") activityId: Int, @Body activityRequest: ActivityRequest
    ): Response<Any>

    @DELETE("actividades/{id}/galeria/{galeriaId}")
    suspend fun deleteGalleryImage(
        @Path("id") activityId: Int, @Path("galeriaId") galleryId: Int
    ): Response<Any>

    @POST("actividades/{id}/galeria")
    suspend fun addGalleryImage(
        @Path("id") activityId: Int, @Body galeriaItem: GaleriaItem
    ): Response<Any>

    @DELETE("actividades/{id}/servicios/{servicioId}")
    suspend fun deleteService(
        @Path("id") activityId: Int, @Path("servicioId") serviceId: Int
    ): Response<Any>

    @POST("actividades/{id}/servicios")
    suspend fun addService(
        @Path("id") activityId: Int, @Body serviceItem: ServicioItem
    ): Response<Any>

    @GET("actividades/provincias")
    suspend fun getAvailableProvinces(): Response<List<String>>

    // También puedes agregar para ciudades si lo necesitas
    @GET("actividades/ciudades")
    suspend fun getAvailableCities(): Response<List<String>>
}

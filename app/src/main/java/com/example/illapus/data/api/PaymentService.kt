package com.example.illapus.data.api

import com.example.illapus.ui.viewmodel.Payment
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface PaymentService {

    /**
     * Obtener todos los pagos de las actividades de un anfitrión
     * @param hostId ID del anfitrión (usuario)
     */
    @GET("pagos/anfitrion/{hostId}")
    suspend fun getHostPayments(
        @Path("hostId") hostId: Int
    ): Response<List<Payment>>

    /**
     * Obtener pagos de una actividad específica
     * @param activityId ID de la actividad
     */
    @GET("pagos/actividad/{activityId}")
    suspend fun getActivityPayments(
        @Path("activityId") activityId: Int
    ): Response<List<Payment>>
}
package com.example.illapus.data.api

import com.example.illapus.data.model.PagoRequest
import com.example.illapus.data.model.PagoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface PagoApiService {
    // Crear pago con comprobante
    @POST("pagos")
    suspend fun createPago(@Body pago: PagoRequest): Response<PagoResponse>

    // Obtener pagos de una reserva
    @GET("pagos/reserva/{reservaId}")
    suspend fun getPagosByReserva(@Path("reservaId") reservaId: Int): List<PagoResponse>

    // Obtener mis pagos
    @GET("pagos/usuario/mis-pagos")
    suspend fun getMisPagos(): Response<List<PagoResponse>>

    @PUT("pagos/{id}")
    suspend fun updatePagoEstado(
        @Path("id") id: Int,
        @Body body: Map<String, String>
    ): Response<Any>

}
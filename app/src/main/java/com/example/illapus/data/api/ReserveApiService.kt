package com.example.illapus.data.api

import com.example.illapus.data.model.ReserveDTO
import com.example.illapus.data.model.ReserveCreationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ReserveApiService {
    @POST("reservas")
    suspend fun createReserva(@Body reserva: ReserveDTO): Response<ReserveCreationResponse>

    @GET("reservas/mis-reservas")
    suspend fun getMyReservations(): Response<List<ReserveCreationResponse>>

    @DELETE("reservas/{id}")
    suspend fun deleteReservation(@Path("id") id: Int): Response<Unit>
}

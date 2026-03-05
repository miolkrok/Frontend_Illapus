package com.example.illapus.data.api

import com.example.illapus.data.model.ReserveDTO
import com.example.illapus.data.model.ReserveCreationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class DisponibilidadResponse(
    val actividadId: Int?,
    val fecha: String?,
    val maxPeople: Int?,
    val personasReservadas: Int?,
    val cupoDisponible: Int?,
    val disponible: Boolean?,
    val totalReservas: Int?
)

interface ReserveApiService {
    @POST("reservas")
    suspend fun createReserva(@Body reserva: ReserveDTO): Response<ReserveCreationResponse>

    @GET("reservas/mis-reservas")
    suspend fun getMyReservations(): Response<List<ReserveCreationResponse>>

    @DELETE("reservas/{id}")
    suspend fun deleteReservation(@Path("id") id: Int): Response<Unit>

    @GET("reservas/disponibilidad/{actividadId}")
    suspend fun getDisponibilidad(
        @Path("actividadId") actividadId: Int,
        @Query("fecha") fecha: String
    ): Response<DisponibilidadResponse>
}

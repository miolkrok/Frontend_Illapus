package com.example.illapus.data.repository

import com.example.illapus.data.api.ReserveApiService
import com.example.illapus.data.model.ReserveApiResponse
import com.example.illapus.data.model.ReserveDTO
import com.example.illapus.data.model.ReserveCreationResponse
import retrofit2.Response

class ReservationRepository(
    private val reserveApiService: ReserveApiService
) {
//    suspend fun createReservation(reserva: ReserveDTO): Response<ReserveCreationResponse> {
//        return reserveApiService.createReserva(reserva)
//    }

    suspend fun getMyReservations(): Response<List<ReserveCreationResponse>> {
        return reserveApiService.getMyReservations()
    }

    suspend fun createReservation(reserva: ReserveDTO): Response<ReserveApiResponse> {
        return reserveApiService.createReservation(reserva)
    }

    suspend fun deleteReservation(reservationId: Int): Response<Unit> {
        return reserveApiService.deleteReservation(reservationId)
    }
}

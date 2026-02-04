package com.example.illapus.data.api

import com.example.illapus.data.model.LoginRequest
import com.example.illapus.data.model.LoginResponse
import com.example.illapus.data.model.RegisterRequest
import com.example.illapus.data.model.RegisterResponse
import com.example.illapus.data.model.UserInfoResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): RegisterResponse

    @GET("auth/me")
    suspend fun getUserInfo(): UserInfoResponse

}

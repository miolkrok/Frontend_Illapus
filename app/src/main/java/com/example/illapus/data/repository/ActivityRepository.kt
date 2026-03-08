package com.example.illapus.data.repository

import com.example.illapus.data.api.ActivityApiService
import com.example.illapus.data.api.ApiClient.activityService
import com.example.illapus.data.model.ActivityDetailsModel
import com.example.illapus.data.model.ActivityRequest
import com.example.illapus.data.model.GaleriaItem
import com.example.illapus.data.model.ServicioItem
import retrofit2.Response

class ActivityRepository(
    private val activityApiService: ActivityApiService
) {
    suspend fun getActivityDetails(id: Int): ActivityDetailsModel {
        return activityApiService.getActivityDetails(id)
    }

    suspend fun getHostActivities(): List<ActivityDetailsModel> {
        return activityApiService.getHostActivities()
    }

    suspend fun deleteActivity(id: Int): Response<Unit> {
        return activityApiService.deleteActivity(id)
    }

    suspend fun updateActivity(id: Int, activityRequest: ActivityRequest): Response<Any> {
        return activityApiService.updateActivity(id, activityRequest)
    }

    suspend fun createActivity(activityRequest: ActivityRequest): Response<Any> {
        return activityApiService.createActivity(activityRequest)
    }

    suspend fun deleteGalleryImage(activityId: Int, galleryId: Int): Response<Any> {
        return activityApiService.deleteGalleryImage(activityId, galleryId)
    }

    suspend fun addGalleryImage(activityId: Int, galeriaItem: GaleriaItem): Response<Any> {
        return activityApiService.addGalleryImage(activityId, galeriaItem)
    }

    suspend fun deleteService(activityId: Int, serviceId: Int): Response<Any> {
        return activityApiService.deleteService(activityId, serviceId)
    }

    suspend fun addService(activityId: Int, serviceItem: ServicioItem): Response<Any> {
        return activityApiService.addService(activityId, serviceItem)
    }
    suspend fun getAvailableProvinces(): List<String> {
        return try {
            val response = activityService.getAvailableProvinces()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getAvailableCities(): List<String> {
        return try {
            val response = activityService.getAvailableCities()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}

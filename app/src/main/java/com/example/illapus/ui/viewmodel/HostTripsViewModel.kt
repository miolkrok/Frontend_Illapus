package com.example.illapus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.illapus.data.api.ApiClient
import com.example.illapus.data.model.ActivityDetailsModel
import com.example.illapus.data.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HostTripsViewModel : ViewModel() {

    // Estado de la UI
    private val _uiState = MutableStateFlow(HostTripsUiState())
    val uiState: StateFlow<HostTripsUiState> = _uiState.asStateFlow()

    private val activityRepository = ActivityRepository(ApiClient.activityService)

    init {
        loadHostActivities()
    }

    fun loadHostActivities() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val activities = activityRepository.getHostActivities()
                _uiState.value = _uiState.value.copy(
                    activities = activities,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar las actividades: ${e.message}"
                )
            }
        }
    }

    fun refresh() {
        loadHostActivities()
    }

    fun deleteActivity(activityId: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            try {
                val response = activityRepository.deleteActivity(activityId)
                if (response.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        successMessage = "Actividad eliminada exitosamente"
                    )
                    // Recargar la lista de actividades después de eliminar
                    loadHostActivities()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Error al eliminar la actividad: ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al eliminar la actividad: ${e.message}"
                )
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    // Clase que representa el estado de la UI
    data class HostTripsUiState(
        val activities: List<ActivityDetailsModel> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null
    )
}

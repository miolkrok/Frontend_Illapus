package com.example.illapus.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.illapus.data.repository.ActivityRepository
import com.example.illapus.data.api.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FilterViewModel: ViewModel() {
    private val activityRepository = ActivityRepository(ApiClient.activityService)

    private val _availableProvinces = MutableStateFlow<List<String>>(emptyList())
    val availableProvinces: StateFlow<List<String>> = _availableProvinces.asStateFlow()

    private val _availableCities = MutableStateFlow<List<String>>(emptyList())
    val availableCities: StateFlow<List<String>> = _availableCities.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadLocationData()
    }

    fun loadLocationData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Cargar provincias y ciudades en paralelo
                val provinces = activityRepository.getAvailableProvinces()
                val cities = activityRepository.getAvailableCities()

                _availableProvinces.value = provinces
                _availableCities.value = cities
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Error al cargar ubicaciones: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshLocationData() {
        loadLocationData()
    }
}
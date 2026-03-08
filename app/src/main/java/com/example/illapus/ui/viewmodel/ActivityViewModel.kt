package com.example.illapus.ui.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.illapus.data.api.ApiClient
import com.example.illapus.data.repository.PropertyRepository
import com.example.illapus.model.Property
import com.example.illapus.model.FilterCriteria
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ActivityViewModel : BaseViewModel() {

    // Estado UI para la pantalla de viajes
    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    private val propertyRepository = PropertyRepository(ApiClient.propertyService)

    // Variable para controlar si ya se cargaron los datos inicialmente
    private var hasLoadedInitialData = false

    // Lista original de propiedades (sin filtrar ni buscar)
    private var originalProperties: List<Property> = emptyList()

    // Lista filtrada por filtros avanzados (base para búsqueda)
    private var filteredProperties: List<Property> = emptyList()

    // Job para el debounce de búsqueda
    private var searchJob: Job? = null

    init {
        // Solo cargar si no hay datos previos
        loadPropertiesIfNeeded()
    }

    // Función que verifica si necesita cargar datos
    private fun loadPropertiesIfNeeded() {
        if (!hasLoadedInitialData && _uiState.value.properties.isEmpty() && !_uiState.value.isLoading) {
            loadProperties(isRefresh = false)
        }
    }

    // Función para cargar propiedades desde la API
    fun loadProperties(isRefresh: Boolean = false) {
        // Evitar múltiples cargas simultáneas
        if (_uiState.value.isLoading && !isRefresh) return

        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = !isRefresh, // Solo mostrar loading si no es refresh
                    isRefreshing = isRefresh,
                    errorMessage = null
                )
            }

            // Usar el método del BaseViewModel para manejo automático de errores
            launchApiCall(
                apiCall = {
                    // Convertir el Flow a una llamada directa
                    var result: Result<List<Property>>? = null
                    propertyRepository.getProperties().collect {
                        result = it
                    }
                    result?.getOrThrow() ?: emptyList()
                },
                onSuccess = { properties ->
                    hasLoadedInitialData = true
                    originalProperties = properties
                    filteredProperties = properties
                    applyLocalSearch() // Aplicar búsqueda si hay query activo
                },
                onError = { exception ->
                    // Si es 404, tratar como lista vacía (no hay actividades aún)
                    if (exception.message?.contains("404") == true) {
                        hasLoadedInitialData = true
                        originalProperties = emptyList()
                        filteredProperties = emptyList()
                        _uiState.update { currentState ->
                            currentState.copy(
                                properties = emptyList(),
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = null // No mostrar error, solo lista vacía
                            )
                        }
                    } else {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                isRefreshing = false,
                                errorMessage = exception.message ?: "Error desconocido"
                            )
                        }
                    }
                }
            )
        }
    }

    // Función específica para el SwipeRefresh
    fun refresh() {
        loadProperties(isRefresh = true)
    }

    // Función para limpiar el error
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    // Función para forzar recarga (útil para casos específicos)
    fun forceReload() {
        hasLoadedInitialData = false
        loadProperties(isRefresh = false)
    }

    // Función para actualizar la consulta de búsqueda con debounce
    fun updateSearchQuery(query: String) {
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = query,
                isSearchActive = query.isNotBlank()
            )
        }

        // Cancelar job anterior si existe
        searchJob?.cancel()

        // Crear nuevo job con debounce de 300ms
        searchJob = viewModelScope.launch {
            delay(300) // Debounce de 300ms
            applyLocalSearch()
        }
    }

    // Función para limpiar la búsqueda
    fun clearSearch() {
        searchJob?.cancel()
        _uiState.update { currentState ->
            currentState.copy(
                searchQuery = "",
                isSearchActive = false
            )
        }
        applyLocalSearch()
    }

    // Función para aplicar búsqueda local sobre la lista actual
    private fun applyLocalSearch() {
        val currentQuery = _uiState.value.searchQuery.trim()

        val searchResults = if (currentQuery.isBlank()) {
            // Si no hay búsqueda, mostrar la lista filtrada (o original si no hay filtros)
            filteredProperties
        } else {
            // Aplicar búsqueda case-insensitive sobre la lista filtrada
            filteredProperties.filter { property ->
                property.title.contains(currentQuery, ignoreCase = true)
            }
        }

        _uiState.update { currentState ->
            currentState.copy(
                properties = searchResults,
                isLoading = false,
                isRefreshing = false,
                errorMessage = null
            )
        }
    }

    // Función para abrir o cerrar el Drawer de navegación
    fun toggleDrawer() {
        _uiState.update { currentState ->
            currentState.copy(
                isDrawerOpen = !currentState.isDrawerOpen
            )
        }
    }

    // Función para cerrar el Drawer de navegación
    fun closeDrawer() {
        _uiState.update { currentState ->
            currentState.copy(
                isDrawerOpen = false
            )
        }
    }

    // Función para aplicar filtros
    fun applyFilters() {
        val currentFilters = _uiState.value.filterCriteria
        if (currentFilters.isEmpty()) {
            // Si no hay filtros, usar lista original
            filteredProperties = originalProperties
            applyLocalSearch() // Aplicar búsqueda si hay query activo
            closeDrawer()
            return
        }

        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            launchApiCall(
                apiCall = {
                    val response = ApiClient.searchService.searchWithFilters(
                        ubicacion = if (currentFilters.ubicacion.isNotBlank()) currentFilters.ubicacion else null,
                        fechaInicio = if (currentFilters.fechaInicio.isNotBlank()) currentFilters.fechaInicio else null,
                        fechaFin = if (currentFilters.fechaFin.isNotBlank()) currentFilters.fechaFin else null,
                        personas = if (currentFilters.personas > 0) currentFilters.personas else null,
                        tipo = if (currentFilters.tipo.isNotBlank()) currentFilters.tipo else null,
                        precioMin = if (currentFilters.precioMin > 0.0) currentFilters.precioMin else null,
                        precioMax = if (currentFilters.precioMax > 0.0) currentFilters.precioMax else null,
                        lat = currentFilters.lat,
                        lng = currentFilters.lng,
                        radio = if (currentFilters.radio > 0.0) currentFilters.radio else null
                    )

                    if (response.isSuccessful) {
                        response.body()?.map { it.toProperty() } ?: emptyList()
                    } else {
                        throw Exception("Error en la búsqueda: ${response.code()}")
                    }
                },
                onSuccess = { properties ->
                    filteredProperties = properties
                    _uiState.update { currentState ->
                        currentState.copy(
                            hasAppliedFilters = true
                        )
                    }
                    applyLocalSearch() // Aplicar búsqueda si hay query activo
                    closeDrawer()
                },
                onError = { exception ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Error al aplicar filtros"
                        )
                    }
                }
            )
        }
    }

    // Función para limpiar filtros
    fun clearFilters() {
        searchJob?.cancel()
        _uiState.update { currentState ->
            currentState.copy(
                filterCriteria = FilterCriteria(),
                hasAppliedFilters = false
            )
        }
        filteredProperties = originalProperties
        applyLocalSearch() // Mantener búsqueda si hay query activo
        closeDrawer()
    }

    // Funciones para actualizar cada campo del filtro
    fun updateLocation(location: String) {
        _uiState.update { currentState ->
            currentState.copy(
                filterCriteria = currentState.filterCriteria.copy(ubicacion = location)
            )
        }
    }

    fun updateStartDate(date: String) {
        _uiState.update { currentState ->
            currentState.copy(
                filterCriteria = currentState.filterCriteria.copy(fechaInicio = date)
            )
        }
    }

    fun updateEndDate(date: String) {
        _uiState.update { currentState ->
            currentState.copy(
                filterCriteria = currentState.filterCriteria.copy(fechaFin = date)
            )
        }
    }

    fun updatePersons(persons: Int) {
        _uiState.update { currentState ->
            currentState.copy(
                filterCriteria = currentState.filterCriteria.copy(personas = persons)
            )
        }
    }

    fun updateActivityType(type: String) {
        _uiState.update { currentState ->
            currentState.copy(
                filterCriteria = currentState.filterCriteria.copy(tipo = type)
            )
        }
    }

    fun updatePriceRange(minPrice: Double, maxPrice: Double) {
        _uiState.update { currentState ->
            currentState.copy(
                filterCriteria = currentState.filterCriteria.copy(
                    precioMin = minPrice,
                    precioMax = maxPrice
                )
            )
        }
    }

    fun updateRadius(radius: Double) {
        _uiState.update { currentState ->
            currentState.copy(
                filterCriteria = currentState.filterCriteria.copy(radio = radius)
            )
        }
    }
}

// Clase que representa el estado de la UI para la pantalla de viajes
data class ActivityUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val properties: List<Property> = emptyList(),
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val isDrawerOpen: Boolean = false,
    val filterCriteria: FilterCriteria = FilterCriteria(),
    val hasAppliedFilters: Boolean = false
)

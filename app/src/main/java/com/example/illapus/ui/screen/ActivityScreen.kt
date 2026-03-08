package com.example.illapus.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.illapus.data.config.FilterOptions
import com.example.illapus.ui.components.PropertyCard
import com.example.illapus.ui.navigation.AppDestinations
import com.example.illapus.ui.navigation.NavigationItems
import com.example.illapus.ui.viewmodel.ActivityViewModel
import com.example.illapus.ui.viewmodel.FilterViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.res.painterResource
import com.example.illapus.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    modifier: Modifier = Modifier,
    viewModel: ActivityViewModel,
    filterViewModel: FilterViewModel = viewModel(),
    navController: NavController,
    isHostMode: Boolean,
    onNavigateToProfile: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val swipeRefreshState = rememberSwipeRefreshState(uiState.isRefreshing)
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Estados para los DatePickers
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Estados para los dropdowns
    var expandedLocation by remember { mutableStateOf(false) }
    var expandedActivityType by remember { mutableStateOf(false) }

    // Sincronizar el estado del drawer con el ViewModel
    LaunchedEffect(uiState.isDrawerOpen) {
        if (uiState.isDrawerOpen && drawerState.isClosed) {
            drawerState.open()
        } else if (!uiState.isDrawerOpen && drawerState.isOpen) {
            drawerState.close()
        }
    }

    // Detectar cuando el drawer se cierra y actualizar el ViewModel
    LaunchedEffect(drawerState.isOpen) {
        if (!drawerState.isOpen && uiState.isDrawerOpen) {
            viewModel.closeDrawer()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(350.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Filtros",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // FILTRO DE UBICACIÓN - Versión mejorada con datos del backend
                    Text(
                        text = "Ubicación",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val filterViewModel: FilterViewModel =
                        viewModel() // Agregar esta línea al inicio del composable

                    val availableCities by filterViewModel.availableCities.collectAsState()
                    val isLoadingLocations by filterViewModel.isLoading.collectAsState()

                    var locationText by remember { mutableStateOf(uiState.filterCriteria.ubicacion) }
                    var showSuggestions by remember { mutableStateOf(false) }

                    // Filtrar ciudades según el texto ingresado
                    val filteredCities = remember(availableCities, locationText) {
                        if (locationText.isBlank()) {
                            emptyList()
                        } else {
                            availableCities.filter {
                                it.contains(locationText, ignoreCase = true)
                            }.take(5) // Limitar a 5 sugerencias
                        }
                    }

                    Column {
                        OutlinedTextField(
                            value = locationText,
                            onValueChange = {
                                locationText = it
                                viewModel.updateLocation(it)
                                showSuggestions = it.isNotBlank()
                            },
                            label = { Text("Buscar ciudad") },
                            placeholder = { Text("Ej: Quito, Cuenca, Guayaquil...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            trailingIcon = {
                                if (isLoadingLocations) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                }
                            }
                        )

                        // Mostrar sugerencias
                        if (showSuggestions && filteredCities.isNotEmpty()) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column {
                                    filteredCities.forEach { city ->
                                        TextButton(
                                            onClick = {
                                                locationText = city
                                                viewModel.updateLocation(city)
                                                showSuggestions = false
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = city,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp)
                                            )
                                        }

                                        if (city != filteredCities.last()) {
                                            Divider()
                                        }
                                    }
                                }
                            }
                        }

                        // Mostrar mensaje de carga
                        if (isLoadingLocations && availableCities.isEmpty()) {
                            Text(
                                text = "Cargando ciudades...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        // Mostrar mensaje si no hay resultados
                        if (locationText.isNotBlank() && filteredCities.isEmpty() && !isLoadingLocations) {
                            Text(
                                text = "No se encontraron ciudades",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filtro de Fecha de Inicio
                    Text(
                        text = "Fecha de Inicio",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = uiState.filterCriteria.fechaInicio,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Seleccionar fecha") },
                        trailingIcon = {
                            IconButton(onClick = { showStartDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, "Seleccionar fecha")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filtro de Fecha de Fin
                    Text(
                        text = "Fecha de Fin",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = uiState.filterCriteria.fechaFin,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Seleccionar fecha") },
                        trailingIcon = {
                            IconButton(onClick = { showEndDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, "Seleccionar fecha")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filtro de Número de Personas
                    Text(
                        text = "Número de Personas",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                if (uiState.filterCriteria.personas > FilterOptions.PersonRange.MIN_PERSONS) {
                                    viewModel.updatePersons(uiState.filterCriteria.personas - 1)
                                }
                            },
                            enabled = uiState.filterCriteria.personas > FilterOptions.PersonRange.MIN_PERSONS
                        ) {
                            Icon(Icons.Default.Remove, "Disminuir")
                        }

                        Text(
                            text = if (uiState.filterCriteria.personas == 0) "Sin seleccionar" else "${uiState.filterCriteria.personas} ${if (uiState.filterCriteria.personas == 1) "persona" else "personas"}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            color = if (uiState.filterCriteria.personas == 0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )

                        IconButton(
                            onClick = {
                                if (uiState.filterCriteria.personas < FilterOptions.PersonRange.MAX_PERSONS) {
                                    viewModel.updatePersons(uiState.filterCriteria.personas + 1)
                                }
                            },
                            enabled = uiState.filterCriteria.personas < FilterOptions.PersonRange.MAX_PERSONS
                        ) {
                            Icon(Icons.Default.Add, "Aumentar")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filtro de Tipo de Actividad
                    Text(
                        text = "Tipo de Actividad",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = expandedActivityType,
                        onExpandedChange = { expandedActivityType = !expandedActivityType }
                    ) {
                        OutlinedTextField(
                            value = uiState.filterCriteria.tipo,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Seleccionar tipo") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedActivityType) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = expandedActivityType,
                            onDismissRequest = { expandedActivityType = false }
                        ) {
                            FilterOptions.activityTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        viewModel.updateActivityType(type)
                                        expandedActivityType = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filtro de Rango de Precios
                    Text(
                        text = "Rango de Precios",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = if (uiState.filterCriteria.precioMin == 0.0 && uiState.filterCriteria.precioMax == 0.0) {
                            "Sin límite de precio"
                        } else {
                            "$${uiState.filterCriteria.precioMin.toInt()} - $${uiState.filterCriteria.precioMax.toInt()}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = if (uiState.filterCriteria.precioMin == 0.0 && uiState.filterCriteria.precioMax == 0.0) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    RangeSlider(
                        value = uiState.filterCriteria.precioMin.toFloat()..uiState.filterCriteria.precioMax.toFloat(),
                        onValueChange = { range ->
                            viewModel.updatePriceRange(
                                range.start.toDouble(),
                                range.endInclusive.toDouble()
                            )
                        },
                        valueRange = FilterOptions.PriceRange.MIN_PRICE.toFloat()..FilterOptions.PriceRange.MAX_PRICE.toFloat(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Filtro de Radio de Búsqueda
                    Text(
                        text = "Radio de Búsqueda",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = if (uiState.filterCriteria.radio == 0.0) {
                            "Sin límite de distancia"
                        } else {
                            "${uiState.filterCriteria.radio.toInt()} km"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = if (uiState.filterCriteria.radio == 0.0) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )

                    Slider(
                        value = uiState.filterCriteria.radio.toFloat(),
                        onValueChange = { viewModel.updateRadius(it.toDouble()) },
                        valueRange = FilterOptions.RadiusRange.MIN_RADIUS.toFloat()..FilterOptions.RadiusRange.MAX_RADIUS.toFloat(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.clearFilters() },
                            modifier = Modifier.weight(1f),
                            enabled = uiState.filterCriteria.hasAnyFilter()
                        ) {
                            Text("Quitar Filtros")
                        }

                        Button(
                            onClick = { viewModel.applyFilters() },
                            modifier = Modifier.weight(1f),
                            enabled = uiState.filterCriteria.hasAnyFilter()
                        ) {
                            Text("Buscar")
                        }
                    }
                }
            }
        }
    ) {
        // DatePickers
        if (showStartDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            startDatePickerState.selectedDateMillis?.let { millis ->
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val date = dateFormat.format(Date(millis))
                                viewModel.updateStartDate(date)
                            }
                            showStartDatePicker = false
                        }
                    ) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStartDatePicker = false }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = startDatePickerState)
            }
        }

        if (showEndDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            endDatePickerState.selectedDateMillis?.let { millis ->
                                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val date = dateFormat.format(Date(millis))
                                viewModel.updateEndDate(date)
                            }
                            showEndDatePicker = false
                        }
                    ) {
                        Text("Aceptar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndDatePicker = false }) {
                        Text("Cancelar")
                    }
                }
            ) {
                DatePicker(state = endDatePickerState)
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        SearchBar(
                            query = uiState.searchQuery,
                            onQueryChange = {
                                // Solo permitir búsqueda si hay actividades disponibles
                                if (uiState.properties.isNotEmpty() || uiState.searchQuery.isNotBlank()) {
                                    viewModel.updateSearchQuery(it)
                                }
                            },
                            onSearch = { /* La búsqueda es en tiempo real, no necesita acción específica */ },
                            active = false, // Mantener siempre inactivo para evitar problemas de layout
                            onActiveChange = { /* No hacer nada */ },
                            placeholder = {
                                Text(
                                    if (uiState.properties.isEmpty() && !uiState.isLoading)
                                        "No hay actividades para buscar"
                                    else
                                        "Buscar actividades..."
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Buscar"
                                )
                            },
                            trailingIcon = {
                                if (uiState.searchQuery.isNotBlank()) {
                                    IconButton(
                                        onClick = { viewModel.clearSearch() }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Limpiar búsqueda"
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            enabled = uiState.properties.isNotEmpty() || uiState.searchQuery.isNotBlank() // Deshabilitar si no hay actividades
                        ) {
                            // Contenido del SearchBar cuando está activo - vacío
                        }
                    },
                    actions = {
                        // NUEVO: Botón de perfil
                        IconButton(
                            onClick = {
                                navController.navigate(NavigationItems.Profile.route)
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_profile),
                                contentDescription = "Perfil",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = {
                                scope.launch {
                                    if (drawerState.isClosed) {
                                        viewModel.toggleDrawer()
                                    } else {
                                        drawerState.close()
                                        viewModel.closeDrawer()
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (uiState.hasAppliedFilters) Icons.Default.FilterListOff else Icons.Default.FilterList,
                                contentDescription = if (uiState.hasAppliedFilters) "Filtros aplicados - Abrir filtros" else "Abrir filtros",
                                tint = if (uiState.hasAppliedFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Contenido principal con SwipeRefresh
                    SwipeRefresh(
                        state = swipeRefreshState,
                        onRefresh = { viewModel.refresh() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when {
                            uiState.isLoading && uiState.properties.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            uiState.errorMessage != null && uiState.properties.isEmpty() -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = uiState.errorMessage ?: "Error desconocido",
                                            color = MaterialTheme.colorScheme.error,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = { viewModel.loadProperties() }
                                        ) {
                                            Text("Reintentar")
                                        }
                                    }
                                }
                            }

                            uiState.properties.isEmpty() && !uiState.isLoading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = when {
                                                uiState.searchQuery.isNotBlank() && uiState.hasAppliedFilters ->
                                                    "No se encontraron actividades con '${uiState.searchQuery}' en los resultados filtrados"

                                                uiState.searchQuery.isNotBlank() ->
                                                    "No se encontraron actividades con '${uiState.searchQuery}'"

                                                uiState.hasAppliedFilters ->
                                                    "No se encontraron actividades con los filtros aplicados"

                                                else ->
                                                    "No hay actividades disponibles"
                                            },
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Botones de acción según el contexto
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (uiState.searchQuery.isNotBlank()) {
                                                OutlinedButton(
                                                    onClick = { viewModel.clearSearch() }
                                                ) {
                                                    Text("Limpiar búsqueda")
                                                }
                                            }

                                            if (uiState.hasAppliedFilters) {
                                                OutlinedButton(
                                                    onClick = { viewModel.clearFilters() }
                                                ) {
                                                    Text("Quitar filtros")
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            else -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(
                                        horizontal = 8.dp,
                                        vertical = 8.dp
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(uiState.properties) { property ->
                                        PropertyCard(
                                            property = property,
                                            onClick = {
                                                navController.navigate(
                                                    AppDestinations.ACTIVITY_DETAILS_ROUTE.replace(
                                                        "{activityId}",
                                                        property.id.toString()
                                                    )
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

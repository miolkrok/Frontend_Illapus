package com.example.illapus.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.illapus.ui.viewmodel.FilterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    viewModel: FilterViewModel = viewModel(),
    onApplyFilters: (FilterCriteria) -> Unit
) {
    val availableProvinces by viewModel.availableProvinces.collectAsState()
    val availableCities by viewModel.availableCities.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Estados locales para los filtros
    var selectedProvince by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("") }
    var searchText by remember { mutableStateOf("") }

    // Filtrar ciudades basado en la provincia seleccionada y texto de búsqueda
    val filteredCities = remember(availableCities, selectedProvince, searchText) {
        availableCities.filter { city ->
            val matchesProvince = selectedProvince.isEmpty() ||
                    city.contains(selectedProvince, ignoreCase = true)
            val matchesSearch = searchText.isEmpty() ||
                    city.contains(searchText, ignoreCase = true)
            matchesProvince && matchesSearch
        }.sorted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filtros") },
                actions = {
                    IconButton(onClick = { viewModel.refreshLocationData() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TextButton(onClick = { /* Limpiar filtros */ }) {
                        Text("Limpiar")
                    }

                    Button(
                        onClick = {
                            onApplyFilters(
                                FilterCriteria(
                                    provincia = selectedProvince,
                                    ciudad = selectedCity
                                    // otros filtros...
                                )
                            )
                        }
                    ) {
                        Text("Aplicar Filtros")
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Ubicación",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Selector de Provincia
            item {
                Text(
                    text = "Provincia",
                    style = MaterialTheme.typography.titleMedium
                )

                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (error != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error ?: "Error desconocido",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                } else {
                    // Dropdown de provincias
                    var expanded by remember { mutableStateOf(false) }

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedProvince,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Seleccionar provincia") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            // Opción "Todas"
                            DropdownMenuItem(
                                text = { Text("Todas las provincias") },
                                onClick = {
                                    selectedProvince = ""
                                    expanded = false
                                }
                            )

                            // Provincias disponibles
                            availableProvinces.forEach { province ->
                                DropdownMenuItem(
                                    text = { Text(province) },
                                    onClick = {
                                        selectedProvince = province
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Buscador de Ciudad
            item {
                Text(
                    text = "Ciudad",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("Buscar ciudad") },
                    placeholder = { Text("Escribe para buscar...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Resultados de ciudades
            if (searchText.isNotBlank() && filteredCities.isNotEmpty()) {
                items(filteredCities) { city ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (city == selectedCity)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        )
                    ) {
                        TextButton(
                            onClick = {
                                selectedCity = if (selectedCity == city) "" else city
                                searchText = if (selectedCity == city) "" else city
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
                    }
                }
            } else if (searchText.isNotBlank() && filteredCities.isEmpty() && !isLoading) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No se encontraron ciudades",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Aquí puedes agregar más filtros (tipo de actividad, precio, etc.)
            item {
                Text(
                    text = "Tipo de Actividad",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
                // Tus chips de tipo de actividad aquí...
            }
        }
    }
}

// Data class para los criterios de filtro
data class FilterCriteria(
    val provincia: String = "",
    val ciudad: String = "",
    val tipoActividad: String = "",
    val precioMin: Double? = null,
    val precioMax: Double? = null,
    val fechaInicio: String? = null,
    val fechaFin: String? = null
)
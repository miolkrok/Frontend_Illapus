package com.example.illapus.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.illapus.R
import com.example.illapus.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
    onLogout: () -> Unit = {},
    navController: androidx.navigation.NavController? = null,
    onToggleHostMode: (Boolean) -> Unit = {},
    isHostMode: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensaje de éxito
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long
            )
            viewModel.clearSuccessMessage()
        }
    }

    // Forzar re-login después de cambiar rol
    LaunchedEffect(uiState.requiresRelogin) {
        if (uiState.requiresRelogin) {
            snackbarHostState.showSnackbar(
                message = "¡Ahora eres operador turístico! Iniciando sesión nuevamente...",
                duration = SnackbarDuration.Short
            )
            kotlinx.coroutines.delay(1500)
            // Cerrar sesión y volver al login
            viewModel.logout()
            onLogout()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { scaffoldPadding ->
        Box(modifier = modifier.fillMaxSize().padding(scaffoldPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Título de la pantalla
                Text(
                    text = "Perfil",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                if (uiState.isLoading) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (uiState.errorMessage != null) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = uiState.errorMessage ?: "Error desconocido",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    // Tarjeta con perfil de usuario
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp, horizontal = 16.dp),
                        ) {
                            // Foto de perfil circular
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (uiState.profileImageUrl != null) {
                                    AsyncImage(
                                        model = uiState.profileImageUrl,
                                        contentDescription = "Foto de perfil",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape)
                                            .border(
                                                2.dp,
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            )
                                    )
                                } else {
                                    // Placeholder si no hay imagen
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .border(
                                                2.dp,
                                                MaterialTheme.colorScheme.primary,
                                                CircleShape
                                            )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(60.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Nombre de usuario en negrita
                            Text(
                                text = uiState.username,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Email
                            Text(
                                text = uiState.email,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Ubicación en letra más pequeña
                            Text(
                                text = uiState.location,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Badge de rol si es proveedor
                            if (uiState.isProveedor) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFFE8F5E9)
                                ) {
                                    Text(
                                        text = "✓ Operador Turístico",
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }

                    // Tarjeta para convertirse en operador turístico
                    // Solo se muestra si NO es proveedor aún

                    if (!uiState.isProveedor) {
                        // Tarjeta para convertirse en anfitrión
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 4.dp
                            ),
                            onClick = { viewModel.showConvertirProveedorDialog() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                if (uiState.isConvertingToProveedor) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        strokeWidth = 3.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Home,
                                        contentDescription = "Icono de casa",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .weight(1f)
                                ) {
                                    Text(
                                        text = "Convertirse en operador turístico",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )

                                    Text(
                                        text = "Crea actividades y genera ingresos",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Opción para cerrar sesión
                    OutlinedButton(
                        onClick = { viewModel.showLogoutConfirmation() },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = SolidColor(MaterialTheme.colorScheme.error)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .padding(bottom = if (uiState.isProveedor) 80.dp else 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "Cerrar Sesión",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }

            // FloatingActionButton centrado en la parte inferior - Solo visible si el usuario es proveedor
            if (uiState.isProveedor) {
                FloatingActionButton(
                    onClick = {
                        val nuevoModo = !isHostMode
                        onToggleHostMode(nuevoModo)
                    },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        // Icono para cambiar entre modos
                        Icon(
                            painter = painterResource(
                                id = if (isHostMode) R.drawable.ic_activities else R.drawable.ic_home
                            ),
                            contentDescription = if (isHostMode) "Icono de viajes" else "Icono de casa",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Texto que cambia según el modo actual
                        Text(
                            text = if (isHostMode) "Cambiar modo Viajero" else "Cambiar modo Operador turístico",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }

    // Diálogo de confirmación para convertirse en operador turístico
    if (uiState.showConvertirDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissConvertirProveedorDialog() },
            title = {
                Text(
                    "Convertirse en operador turístico",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Al convertirte en operador turístico podrás crear actividades y recibir reservas de viajeros.\n\n" +
                            "Después de aceptar, deberás cerrar sesión y volver a iniciar sesión para que los cambios se apliquen."
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.convertirAProveedor() }
                ) {
                    Text("Sí, quiero ser operador")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissConvertirProveedorDialog() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de confirmación para cerrar sesión
    if (uiState.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissLogoutConfirmation() },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Estás seguro que deseas cerrar la sesión?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.dismissLogoutConfirmation()
                        if (viewModel.logout()) {
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sí, cerrar sesión")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissLogoutConfirmation() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // ModalBottomSheet para opciones de anfitrión
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Elige tu tipo de anfitrión",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // Opción 1: Alojamiento
//                OptionCard(
//                    title = "Alojamiento",
//                    description = "Ofrece tu espacio para que otros se hospeden",
//                    icon = Icons.Default.Home,
//                    onClick = {
//                        showBottomSheet = false
//                        navController?.navigate(com.example.illapus.ui.navigation.AppDestinations.GENERIC_SCREEN_ROUTE)
//                    }
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))

                // Opción 2: Actividades
                OptionCard(
                    title = "Actividades",
                    description = "Organiza experiencias y actividades únicas",
                    icon = Icons.Default.Event,
                    onClick = {
                        showBottomSheet = false
                        navController?.navigate(com.example.illapus.ui.navigation.AppDestinations.GENERIC_SCREEN_ROUTE)
                    }
                )

                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}

@Composable
private fun OptionCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
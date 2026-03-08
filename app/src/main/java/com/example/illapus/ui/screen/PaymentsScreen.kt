package com.example.illapus.ui.screen

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.illapus.ui.viewmodel.Payment
import com.example.illapus.ui.viewmodel.PaymentsViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(
    navController: NavController,
    viewModel: PaymentsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadAllHostPayments()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pagos Recibidos") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.payments.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No hay pagos recibidos", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Los pagos de tus actividades aparecerán aquí",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.payments) { payment ->
                            PaymentDetailCard(
                                payment = payment,
                                onStatusChanged = { id, newStatus ->
                                    viewModel.updatePaymentStatus(id, newStatus)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentDetailCard(
    payment: Payment,
    onStatusChanged: (Int, String) -> Unit
) {
    var showImageDialog by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val currentStatus = payment.estado ?: payment.estadoPago ?: "PENDIENTE"

    val comprobanteUrl = payment.imagenComprobante

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // ── Título de la actividad ──
            Text(
                text = payment.actividadTitulo ?: "Actividad",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // ── Nombre del cliente ──
            DetailRow(label = "Cliente", value = payment.nombreUsuario ?: "Sin nombre")

            // ── Email ──
            if (!payment.emailUsuario.isNullOrEmpty()) {
                DetailRow(label = "Email", value = payment.emailUsuario)
            }

            // ── Fecha de reserva ──
            DetailRow(label = "Fecha reserva", value = formatDate(payment.fechaReserva))

            // ── Cantidad de personas ──
            DetailRow(label = "Personas", value = "${payment.cantidadPersonas ?: 0}")

            // ── Monto ──
            DetailRow(
                label = "Monto total",
                value = "$${payment.monto ?: 0.0}",
                valueColor = MaterialTheme.colorScheme.primary,
                valueBold = true
            )

            // ── Método de pago ──
            if (!payment.metodoPago.isNullOrEmpty()) {
                DetailRow(label = "Método", value = payment.metodoPago)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // COMPROBANTE (ÚNICA SECCIÓN)
            if (!comprobanteUrl.isNullOrEmpty()) {
                Text(
                    text = "Comprobante de pago:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Verificar si es una URL de imagen
                if (comprobanteUrl.startsWith("http")) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable { showImageDialog = true },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        AsyncImage(
                            model = comprobanteUrl,
                            contentDescription = "Comprobante de pago",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Toca para ampliar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // Si es base64 o texto, mostrar como texto
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = comprobanteUrl.take(100) +
                                    if (comprobanteUrl.length > 100) "..." else "",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                // No hay comprobante
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sin comprobante adjunto",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // ── Estado con Dropdown ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Estado:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )

                Box {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = getStatusColor(currentStatus).first
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable { expanded = true }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentStatus.uppercase(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = getStatusColor(currentStatus).second
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = getStatusColor(currentStatus).second,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("PENDIENTE", "APROBADO", "RECHAZADO").forEach { status ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        status,
                                        color = getStatusColor(status).second,
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                onClick = {
                                    expanded = false
                                    onStatusChanged(payment.id, status)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog para ver imagen ampliada
    if (showImageDialog && !comprobanteUrl.isNullOrEmpty() && comprobanteUrl.startsWith("http")) {
        Dialog(onDismissRequest = { showImageDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = comprobanteUrl,
                        contentDescription = "Comprobante ampliado",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    TextButton(
                        onClick = { showImageDialog = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Text("Cerrar", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    valueBold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (valueBold) FontWeight.Bold else FontWeight.Normal,
            color = valueColor
        )
    }
}

private fun getStatusColor(status: String): Pair<Color, Color> {
    return when (status.uppercase()) {
        "PENDIENTE" -> Color(0xFFFFF3E0) to Color(0xFFE65100)
        "APROBADO", "COMPLETADO" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "RECHAZADO", "CANCELADO" -> Color(0xFFFFEBEE) to Color(0xFFC62828)
        else -> Color(0xFFF5F5F5) to Color(0xFF666666)
    }
}

fun formatDate(date: Any?): String {
    return when (date) {
        null -> "No especificada"
        is String -> {
            try {
                val parsedDate =
                    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()).parse(date)
                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(parsedDate!!)
            } catch (e: Exception) {
                try {
                    val parsedDate =
                        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(date)
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(parsedDate!!)
                } catch (e: Exception) {
                    date.toString().take(16).replace("T", " ")
                }
            }
        }

        else -> "Fecha inválida"
    }
}
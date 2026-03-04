package com.example.illapus.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.illapus.data.model.OpinionResponse
import com.example.illapus.ui.viewmodel.OpinionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(
    activityId: String = "",
    activityName: String = "Actividad",
    onBackPressed: () -> Unit = {},
    opinionViewModel: OpinionViewModel = viewModel()
) {
    val uiState by opinionViewModel.uiState.collectAsState()
    val commentText by opinionViewModel.commentText.collectAsState()
    val selectedRating by opinionViewModel.selectedRating.collectAsState()
    val showError by opinionViewModel.showError.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Cargar opiniones al entrar a la pantalla
    LaunchedEffect(activityId) {
        val id = activityId.toIntOrNull()
        if (id != null) {
            opinionViewModel.loadOpiniones(id)
        }
    }

    // Mostrar snackbar de éxito
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            opinionViewModel.clearSuccessMessage()
        }
    }

    // Mostrar snackbar de error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            opinionViewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Opiniones") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // ══════════════════════════════════════
            // Resumen de calificación estilo Airbnb
            // ══════════════════════════════════════
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFF222222),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = String.format("%.2f", uiState.promedioCalificacion),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF222222)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("·", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${uiState.totalOpiniones} evaluaciones",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF222222)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ══════════════════════════════════════
            // Formulario para nueva opinión
            // ══════════════════════════════════════
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "¿Qué te pareció $activityName?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = commentText,
                    onValueChange = { opinionViewModel.onCommentTextChange(it) },
                    label = { Text("Escribe tu opinión...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isSubmitting
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Tu calificación",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (i in 1..5) {
                        IconButton(
                            onClick = { opinionViewModel.onRatingChange(i) },
                            modifier = Modifier.size(40.dp),
                            enabled = !uiState.isSubmitting
                        ) {
                            Icon(
                                imageVector = if (i <= selectedRating)
                                    Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Estrella $i",
                                tint = if (i <= selectedRating)
                                    Color(0xFFFFB800) else Color(0xFFCCCCCC),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                if (showError) {
                    Text(
                        text = "Debes escribir un comentario y dar una calificación",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val id = activityId.toIntOrNull()
                        if (id != null) {
                            opinionViewModel.submitOpinion(id)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !uiState.isSubmitting
                ) {
                    if (uiState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enviando...")
                    } else {
                        Text("Enviar opinión", modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ══════════════════════════════════════
            // Lista de opiniones
            // ══════════════════════════════════════
            Text(
                text = "Todas las opiniones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.opiniones.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aún no hay opiniones. ¡Sé el primero!",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                uiState.opiniones.forEach { opinion ->
                    OpinionItem(opinion = opinion)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Item de opinión con datos reales del backend ──
@Composable
private fun OpinionItem(opinion: OpinionResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Avatar + Nombre
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Avatar con inicial del nombre
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (opinion.nombreUsuario ?: "U").first().uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = opinion.nombreUsuario ?: "Usuario",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Estrellas + fecha
        Row(verticalAlignment = Alignment.CenterVertically) {
            repeat(5) { index ->
                Icon(
                    imageVector = if (index < (opinion.calificacion ?: 0))
                        Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = if (index < (opinion.calificacion ?: 0))
                        Color(0xFF222222) else Color(0xFFDDDDDD),
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "· ${OpinionViewModel.formatTimeAgo(opinion.fechaCreacion)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Comentario
        Text(
            text = opinion.comentario ?: "",
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        HorizontalDivider(color = Color(0xFFF0F0F0))
    }
}
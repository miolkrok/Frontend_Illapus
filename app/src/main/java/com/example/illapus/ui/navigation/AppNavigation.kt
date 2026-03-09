package com.example.illapus.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.illapus.R
import com.example.illapus.ui.screen.*
import com.example.illapus.ui.theme.BluePrimary
import com.example.illapus.ui.theme.BlueSecondary
import com.example.illapus.ui.theme.White
import com.example.illapus.ui.viewmodel.ActivityViewModel
import com.example.illapus.utils.AuthManager
import com.example.illapus.utils.TokenManager

object AppDestinations {
    const val MAIN_ROUTE = "main"
    const val SPLASH_ROUTE = "splash"
    const val LOGIN_ROUTE = "login"
    const val REGISTER_ROUTE = "register"
    const val GENERIC_SCREEN_ROUTE = "generic"
    const val EDIT_ACTIVITY_PREFIX = "edit_activity"
    const val EDIT_ACTIVITY_ROUTE = "$EDIT_ACTIVITY_PREFIX/{activityId}"
    const val ACTIVITY_DETAILS_PREFIX = "activity_details"
    const val ACTIVITY_DETAILS_ROUTE = "$ACTIVITY_DETAILS_PREFIX/{activityId}"
    const val RESERVATIONS_ROUTE = "reservations"

    //Comentarios de cada viaje.
    const val COMMENTS_PREFIX = "comments"
    const val COMMENTS_ROUTE = "$COMMENTS_PREFIX/{activityId}/{activityName}"

    const val PAYMENTS_ROUTE = "payments"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    var isHostMode by remember { mutableStateOf(false) }

    // Crear el TripsViewModel una sola vez a nivel de navegación
    val activitiesViewModel: ActivityViewModel = viewModel()

    // Escuchar el estado de autenticación del AuthManager
    val shouldNavigateToLogin by AuthManager.shouldNavigateToLogin.collectAsState()

    // Manejar la navegación automática al login cuando expire la sesión
    LaunchedEffect(shouldNavigateToLogin) {
        if (shouldNavigateToLogin == true) {
            // Navegar al login y limpiar el back stack
            navController.navigate(AppDestinations.LOGIN_ROUTE) {
                popUpTo(0) { inclusive = true }
            }
            // Marcar que ya se procesó la navegación
            AuthManager.onNavigatedToLogin()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            val showBottomBar = currentRoute != AppDestinations.LOGIN_ROUTE &&
                    currentRoute != AppDestinations.REGISTER_ROUTE &&
                    currentRoute != AppDestinations.GENERIC_SCREEN_ROUTE &&
                    currentRoute != AppDestinations.SPLASH_ROUTE &&
                    !currentRoute.orEmpty().startsWith(AppDestinations.ACTIVITY_DETAILS_PREFIX) &&
                    !currentRoute.orEmpty().startsWith(AppDestinations.COMMENTS_PREFIX)

            if (showBottomBar) {
                NavigationBar(
                    containerColor = White,
                    contentColor = BluePrimary
                ) {
                    val currentDestination = navBackStackEntry?.destination
                    val itemsToShow = if (isHostMode) {
                        NavigationItems.hostModeItems
                    } else {
                        NavigationItems.items
                    }
                    itemsToShow.forEach { item ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    painter = painterResource(id = item.icon),
                                    contentDescription = item.title,
                                    tint = if (selected) BlueSecondary else BluePrimary
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    color = if (selected) BlueSecondary else BluePrimary
                                )
                            },
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestinations.SPLASH_ROUTE,
            modifier = Modifier.padding(innerPadding),
        ) {

            composable(
                route = "payments_list/{activityId}",
                arguments = listOf(navArgument("activityId") { type = NavType.StringType })
            ) { backStackEntry ->
                val activityId = backStackEntry.arguments?.getString("activityId") ?: ""
                PaymentsScreen(
                    navController = navController,
                )
            }

            // Pantalla de Splash
            composable(AppDestinations.SPLASH_ROUTE) {
                SplashScreen(
                    navController = navController,
                    onSplashFinished = {
                        // Verificar si el usuario ya está logueado
                        if (TokenManager.isLoggedIn()) {
                            // Si ya está logueado, ir directamente a la pantalla principal
                            navController.navigate(NavigationItems.Activities.route) {
                                popUpTo(AppDestinations.SPLASH_ROUTE) { inclusive = true }
                            }
                        } else {
                            // Si no está logueado, ir al login
                            navController.navigate(AppDestinations.LOGIN_ROUTE) {
                                popUpTo(AppDestinations.SPLASH_ROUTE) { inclusive = true }
                            }
                        }
                    }
                )
            }

            // Pantalla de Login
            composable(AppDestinations.LOGIN_ROUTE) {
                LoginScreen(
                    onLoginSuccess = {
                        // Cuando el login es exitoso, navegar a la pantalla principal
                        navController.navigate(NavigationItems.Activities.route) {
                            // Limpiar el back stack para que el usuario no pueda volver a la pantalla de login
                            popUpTo(AppDestinations.LOGIN_ROUTE) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        // Navegar a la pantalla de registro
                        navController.navigate(AppDestinations.REGISTER_ROUTE)
                    }
                )
            }

            // Pantalla de Registro
            composable(AppDestinations.REGISTER_ROUTE) {
                RegisterScreen(
                    onRegisterSuccess = {
                        // Cuando el registro es exitoso, navegar a la pantalla principal
                        navController.navigate(NavigationItems.Activities.route) {
                            // Limpiar el back stack para que el usuario no pueda volver a las pantallas anteriores
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToLogin = {
                        // Volver a la pantalla de login
                        navController.popBackStack()
                    }
                )
            }

            composable(NavigationItems.Activities.route) {
                ActivityScreen(
                    navController = navController,
                    viewModel = activitiesViewModel, // Pasar el ViewModel creado a nivel de navegación
                    isHostMode = isHostMode,
                    onNavigateToProfile = {
                        navController.navigate(NavigationItems.Profile.route)
                    }
                )
            }

            composable(NavigationItems.Host.route) {
                HostTripsScreen(
                    navController = navController,
                    isHostMode = isHostMode,
                    onNavigateToProfile = {
                        navController.navigate(NavigationItems.Profile.route)
                    }
                )
            }

            composable(NavigationItems.Payments.route) {
                PaymentsScreen(navController = navController)
            }

            composable(NavigationItems.Profile.route) {
                ProfileScreen(
                    onLogout = {
                        // Cuando el usuario cierre sesión, navegar a la pantalla de login
                        navController.navigate(AppDestinations.LOGIN_ROUTE) {
                            // Limpiar el back stack para que el usuario no pueda volver atrás
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    navController = navController,
                    isHostMode = isHostMode,
                    onToggleHostMode = { enabled ->
                        isHostMode = enabled
                        navController.navigate(NavigationItems.Activities.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(AppDestinations.GENERIC_SCREEN_ROUTE) {
                GenericScreen(navController = navController)
            }

            // Pantalla de Reservas
            composable(AppDestinations.RESERVATIONS_ROUTE) {
                ReservationsScreen(
                    onNavigateToComments = { actividadId ->
                        navController.navigate(
                            "${AppDestinations.COMMENTS_PREFIX}/$actividadId/${Uri.encode("Actividad")}"
                        )
                    }
                )
            }

            // Pantalla de Detalles de Actividad
            composable(
                route = AppDestinations.ACTIVITY_DETAILS_ROUTE,
                arguments = listOf(
                    navArgument("activityId") {
                        type = NavType.StringType
                        defaultValue = "default_id"
                    }
                )
            ) { backStackEntry ->
                val activityId = backStackEntry.arguments?.getString("activityId") ?: "default_id"
                ActivityDetailsScreen(
                    activityId = activityId,
                    onBackPressed = { navController.popBackStack() },
                    onReservationRequested = {
                        // Implementar la navegación a la pantalla de reserva cuando esté disponible
                    },
                    onViewAllComments = { id, name ->
                        navController.navigate(
                            "${AppDestinations.COMMENTS_PREFIX}/$id/${Uri.encode(name)}"
                        )
                    }
                )
            }

            // ── NUEVA: Pantalla de Comentarios/Opiniones ──
            composable(
                route = AppDestinations.COMMENTS_ROUTE,
                arguments = listOf(
                    navArgument("activityId") {
                        type = NavType.StringType
                        defaultValue = "default_id"
                    },
                    navArgument("activityName") {
                        type = NavType.StringType
                        defaultValue = "Actividad"
                    }
                )
            ) { backStackEntry ->
                val activityId = backStackEntry.arguments?.getString("activityId") ?: "default_id"
                val activityName =
                    backStackEntry.arguments?.getString("activityName") ?: "Actividad"

                CommentsScreen(
                    activityId = activityId,
                    activityName = activityName,
                    onBackPressed = { navController.popBackStack() }
                )
            }

            // Pantalla para Editar Actividad
            composable(
                route = AppDestinations.EDIT_ACTIVITY_ROUTE,
                arguments = listOf(
                    navArgument("activityId") {
                        type = NavType.StringType
                        defaultValue = "default_id"
                    }
                )
            ) { backStackEntry ->
                val activityId = backStackEntry.arguments?.getString("activityId") ?: "default_id"
                EditActivityScreen(
                    activityId = activityId,
                    onBackPressed = { navController.popBackStack() }
                )
            }
        }
    }
}

/**
 * Definición de los elementos de navegación
 */
object NavigationItems {
    val Activities = NavigationItem("activities", "Actividades", R.drawable.ic_activities)
    val Reservations = NavigationItem("reservations", "Reservas", R.drawable.ic_reservations)
    val Payments = NavigationItem("payments", "Pagos", R.drawable.ic_payment)
    val Host = NavigationItem("host", "Operador", R.drawable.ic_home)
    val Profile = NavigationItem("profile", "Perfil", R.drawable.ic_profile)

    val items = listOf(Activities, Reservations, Profile)
    val hostModeItems = listOf(Host, Payments, Profile)
}

/**
 * Clase que representa un ítem de navegación
 */
data class NavigationItem(
    val route: String,
    val title: String,
    val icon: Int
)

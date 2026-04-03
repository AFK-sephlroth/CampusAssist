package com.example.campusassist.ui.navigation

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.campusassist.ui.screens.*
import com.example.campusassist.ui.viewmodel.*

sealed class Screen(val route: String) {
    object Login         : Screen("login")
    object Register      : Screen("register")
    object TicketList    : Screen("ticket_list")
    object CreateTicket  : Screen("create_ticket")
    object Profile       : Screen("profile")
    object Notifications : Screen("notifications")
    object TicketDetail  : Screen("ticket_detail/{ticketId}") {
        fun createRoute(id: Long) = "ticket_detail/$id"
    }
}

@Composable
fun AppNavigation() {
    val navController   = rememberNavController()
    val authViewModel   : AuthViewModel        = hiltViewModel()
    val ticketViewModel : TicketViewModel       = hiltViewModel()
    val notifViewModel  : NotificationViewModel = hiltViewModel()
    val themeViewModel  : ThemeViewModel        = hiltViewModel()
    val syncViewModel   : SyncViewModel         = hiltViewModel()

    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            authState.currentUser?.let {
                notifViewModel.setUser(it.id)
                syncViewModel.setUser(it.id)   // start periodic sync
            }
            navController.navigate(Screen.TicketList.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    val startDestination = if (authState.isLoggedIn) Screen.TicketList.route else Screen.Login.route

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onNavigateToRegister = { navController.navigate(Screen.Register.route) }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TicketList.route) {
            TicketListScreen(
                viewModel = ticketViewModel,
                onCreateTicket = { navController.navigate(Screen.CreateTicket.route) },
                onTicketClick = { ticketId ->
                    navController.navigate(Screen.TicketDetail.createRoute(ticketId))
                },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                notifViewModel = notifViewModel,
                syncViewModel = syncViewModel,
                currentUser = authState.currentUser
            )
        }

        composable(Screen.CreateTicket.route) {
            CreateTicketScreen(
                viewModel = ticketViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.TicketDetail.route,
            arguments = listOf(navArgument("ticketId") { type = NavType.LongType })
        ) { backStack ->
            val id = backStack.arguments?.getLong("ticketId") ?: return@composable
            TicketDetailScreen(
                ticketId = id,
                viewModel = ticketViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            authState.currentUser?.let { user ->
                ProfileScreen(
                    user = user,
                    authViewModel = authViewModel,
                    themeViewModel = themeViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        composable(Screen.Notifications.route) {
            NotificationScreen(
                viewModel = notifViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
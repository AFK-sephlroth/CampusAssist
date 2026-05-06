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
    val authViewModel      : AuthViewModel        = hiltViewModel()
    val ticketViewModel    : TicketViewModel       = hiltViewModel()
    val notifViewModel     : NotificationViewModel = hiltViewModel()
    val themeViewModel     : ThemeViewModel        = hiltViewModel()
    val syncViewModel      : SyncViewModel         = hiltViewModel()
    val departmentViewModel: DepartmentViewModel   = hiltViewModel()

    val authState by authViewModel.authState.collectAsState()

    // FIX: Do not show anything while the session check is still running.
    // Without this guard, NavHost mounts with startDestination = Login, then
    // immediately gets replaced — causing a visible flash and, more
    // importantly, a stale back-stack that fights the auth redirect.
    if (authState.isLoading) return

    // FIX: key() forces NavHost to be fully torn down and recreated whenever
    // the login state flips.  This gives us a clean, empty back-stack on both
    // login AND logout — no residual entries that could block navigation to
    // Register (or anywhere else) from the unauthenticated start destination.
    key(authState.isLoggedIn) {
        val navController = rememberNavController()

        // FIX: Removed the LaunchedEffect(authState.isLoggedIn) block entirely.
        //
        // The old LaunchedEffect had two problems:
        //
        //  1. Its key (authState.isLoggedIn) only changes value once per
        //     login/logout cycle.  After logout the effect fires and the else-
        //     branch runs.  When the user later taps "Sign Up", isLoggedIn is
        //     still false — the key has not changed — so the effect does NOT
        //     re-run.  The guard inside it read currentBackStackEntry *before*
        //     the Register navigation had settled, so it incorrectly sent the
        //     user back to Login.
        //
        //  2. Driving navigation reactively from a boolean observer is fragile;
        //     the timing of coroutine dispatch vs. Compose recomposition makes
        //     currentBackStackEntry unreliable as a guard.
        //
        // The key() wrapper above is the correct solution: when isLoggedIn
        // flips, the entire NavHost (and its navController) are discarded and
        // rebuilt from the new startDestination.  No extra navigation calls are
        // needed, and the Register route is always reachable from the
        // unauthenticated graph.

        LaunchedEffect(authState.isLoggedIn) {
            if (authState.isLoggedIn) {
                authState.currentUser?.let {
                    notifViewModel.setUser(it.username)
                    syncViewModel.setUser(it.username)
                }
            }
        }

        val startDestination =
            if (authState.isLoggedIn) Screen.TicketList.route else Screen.Login.route

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
                    viewModel            = ticketViewModel,
                    onCreateTicket       = { navController.navigate(Screen.CreateTicket.route) },
                    onTicketClick        = { ticketId ->
                        navController.navigate(Screen.TicketDetail.createRoute(ticketId))
                    },
                    onProfileClick       = { navController.navigate(Screen.Profile.route) },
                    onNotificationsClick = { navController.navigate(Screen.Notifications.route) },
                    notifViewModel       = notifViewModel,
                    syncViewModel        = syncViewModel,
                    currentUser          = authState.currentUser,
                    departmentViewModel  = departmentViewModel,
                )
            }

            composable(Screen.CreateTicket.route) {
                CreateTicketScreen(
                    viewModel           = ticketViewModel,
                    departmentViewModel = departmentViewModel,
                    onNavigateBack      = { navController.popBackStack() }
                )
            }

            composable(
                route     = Screen.TicketDetail.route,
                arguments = listOf(navArgument("ticketId") { type = NavType.LongType })
            ) { backStack ->
                val id = backStack.arguments?.getLong("ticketId") ?: return@composable
                TicketDetailScreen(
                    ticketId            = id,
                    viewModel           = ticketViewModel,
                    departmentViewModel = departmentViewModel,
                    onNavigateBack      = { navController.popBackStack() }
                )
            }

            composable(Screen.Profile.route) {
                authState.currentUser?.let { user ->
                    ProfileScreen(
                        user           = user,
                        authViewModel  = authViewModel,
                        themeViewModel = themeViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }

            composable(Screen.Notifications.route) {
                NotificationScreen(
                    viewModel      = notifViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
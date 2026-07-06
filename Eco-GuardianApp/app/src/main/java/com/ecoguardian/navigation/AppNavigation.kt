package com.ecoguardian.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ecoguardian.data.SupabaseClient
import com.ecoguardian.ui.LoginScreen
import com.ecoguardian.ui.screens.AdminPanelScreen
import com.ecoguardian.ui.screens.UserHomeScreen
import com.ecoguardian.viewmodel.AuthState
import com.ecoguardian.viewmodel.AuthViewModel
import com.ecoguardian.viewmodel.UserReportsViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.checkSession()
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.NavigateToUser -> {
                navController.navigate(Routes.USER_HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
            is AuthState.NavigateToAdmin -> {
                navController.navigate(Routes.ADMIN_PANEL) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
            else -> {}
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToUser = {
                    navController.navigate(Routes.USER_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate(Routes.ADMIN_PANEL) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onForgotPassword = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                },
                viewModel = authViewModel
            )
        }

        // User home screen
        composable(Routes.USER_HOME) {
            val userReportsViewModel = remember { UserReportsViewModel(SupabaseClient.client) }
            UserHomeScreen(viewModel = userReportsViewModel)
        }

        // Admin panel
        composable(Routes.ADMIN_PANEL) {
            AdminPanelScreen()
        }

        // AI report screen
        composable(Routes.AI_REPORT) {
            // placeholder until the screen is built by the team
        }

        // Forgot password screen
        composable(Routes.FORGOT_PASSWORD) {
            // placeholder until the screen is built by the team
        }
    }
}
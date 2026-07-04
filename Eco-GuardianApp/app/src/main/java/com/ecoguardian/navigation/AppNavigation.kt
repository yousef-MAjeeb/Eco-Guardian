package com.ecoguardian.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ecoguardian.ui.LoginScreen
import com.ecoguardian.ui.screens.AdminPanelScreen
import com.ecoguardian.ui.screens.UserHomeScreen
import com.ecoguardian.viewmodel.AuthState
import com.ecoguardian.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    // Check if a session already exists when the app launches.
    // If logged in, skips the login screen and goes directly to the correct home screen.
    LaunchedEffect(Unit) {
        authViewModel.checkSession()
    }

    // Navigate based on auth state changes from checkSession()
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
        startDestination = Routes.USER_HOME
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
            UserHomeScreen()
        }



        // User home screen
        composable(Routes.USER_HOME) {
            UserHomeScreen()
        }

        // Admin panel
        composable(Routes.ADMIN_PANEL) {
            AdminPanelScreen()
        }

        // AI report screen — shown after AI generates a report before submission
        composable(Routes.AI_REPORT) {
            // placeholder until the screen is built by the team
        }

        // Forgot password screen — placeholder until built
        composable(Routes.FORGOT_PASSWORD) {
            // placeholder until the screen is built by the team
        }
    }
}
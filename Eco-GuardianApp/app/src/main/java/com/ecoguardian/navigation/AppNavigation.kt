package com.ecoguardian.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ecoguardian.data.SupabaseClient
import com.ecoguardian.ui.LoginScreen
import com.ecoguardian.ui.screens.AdminPanelScreen
import com.ecoguardian.ui.screens.AiReportScreen
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

            UserHomeScreen(
                viewModel = userReportsViewModel,
                onNavigateToAiReport = { uri ->
                    val encodedUri = Uri.encode(uri.toString())
                    navController.navigate(Routes.createAiReportRoute(encodedUri))
                },
                // إضافة لوجيك تسجيل الخروج هنا
                onLogoutClick = {
                    // التطبيق هينتظر الـ logout يخلص، وبعدين ينفذ الـ navigate
                    authViewModel.logout(onSuccess = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    })
                }
            )
        }
//        composable(Routes.USER_HOME) {
//            val userReportsViewModel = remember { UserReportsViewModel(SupabaseClient.client) }
//            UserHomeScreen(
//                viewModel = userReportsViewModel,
//                onNavigateToAiReport = { uri ->
//                    val encodedUri = Uri.encode(uri.toString())
//                    navController.navigate(Routes.createAiReportRoute(encodedUri))
//                }
//            )
//        }

        // Admin panel
        composable(Routes.ADMIN_PANEL) {
            AdminPanelScreen(
                onLogoutClick = {
                    // التطبيق هينتظر الـ logout يخلص، وبعدين ينفذ الـ navigate
                    authViewModel.logout(onSuccess = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    })
                }
            )
        }

        // AI report screen
        composable(
            route = Routes.AI_REPORT,
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            val imageUri = Uri.parse(Uri.decode(encodedUri))
            val userId = authViewModel.getCurrentUserId() ?: ""

            AiReportScreen(
                imageUri = imageUri,
                userId = userId,
                onSubmitted = {
                    navController.popBackStack()
                }
            )
        }

        // Forgot password screen
        composable(Routes.FORGOT_PASSWORD) {
            // placeholder until the screen is built by the team
        }
    }
}
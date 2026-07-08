package com.ecoguardian.navigation

// Centralised route constants for the NavHost.
// Always use these constants for navigation instead of hardcoding strings,
// so renaming a route only requires a change in one place.
object Routes {
    const val LOGIN = "login"
    const val USER_HOME = "userHome"
    const val ADMIN_PANEL = "adminPanel"
    const val AI_REPORT = "aiReport/{imageUri}"
    fun createAiReportRoute(uri: String) = "aiReport/$uri"
    const val FORGOT_PASSWORD = "forgotPassword" // placeholder, screen not built yet

}
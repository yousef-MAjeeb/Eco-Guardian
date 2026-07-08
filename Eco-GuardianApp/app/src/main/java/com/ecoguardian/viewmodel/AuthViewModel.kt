package com.ecoguardian.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ecoguardian.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Represents all possible states of the authentication flow.
// Idle = no action, Loading = waiting for response,
// NavigateToUser/Admin = login succeeded, Error = something went wrong.
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class NavigateToUser(val message: String = "") : AuthState()
    data class NavigateToAdmin(val message: String = "") : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel(){
    private val repository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    // Logs in an existing user with email and password
    // On success, fetches the user's role and navigates to the correct screen (user or admin)
    // On failure, emits an Error state with the error message
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.login(email, password)
                val role = repository.getUserRole()
                if (role == "admin") {
                    _authState.value = AuthState.NavigateToAdmin()
                } else {
                    _authState.value = AuthState.NavigateToUser()
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Login failed")
            }
        }
    }

    // Registers a new user with email and password
    // On success, navigates to the user home screen
    // On failure, emits an Error state with the error message
    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                repository.register(email, password)
                _authState.value = AuthState.NavigateToUser()
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Registration failed")
            }
        }
    }

    // Signs the current user out of Supabase
    // Resets the auth state back to Idle on success
    fun logout() {
        viewModelScope.launch {
            try {
                repository.logout()
                _authState.value = AuthState.Idle
            } catch (e: Exception) {
                _authState.value = AuthState.Error(e.message ?: "Logout failed")
            }
        }
    }

    // Checks if a user session already exists when the app starts.
    // If logged in, fetches their role and navigates to the correct screen automatically.
    // If not logged in, resets state to Idle so the login screen shows.
    fun checkSession() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                if (repository.isLoggedIn()) {
                    val role = repository.getUserRole()
                    if (role == "admin") {
                        _authState.value = AuthState.NavigateToAdmin()
                    } else {
                        _authState.value = AuthState.NavigateToUser()
                    }
                } else {
                    _authState.value = AuthState.Idle
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Idle
            }
        }
    }

    fun getCurrentUserId(): String? {
        return repository.getCurrentUserId()
    }
}
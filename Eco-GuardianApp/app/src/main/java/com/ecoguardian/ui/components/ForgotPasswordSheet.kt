package com.ecoguardian.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ecoguardian.viewmodel.AuthState
import com.ecoguardian.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordSheet(
    // Pre-filled with whatever email the user typed on the login screen
    initialEmail: String,
    onDismiss: () -> Unit,
    viewModel: AuthViewModel
) {
    val authState by viewModel.authState.collectAsState()

    // 1 = enter email, 2 = enter code + new password
    var step by remember { mutableStateOf(1) }

    var email by remember { mutableStateOf(initialEmail) }
    var code by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    // Guards against reacting to stale authState left over from login/register
    var requestInFlight by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (!requestInFlight) return@LaunchedEffect
        when (val state = authState) {
            is AuthState.Loading -> {
                isLoading = true
            }
            is AuthState.OtpSent -> {
                isLoading = false
                requestInFlight = false
                step = 2
            }
            is AuthState.PasswordResetSuccess -> {
                isLoading = false
                requestInFlight = false
                isSuccess = true
            }
            is AuthState.Error -> {
                isLoading = false
                requestInFlight = false
                error = state.message
            }
            else -> {}
        }
    }

    fun closeSheet() {
        viewModel.resetAuthState()
        onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = { closeSheet() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
            )

            when {
                isSuccess -> {
                    Text(
                        text = "Password Reset",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Your password has been updated. You can now log in.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { closeSheet() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done")
                    }
                }

                step == 1 -> {
                    Text(
                        text = "Reset Password",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Enter your email and we'll send you a 6-digit code.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            error = null
                        },
                        label = { Text("Email address") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        isError = error != null,
                        supportingText = {
                            if (error != null) {
                                Text(text = error!!, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )

                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = {
                                error = null
                                requestInFlight = true
                                viewModel.requestPasswordReset(email)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = email.isNotBlank()
                        ) {
                            Text("Send Code")
                        }
                    }
                }

                else -> {
                    Text(
                        text = "Enter Code",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "We sent a 6-digit code to $email. Enter it below along with your new password.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = code,
                        onValueChange = {
                            code = it
                            error = null
                        },
                        label = { Text("6-digit code") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                            error = null
                        },
                        label = { Text("New password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            error = null
                        },
                        label = { Text("Confirm new password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        isError = error != null,
                        supportingText = {
                            if (error != null) {
                                Text(text = error!!, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )

                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Button(
                            onClick = {
                                when {
                                    newPassword.length < 6 -> {
                                        error = "Password must be at least 6 characters"
                                    }
                                    newPassword != confirmPassword -> {
                                        error = "Passwords do not match"
                                    }
                                    else -> {
                                        error = null
                                        requestInFlight = true
                                        viewModel.verifyOtpAndResetPassword(email, code, newPassword)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = code.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank()
                        ) {
                            Text("Reset Password")
                        }

                        TextButton(onClick = { step = 1; error = null }) {
                            Text("Back")
                        }
                    }
                }
            }
        }
    }
}


//package com.ecoguardian.ui.components
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import com.ecoguardian.data.AuthRepository
//import kotlinx.coroutines.launch
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ForgotPasswordSheet(
//    // Pre-filled with whatever email the user typed on the login screen
//    initialEmail: String,
//    onDismiss: () -> Unit
//) {
//    val repository = remember { AuthRepository() }
//    val scope = rememberCoroutineScope()
//
//    var email by remember { mutableStateOf(initialEmail) }
//    var isLoading by remember { mutableStateOf(false) }
//    var isSuccess by remember { mutableStateOf(false) }
//    var error by remember { mutableStateOf<String?>(null) }
//
//    ModalBottomSheet(
//        onDismissRequest = onDismiss,
//        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
//        containerColor = MaterialTheme.colorScheme.surface
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 24.dp)
//                .padding(bottom = 32.dp),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // Handle
//            Box(
//                modifier = Modifier
//                    .width(40.dp)
//                    .height(4.dp)
//            )
//
//            Text(
//                text = "Reset Password",
//                style = MaterialTheme.typography.titleLarge,
//                color = MaterialTheme.colorScheme.onSurface
//            )
//
//            Text(
//                text = "Enter your email and we'll send you a reset link.",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant,
//                textAlign = TextAlign.Center
//            )
//
//            if (isSuccess) {
//                // Success state
//                Text(
//                    text = "Reset link sent — check your inbox.",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.primary,
//                    textAlign = TextAlign.Center
//                )
//                Spacer(modifier = Modifier.height(8.dp))
//                Button(
//                    onClick = onDismiss,
//                    modifier = Modifier.fillMaxWidth(),
//                    shape = RoundedCornerShape(12.dp)
//                ) {
//                    Text("Done")
//                }
//            } else {
//                // Input state
//                OutlinedTextField(
//                    value = email,
//                    onValueChange = {
//                        email = it
//                        error = null
//                    },
//                    label = { Text("Email address") },
//                    singleLine = true,
//                    shape = RoundedCornerShape(12.dp),
//                    modifier = Modifier.fillMaxWidth(),
//                    isError = error != null,
//                    supportingText = {
//                        if (error != null) {
//                            Text(
//                                text = error!!,
//                                color = MaterialTheme.colorScheme.error
//                            )
//                        }
//                    }
//                )
//
//                if (isLoading) {
//                    CircularProgressIndicator()
//                } else {
//                    Button(
//                        onClick = {
//                            scope.launch {
//                                isLoading = true
//                                error = null
//                                try {
//                                    repository.sendPasswordReset(email)
//                                    isSuccess = true
//                                } catch (e: Exception) {
//                                    error = e.message ?: "Failed to send reset link"
//                                } finally {
//                                    isLoading = false
//                                }
//                            }
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(52.dp),
//                        shape = RoundedCornerShape(12.dp),
//                        enabled = email.isNotBlank()
//                    ) {
//                        Text("Send Reset Link")
//                    }
//                }
//            }
//        }
//    }
//}
package com.example.ritik_2.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.ritik_2.R
import com.example.ritik_2.ui.theme.Ritik_2Theme

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginScreen(
    onLoginClick: (String, String) -> Unit = { _, _ -> },
    onRegisterClick: () -> Unit = {},
    onForgotPasswordClick: (String, (Boolean) -> Unit) -> Unit = { _, _ -> }
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) } // Controls Mini-Window

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { focusManager.clearFocus() }, // Hide keyboard on outside click
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(scrollState) // Make it scrollable
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "App Logo",
                modifier = Modifier.size(120.dp) // Removed background
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Input
            CustomTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email",
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Password Input
            CustomTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Password",
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.clickable { isPasswordVisible = !isPasswordVisible }
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }) // Hide keyboard on Done
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Login Button with Animation
            Button(
                onClick = {
                    isLoading = true
                    onLoginClick(email, password)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
            ) {
                AnimatedVisibility(visible = isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                }
                Text("Login", color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Forgot Password and Register Buttons Aligned Parallel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = { showForgotPasswordDialog = true }) { // Opens mini window
                    Text("Forgot Password?", color = Color.Red)
                }

                OutlinedButton(
                    onClick = onRegisterClick,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Create Account")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Mini-Window (Forgot Password)
    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            onDismiss = { showForgotPasswordDialog = false },
            onSendResetLink = { email, callback ->
                onForgotPasswordClick(email, callback)
            }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onSendResetLink: (String, (Boolean) -> Unit) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var isSuccess by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Password") },
        text = {
            Column {
                Text("Enter your email to receive a password reset link.")
                Spacer(modifier = Modifier.height(8.dp))
                CustomTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email"
                )
            }
        },
        confirmButton = {
            AnimatedContent(targetState = isSending, transitionSpec = { fadeIn() with fadeOut() }) { sending ->
                if (sending) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Button(onClick = {
                        isSending = true
                        onSendResetLink(email) { success ->
                            isSuccess = success
                            isSending = false
                        }
                    }) {
                        Text("Send Link")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        placeholder = { Text(placeholder) },
        shape = RoundedCornerShape(12.dp),
        visualTransformation = visualTransformation,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent, // Remove underline
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    Ritik_2Theme {
        LoginScreen()
    }
}

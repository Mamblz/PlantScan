package com.example.plantscan.Presentation.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.plantscan.Presentation.Navigation.NavigationRoutes
import com.example.plantscan.ui.theme.Montserrat
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    val greenColor = Color(0xFF2E7D32)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.TopStart)
                .background(Color.White, shape = CircleShape)
                .size(40.dp)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color.Black)
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.Center),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Восстановление пароля",
                    fontSize = 22.sp,
                    fontFamily = Montserrat,
                    color = Color(0xFF333333)
                )

                val textFieldModifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)

                Text("Email", fontFamily = Montserrat)
                OutlinedTextField(
                    value = viewModel.email,
                    onValueChange = { viewModel.email = it },
                    shape = RoundedCornerShape(12.dp),
                    modifier = textFieldModifier,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                    isError = viewModel.email.isNotEmpty() &&
                            !android.util.Patterns.EMAIL_ADDRESS.matcher(viewModel.email).matches(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor,
                        unfocusedBorderColor = greenColor,
                        cursorColor = greenColor,
                        errorBorderColor = Color.Red
                    )
                )

                Text("Код восстановления", fontFamily = Montserrat)
                OutlinedTextField(
                    value = viewModel.recoveryCode,
                    onValueChange = { viewModel.recoveryCode = it },
                    shape = RoundedCornerShape(12.dp),
                    modifier = textFieldModifier,
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor,
                        unfocusedBorderColor = greenColor,
                        cursorColor = greenColor
                    )
                )

                Text("Новый пароль", fontFamily = Montserrat)
                OutlinedTextField(
                    value = viewModel.newPassword,
                    onValueChange = { viewModel.newPassword = it },
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = textFieldModifier,
                    singleLine = true,
                    isError = viewModel.newPassword.isNotEmpty() && viewModel.newPassword.length < 6,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor,
                        unfocusedBorderColor = greenColor,
                        cursorColor = greenColor,
                        errorBorderColor = Color.Red
                    )
                )

                Text("Повторите пароль", fontFamily = Montserrat)
                OutlinedTextField(
                    value = viewModel.confirmPassword,
                    onValueChange = { viewModel.confirmPassword = it },
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = textFieldModifier,
                    singleLine = true,
                    isError = viewModel.confirmPassword.isNotEmpty() &&
                            viewModel.confirmPassword != viewModel.newPassword,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor,
                        unfocusedBorderColor = greenColor,
                        cursorColor = greenColor,
                        errorBorderColor = Color.Red
                    )
                )

                viewModel.errorMessage?.let { Text(it, color = Color.Red, fontSize = 14.sp) }
                viewModel.successMessage?.let { msg ->
                    Text(msg, color = greenColor, fontSize = 16.sp)
                    LaunchedEffect(msg) {
                        delay(2000)
                        navController.navigate(NavigationRoutes.SignInScreen) {
                            popUpTo(NavigationRoutes.ForgotPasswordScreen) { inclusive = true }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.resetPassword() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = greenColor),
                    enabled = !viewModel.isLoading
                ) {
                    if (viewModel.isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Сбросить пароль", color = Color.White, fontFamily = Montserrat)
                    }
                }
            }
        }
    }
}

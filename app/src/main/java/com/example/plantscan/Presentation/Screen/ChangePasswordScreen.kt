package com.example.plantscan.Presentation.Screen

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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.plantscan.Presentation.ViewModels.ChangePasswordViewModel
import com.example.plantscan.ui.theme.Montserrat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController,
    viewModel: ChangePasswordViewModel = viewModel()
) {
    val state by viewModel.uiState
    val result by viewModel.resultState.collectAsState()
    val greenColor = Color(0xFF2E7D32)

    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(result) {
        if (result is ChangePasswordViewModel.Result.Success) {
            navController.navigate("profile") {
                popUpTo("changePassword") { inclusive = true }
            }
        }
    }

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
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .align(Alignment.Center),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Смена пароля",
                    fontSize = 22.sp,
                    fontFamily = Montserrat,
                    color = Color(0xFF333333)
                )

                Text("Старый пароль", fontFamily = Montserrat)
                OutlinedTextField(
                    value = state.oldPassword,
                    onValueChange = { viewModel.updateState(state.copy(oldPassword = it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (oldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor,
                        unfocusedBorderColor = greenColor,
                        cursorColor = greenColor
                    )
                )
                state.oldPasswordError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

                Text("Новый пароль", fontFamily = Montserrat)
                OutlinedTextField(
                    value = state.newPassword,
                    onValueChange = { viewModel.updateState(state.copy(newPassword = it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (newPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor,
                        unfocusedBorderColor = greenColor,
                        cursorColor = greenColor
                    )
                )
                state.newPasswordError?.let { Text(it, color = Color.Red, fontSize = 12.sp) }

                Text("Повторите новый пароль", fontFamily = Montserrat)
                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = { viewModel.updateState(state.copy(confirmPassword = it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = state.confirmPassword.isNotEmpty() && state.confirmPassword != state.newPassword,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor,
                        unfocusedBorderColor = greenColor,
                        cursorColor = greenColor,
                        errorBorderColor = Color.Red
                    )
                )
                if (state.confirmPassword.isNotEmpty() && state.confirmPassword != state.newPassword) {
                    Text("Пароли не совпадают", color = Color.Red, fontSize = 12.sp)
                }

                when (result) {
                    is ChangePasswordViewModel.Result.Loading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = greenColor
                    )
                    is ChangePasswordViewModel.Result.Error -> Text(
                        text = (result as ChangePasswordViewModel.Result.Error).message,
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                    else -> Spacer(modifier = Modifier.height(1.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.changePassword() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = greenColor),
                    enabled = state.oldPassword.isNotBlank() &&
                            state.newPassword.length >= 6 &&
                            state.newPassword == state.confirmPassword
                ) {
                    Text(
                        "Сменить пароль",
                        color = Color.White,
                        fontFamily = Montserrat
                    )
                }
            }
        }
    }
}

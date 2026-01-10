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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.plantscan.Presentation.Navigation.NavigationRoutes
import com.example.plantscan.Presentation.ViewModels.ResultState
import com.example.plantscan.Presentation.ViewModels.SignUpViewModel
import com.example.plantscan.ui.theme.Montserrat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: SignUpViewModel = viewModel()
) {
    val state by viewModel.uiState
    val result by viewModel.resultState.collectAsState()

    val greenColor = Color(0xFF2E7D32)
    val blueColor = Color(0xFF1E88E5)
    val fieldHeight = 52.dp

    LaunchedEffect(result) {
        if (result is ResultState.Success && (result as ResultState.Success).readyToNavigate) {
            navController.navigate(NavigationRoutes.SignInScreen) {
                popUpTo(NavigationRoutes.SignUpScreen) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(horizontal = 16.dp)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.TopStart)
                .background(Color.White, shape = CircleShape)
                .size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Назад",
                tint = Color.Black
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Создание аккаунта",
                    fontSize = 22.sp,
                    fontFamily = Montserrat,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFF333333)
                )


                Text("Имя", fontFamily = Montserrat)
                OutlinedTextField(
                    value = state.firstName,
                    onValueChange = { viewModel.onFirstNameChange(it) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(fieldHeight)
                        .testTag("firstNameField"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor,
                        unfocusedBorderColor = greenColor,
                        cursorColor = greenColor
                    )
                )

                Text("Фамилия", fontFamily = Montserrat)
                OutlinedTextField(
                    value = state.lastName,
                    onValueChange = { viewModel.onLastNameChange(it) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(fieldHeight)
                        .testTag("lastNameField"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor,
                        unfocusedBorderColor = greenColor,
                        cursorColor = greenColor
                    )
                )

                Text("Email", fontFamily = Montserrat)
                OutlinedTextField(
                    value = state.email,
                    onValueChange = { viewModel.onEmailChange(it) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(fieldHeight)
                        .testTag("emailField"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = state.email.isNotEmpty() &&
                            !android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor,
                        unfocusedBorderColor = greenColor,
                        cursorColor = greenColor
                    )
                )

                Text("Номер телефона", fontFamily = Montserrat)
                OutlinedTextField(
                    value = state.phone,
                    onValueChange = { viewModel.onPhoneChange(it) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(fieldHeight)
                        .testTag("phoneField"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor,
                        unfocusedBorderColor = greenColor,
                        cursorColor = greenColor
                    )
                )

                Text("Пароль", fontFamily = Montserrat)
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { viewModel.onPasswordChange(it) },
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(fieldHeight)
                        .testTag("passwordField"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor,
                        unfocusedBorderColor = greenColor,
                        cursorColor = greenColor
                    )
                )

                Text("Повторите пароль", fontFamily = Montserrat)
                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = { viewModel.onConfirmPasswordChange(it) },
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(fieldHeight)
                        .testTag("confirmPasswordField"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = state.confirmPassword.isNotEmpty() &&
                            state.confirmPassword != state.password,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor,
                        unfocusedBorderColor = greenColor,
                        cursorColor = greenColor,
                        errorBorderColor = Color.Red
                    )
                )


                when (result) {
                    is ResultState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            color = greenColor
                        )
                    }

                    is ResultState.Success -> {
                        val message = (result as ResultState.Success).message
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Регистрация успешна!",
                                color = greenColor,
                                fontSize = 16.sp,
                                fontFamily = Montserrat
                            )
                            if (message.contains("код восстановления")) {
                                Text("Ваш код восстановления:", fontFamily = Montserrat)
                                Text(
                                    text = message.substringAfter(":").trim(),
                                    color = blueColor,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = Montserrat
                                )
                            }
                            Text(
                                text = "Вы будете перенаправлены через 20 секунд",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    is ResultState.Error -> {
                        Text(
                            text = (result as ResultState.Error).message,
                            color = Color.Red,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    else -> Unit
                }


                Button(
                    onClick = { viewModel.signUp() },
                    modifier = Modifier.fillMaxWidth().height(fieldHeight),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = greenColor),
                    enabled = state.firstName.isNotBlank() &&
                            state.lastName.isNotBlank() &&
                            state.email.isNotBlank() &&
                            state.phone.isNotBlank() &&
                            state.password.isNotBlank() &&
                            state.password == state.confirmPassword &&
                            android.util.Patterns.EMAIL_ADDRESS.matcher(state.email).matches()
                ) {
                    Text(
                        text = "Зарегистрироваться",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontFamily = Montserrat,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
    }
}
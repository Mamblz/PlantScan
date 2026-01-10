package com.example.plantscan.Presentation.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.app.domain.state.ResultState
import com.example.plantscan.Presentation.Navigation.NavigationRoutes
import com.example.plantscan.Presentation.ViewModels.SignInViewModel
import com.example.plantscan.ui.theme.Montserrat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    navController: NavController,
    viewModel: SignInViewModel = viewModel()
) {
    val uiState by viewModel.uiState
    val resultState by viewModel.resultState.collectAsState()
    val navigateToMain by viewModel.navigateToMain.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.resetNavigation()
        viewModel.resetState()
    }

    LaunchedEffect(navigateToMain) {
        if (navigateToMain) {
            navController.navigate(NavigationRoutes.MainScreen) {
                popUpTo(NavigationRoutes.SignInScreen) { inclusive = true }
            }
            viewModel.resetNavigation()
        }
    }

    val greenColor = Color(0xFF2E7D32)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Вход",
                    fontSize = 22.sp,
                    fontFamily = Montserrat,
                    fontStyle = Italic,
                    color = Color(0xFF333333)
                )

                Text(
                    "Email",
                    fontFamily = Montserrat,
                    modifier = Modifier.padding(bottom = 0.dp, top = 4.dp)
                )
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.updateEmail(it) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("emailField"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email),
                    isError = uiState.email.isNotEmpty() &&
                            !android.util.Patterns.EMAIL_ADDRESS.matcher(uiState.email).matches(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor,
                        unfocusedBorderColor = greenColor,
                        cursorColor = greenColor
                    )
                )

                Text(
                    "Пароль",
                    fontFamily = Montserrat,
                    modifier = Modifier.padding(bottom = 0.dp, top = 4.dp)
                )
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { viewModel.updatePassword(it) },
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("passwordField"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor,
                        unfocusedBorderColor = greenColor,
                        cursorColor = greenColor
                    )
                )

                when (resultState) {
                    is ResultState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = greenColor)
                        }
                    }
                    is ResultState.Error -> {
                        Text(
                            text = (resultState as ResultState.Error).message,
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                    is ResultState.Success -> {
                        Text(
                            text = (resultState as ResultState.Success).message,
                            color = greenColor,
                            fontSize = 14.sp
                        )
                    }
                    else -> {}
                }

                Button(
                    onClick = {
                        viewModel.signIn(
                            email = uiState.email,
                            password = uiState.password
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = greenColor),
                    shape = RoundedCornerShape(12.dp),
                    enabled = uiState.email.isNotEmpty() &&
                            uiState.password.isNotEmpty() &&
                            android.util.Patterns.EMAIL_ADDRESS.matcher(uiState.email).matches()
                ) {
                    Text(
                        "Войти",
                        color = Color.White,
                        fontFamily = Montserrat,
                        fontStyle = Italic
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ClickableText(
                        text = AnnotatedString("Забыли пароль?"),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = Montserrat,
                            color = greenColor,
                        ),
                        onClick = {
                            navController.navigate(NavigationRoutes.ForgotPasswordScreen)
                        }
                    )
                    ClickableText(
                        text = AnnotatedString("Регистрация"),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = Montserrat,
                            color = greenColor,
                        ),
                        onClick = {
                            navController.navigate(NavigationRoutes.SignUpScreen)
                        }
                    )
                }
            }
        }
    }
}
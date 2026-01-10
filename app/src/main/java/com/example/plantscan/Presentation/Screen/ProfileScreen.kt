package com.example.plantscan.Presentation.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.plantscan.Presentation.Navigation.NavigationRoutes
import com.example.plantscan.Presentation.ViewModels.ProfileViewModel
import com.example.plantscan.apiconnect.model.UserSession.currentEmail
import com.example.plantscan.ui.theme.Montserrat

@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel = viewModel()) {

    LaunchedEffect(currentEmail) {
        viewModel.loadUser(currentEmail)
    }

    val userName = viewModel.fullName
    val userEmail = viewModel.email
    val phone = viewModel.phone
    val isLoading = viewModel.isLoading
    val error = viewModel.error

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F2))
                .padding(padding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Профиль", fontSize = 20.sp, color = Color.Black, fontFamily = Montserrat)
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF2E7D32))
                }
                return@Column
            }

            if (error != null) {
                Text(
                    text = error,
                    color = Color.Red,
                    modifier = Modifier.padding(16.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text("Имя и фамилия", fontSize = 16.sp, color = Color.Gray, fontFamily = Montserrat)
                Text(userName, fontSize = 18.sp, color = Color.Black, fontFamily = Montserrat)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Электронная почта", fontSize = 16.sp, color = Color.Gray, fontFamily = Montserrat)
                Text(userEmail, fontSize = 18.sp, color = Color.Black, fontFamily = Montserrat)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Телефон", fontSize = 16.sp, color = Color.Gray, fontFamily = Montserrat)
                Text(phone, fontSize = 18.sp, color = Color.Black, fontFamily = Montserrat)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { navController.navigate(NavigationRoutes.PersonalDataScreen) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("personalDataButton"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Личные данные", color = Color.White, fontFamily = Montserrat)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { navController.navigate("change_password") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("changePasswordButton"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Сменить пароль", color = Color.White, fontFamily = Montserrat)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        viewModel.logout()
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("logoutButton"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("Выйти", color = Color.White, fontFamily = Montserrat)
                }
            }
        }
    }
}

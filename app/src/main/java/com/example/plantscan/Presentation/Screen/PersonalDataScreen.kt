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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.plantscan.Presentation.ViewModels.PersonalDataViewModel
import com.example.plantscan.ui.theme.Montserrat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalDataScreen(
    navController: NavController,
    viewModel: PersonalDataViewModel = viewModel()
) {
    val state by viewModel.uiState
    val result by viewModel.resultState.collectAsState()
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
                    "Личные данные",
                    fontSize = 22.sp,
                    fontFamily = Montserrat,
                    color = Color(0xFF333333)
                )

                Text("Имя", fontFamily = Montserrat)
                OutlinedTextField(
                    value = state.firstName,
                    onValueChange = { viewModel.updateState(state.copy(firstName = it)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
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
                    onValueChange = { viewModel.updateState(state.copy(lastName = it)) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
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
                    onValueChange = { viewModel.updateState(state.copy(email = it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(52.dp)
                        .testTag("emailField"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    isError = !state.emailError.isNullOrEmpty(),
                    supportingText = { state.emailError?.let { Text(it, color = Color.Red) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor,
                        unfocusedBorderColor = greenColor,
                        cursorColor = greenColor
                    )
                )

                Text("Номер телефона", fontFamily = Montserrat)
                OutlinedTextField(
                    value = state.phone,
                    onValueChange = { viewModel.updateState(state.copy(phone = it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(52.dp)
                        .testTag("phoneField"),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    isError = !state.phoneError.isNullOrEmpty(),
                    supportingText = { state.phoneError?.let { Text(it, color = Color.Red) } },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = greenColor,
                        unfocusedBorderColor = greenColor,
                        cursorColor = greenColor
                    )
                )

                when (result) {
                    is PersonalDataViewModel.Result.Loading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = greenColor
                    )
                    is PersonalDataViewModel.Result.Success -> Text(
                        text = (result as PersonalDataViewModel.Result.Success).message,
                        color = greenColor,
                        fontSize = 14.sp
                    )
                    is PersonalDataViewModel.Result.Error -> Text(
                        text = (result as PersonalDataViewModel.Result.Error).message,
                        color = Color.Red,
                        fontSize = 14.sp
                    )
                    else -> Spacer(modifier = Modifier.height(1.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.saveChanges() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("saveButton"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = greenColor),
                    enabled = state.firstName.isNotBlank() &&
                            state.lastName.isNotBlank() &&
                            state.email.isNotBlank() &&
                            state.emailError.isNullOrEmpty() &&
                            state.phoneError.isNullOrEmpty()
                ) {
                    Text(
                        "Сохранить изменения",
                        color = Color.White,
                        fontFamily = Montserrat,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

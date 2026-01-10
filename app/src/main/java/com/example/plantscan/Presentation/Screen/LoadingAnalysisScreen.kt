package com.example.plantscan.Presentation.Screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.plantscan.Presentation.Navigation.NavigationRoutes
import com.example.plantscan.Presentation.ViewModels.AnalysisViewModel
import com.example.plantscan.ui.theme.Montserrat
import java.io.File

@Composable
fun LoadingAnalysisScreen(
    navController: NavController,
    viewModel: AnalysisViewModel,
    filePath: String
) {
    val diseaseResult by viewModel.diseaseResult.collectAsState()
    val isAnalyzing by remember { derivedStateOf { viewModel.isAnalyzing } }
    val errorMessage by remember { derivedStateOf { viewModel.errorMessage } }
    val attemptCount by remember { derivedStateOf { viewModel.attemptCount } }

    val greenColor = Color(0xFF2E7D32)

    LaunchedEffect(Unit) {
        viewModel.setImage(filePath)
        viewModel.identifyPlant(File(filePath))
    }

    LaunchedEffect(diseaseResult) {
        if (diseaseResult != null) {
            navController.navigate(NavigationRoutes.AnalysisScreen) {
                popUpTo(NavigationRoutes.MainScreen) { inclusive = false }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        when {
            isAnalyzing -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = greenColor, strokeWidth = 5.dp)
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Анализируем фото...",
                        fontSize = 22.sp,
                        fontFamily = Montserrat,
                        color = Color(0xFF1B5E20)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Определяем растение и состояние",
                        fontSize = 15.sp,
                        fontFamily = Montserrat,
                        color = Color(0xFF666666)
                    )
                    if (attemptCount > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Попытка $attemptCount...",
                            fontSize = 16.sp,
                            fontFamily = Montserrat,
                            color = Color.Gray
                        )
                    }
                }
            }

            !errorMessage.isNullOrEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage!!,
                        fontFamily = Montserrat,
                        fontSize = 16.sp,
                        color = Color.Red,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.identifyPlant(File(filePath))
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = greenColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Попробовать снова",
                            fontFamily = Montserrat,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

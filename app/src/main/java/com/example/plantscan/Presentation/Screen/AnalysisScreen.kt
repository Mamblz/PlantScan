package com.example.app.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.plantscan.Presentation.Navigation.NavigationRoutes
import com.example.plantscan.Presentation.ViewModels.AnalysisViewModel
import com.example.plantscan.ui.theme.Montserrat
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    navController: NavController,
    viewModel: AnalysisViewModel = viewModel()
) {
    val disease by viewModel.diseaseResult.collectAsState()
    val imagePath = viewModel.imageUri
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 24.dp)
    ) {
        item {
            Text(
                text = "Результаты анализа",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat,
                color = Color(0xFF1B5E20)
            )
        }

        item {
            if (!imagePath.isNullOrEmpty()) {
                val file = File(imagePath)
                val painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(file)
                        .crossfade(true)
                        .build()
                )
                Image(
                    painter = painter,
                    contentDescription = "Фото листа",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Фото не загружено",
                        fontFamily = Montserrat,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        disease?.let { d ->
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item { InfoCardModern(title = "Растение", content = d.plant_name.ifEmpty { "Не удалось определить" }) }

            val isHealthy = d.diagnosis.isEmpty() || d.diagnosis.contains("здоров", ignoreCase = true)
            item {
                InfoCardModern(
                    title = "Состояние",
                    content = if (isHealthy) "Здорово" else "Болезнь",
                    contentColor = if (isHealthy) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                )
            }

            if (!isHealthy) {
                if (d.symptoms.isNotEmpty()) item { ListSectionModern("Симптомы", d.symptoms) }
                if (d.causes.isNotEmpty()) item { ListSectionModern("Возможные причины", d.causes) }
                if (d.recommendations.isNotEmpty()) item { ListSectionModern("Рекомендации", d.recommendations) }
            }

        } ?: run {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(color = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Анализируется...", fontFamily = Montserrat, fontSize = 16.sp, color = Color.Gray)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            var isSaving by remember { mutableStateOf(false) }
            var saveError by remember { mutableStateOf(false) }

            Button(
                onClick = {
                    isSaving = true
                    saveError = false
                    viewModel.viewModelScope.launch {
                        try {
                            val favSuccess = viewModel.saveToFavorites(context)
                            val histSuccess = viewModel.saveHistory(context)
                            isSaving = false

                            if (favSuccess && histSuccess) {
                                navController.navigate(NavigationRoutes.FavoritesScreen) {
                                    popUpTo(NavigationRoutes.MainScreen) { inclusive = false }
                                }
                            } else {
                                saveError = true
                            }
                        } catch (e: Exception) {
                            isSaving = false
                            saveError = true
                        }
                    }
                },
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Сохранение...", color = Color.White, fontFamily = Montserrat, fontSize = 16.sp)
                } else {
                    Text("Добавить в избранное", color = Color.White, fontFamily = Montserrat, fontSize = 16.sp)
                }
            }

            AnimatedVisibility(visible = saveError) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Не удалось сохранить. Проверьте интернет.",
                        color = Color(0xFFD32F2F),
                        fontSize = 13.sp,
                        fontFamily = Montserrat
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = {
                    viewModel.viewModelScope.launch {
                        try {
                            viewModel.saveHistory(context)
                        } catch (e: Exception) {
                        }
                        viewModel.reset()
                        navController.navigate(NavigationRoutes.MainScreen) {
                            popUpTo(NavigationRoutes.MainScreen) { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32)),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2E7D32))
                )
            ) {
                Text("Новый анализ", color = Color(0xFF2E7D32), fontFamily = Montserrat, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun InfoCardModern(title: String, content: String, contentColor: Color = Color(0xFF222222)) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF555555), fontFamily = Montserrat)
            Spacer(modifier = Modifier.height(6.dp))
            Text(content, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = contentColor, fontFamily = Montserrat)
        }
    }
}

@Composable
fun ListSectionModern(title: String, items: List<String>, highlight: Boolean = false) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if (highlight) Color(0xFFF1F8E9) else Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1B5E20), fontFamily = Montserrat)
            Spacer(modifier = Modifier.height(8.dp))
            items.forEach { item ->
                Text("• $item", fontSize = 15.sp, color = Color(0xFF555555), fontFamily = Montserrat, modifier = Modifier.padding(vertical = 2.dp))
            }
        }
    }
}

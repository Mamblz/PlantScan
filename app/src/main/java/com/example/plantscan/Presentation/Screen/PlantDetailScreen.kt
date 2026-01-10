package com.example.plantscan.Presentation.Screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.plantscan.Presentation.ViewModels.PlantDetailViewModel
import com.example.plantscan.apiconnect.model.FavoritePlant
import com.example.plantscan.ui.theme.Montserrat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    navController: NavController,
    plantId: String,
    viewModel: PlantDetailViewModel = viewModel()
) {
    val plant by viewModel.plant.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(plantId) {
        viewModel.loadPlant(plantId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Детали растения",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Назад", tint = Color(0xFF1B5E20))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color(0xFF1B5E20)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FBF5))
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            when {
                isLoading -> LoadingDetailState()
                plant == null -> ErrorDetailState()
                else -> PlantDetailContent(
                    plant = plant!!,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it }
                )
            }
        }
    }
}

@Composable
private fun LoadingDetailState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        CircularProgressIndicator(
            color = Color(0xFF2E7D32),
            strokeWidth = 5.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Загружаем данные...",
            fontFamily = Montserrat,
            fontSize = 16.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
private fun ErrorDetailState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            Icons.Default.Close,
            contentDescription = null,
            tint = Color(0xFFD32F2F),
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Растение не найдено",
            color = Color(0xFFD32F2F),
            fontFamily = Montserrat,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Попробуйте позже или вернитесь назад",
            color = Color(0xFF888888),
            fontFamily = Montserrat,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PlantDetailContent(
    plant: FavoritePlant,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .background(Color(0xFFF8FBF5))
    ) {

        if (!plant.photo_url.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(plant.photo_url),
                contentDescription = plant.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .background(Color(0xFFE8F5E8), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFAAAAAA),
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {

            Text(
                plant.name,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat,
                color = Color(0xFF1B5E20),
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Spacer(modifier = Modifier.height(16.dp))
            plant.diagnosis?.takeIf { it.isNotBlank() }?.let { diag ->
                val isHealthy = diag.contains("здоров", ignoreCase = true)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isHealthy) Color(0xFFF1F8E9) else Color(0xFFFFF3E0),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isHealthy) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isHealthy) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = diag,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = Montserrat,
                            color = if (isHealthy) Color(0xFF2E7D32) else Color(0xFFD32F2F)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            plant.description?.takeIf { it.isNotBlank() }?.let { desc ->
                Text(
                    text = desc,
                    fontSize = 16.sp,
                    color = Color(0xFF444444),
                    fontFamily = Montserrat,
                    lineHeight = 24.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (plant.symptoms.isNotEmpty()) {
                InfoSection("Симптомы", plant.symptoms)
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (plant.causes.isNotEmpty()) {
                InfoSection("Возможные причины", plant.causes)
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (plant.recommendations.isNotEmpty()) {
                InfoSection("Рекомендации", plant.recommendations)
            }
        }
    }
}

@Composable
private fun InfoSection(title: String, items: List<String>) {
    Column {
        Text(
            text = title,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Montserrat,
            color = Color(0xFF1B5E20)
        )
        Spacer(modifier = Modifier.height(8.dp))
        items.forEach { item ->
            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                Text("• ", color = Color(0xFF2E7D32), fontSize = 16.sp)
                Text(
                    text = item,
                    fontSize = 15.sp,
                    color = Color(0xFF555555),
                    fontFamily = Montserrat,
                    lineHeight = 22.sp
                )
            }
        }
    }
}

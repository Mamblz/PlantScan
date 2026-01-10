package com.example.plantscan.Presentation.Screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle.Companion.Italic
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import com.example.plantscan.Presentation.Navigation.NavigationRoutes
import com.example.plantscan.Presentation.ViewModels.HistoryViewModel
import com.example.plantscan.Presentation.ViewModels.PlantHistoryItem
import com.example.plantscan.ui.theme.Montserrat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController, viewModel: HistoryViewModel = viewModel()) {
    val historyList = viewModel.historyList
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            BottomAppBar(containerColor = Color.White) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = { navController.navigate(NavigationRoutes.MainScreen) }) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = "Главная",
                            tint = if (currentRoute == NavigationRoutes.MainScreen) Color(0xFF2E7D32) else Color(0xFF555555)
                        )
                    }

                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "История",
                            tint = if (currentRoute == NavigationRoutes.HistoryScreen) Color(0xFF2E7D32) else Color(0xFF555555)
                        )
                    }

                    IconButton(onClick = { navController.navigate(NavigationRoutes.FavoritesScreen) }) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Избранное",
                            tint = if (currentRoute == NavigationRoutes.FavoritesScreen) Color(0xFF2E7D32) else Color(0xFF555555)
                        )
                    }

                    IconButton(onClick = { navController.navigate(NavigationRoutes.ArticlesScreen) }) {
                        Icon(
                            Icons.Default.List,
                            contentDescription = "Статьи",
                            tint = if (currentRoute == NavigationRoutes.ArticlesScreen) Color(0xFF2E7D32) else Color(0xFF555555)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F2))
                .padding(padding)
                .padding(12.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "История",
                        fontSize = 20.sp,
                        fontFamily = Montserrat,
                        fontStyle = Italic,
                        color = Color(0xFF222222)
                    )

                    if (historyList.isEmpty()) {
                        Text("Нет последних растений", color = Color.Gray, fontSize = 14.sp)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(historyList) { item ->
                                HistoryItemRow(item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemRow(item: PlantHistoryItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF9F9F9), shape = RoundedCornerShape(10.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (item.photoUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(item.photoUrl),
                contentDescription = item.name,
                modifier = Modifier.size(60.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Фото", color = Color.DarkGray, fontSize = 14.sp, fontFamily = Montserrat)
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(item.name, fontSize = 16.sp, color = Color.Black, fontFamily = Montserrat)
            Text("Дата: ${item.dateAdded}", fontSize = 14.sp, color = Color.Gray, fontFamily = Montserrat)
        }
    }
}
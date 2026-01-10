package com.example.plantscan.Presentation.Screen

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import com.example.plantscan.Presentation.Navigation.NavigationRoutes
import com.example.plantscan.Presentation.ViewModels.FavoritesViewModel
import com.example.plantscan.apiconnect.model.FavoritePlant
import com.example.plantscan.ui.theme.Montserrat

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: FavoritesViewModel = viewModel()
) {
    val favorites by viewModel.favorites.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val error by viewModel.error.collectAsState(initial = null)
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val greenColor = Color(0xFF2E7D32)
    val fieldHeight = 50.dp

    Scaffold(
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    IconButton(onClick = { navController.navigate(NavigationRoutes.MainScreen) }) {
                        Icon(
                            Icons.Default.Home,
                            "Главная",
                            tint = if (currentRoute == NavigationRoutes.MainScreen) greenColor else Color(0xFF555555)
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Favorite,
                            "Избранное",
                            tint = if (currentRoute == NavigationRoutes.FavoritesScreen) greenColor else Color(0xFF555555)
                        )
                    }
                    IconButton(onClick = { navController.navigate(NavigationRoutes.ArticlesScreen) }) {
                        Icon(
                            Icons.Default.List,
                            "Статьи",
                            tint = if (currentRoute == NavigationRoutes.ArticlesScreen) greenColor else Color(0xFF555555)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FBF5))
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = "Избранное",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Montserrat,
                        color = greenColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    var searchQuery by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Поиск") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = searchFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )

                    when {
                        isLoading -> FavoritesLoadingState()
                        error != null -> FavoritesErrorState(error!!, onRetry = { viewModel.loadFavorites() })
                        favorites.isEmpty() -> FavoritesEmptyState()
                        else -> FavoriteList(navController, favorites.filter {
                            it.name.contains(searchQuery, ignoreCase = true)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun searchFieldColors(): TextFieldColors {
    val greenColor = Color(0xFF2E7D32)
    return TextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        disabledTextColor = Color.Gray,
        errorTextColor = Color.Red,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        errorContainerColor = Color.Transparent,
        cursorColor = greenColor,
        errorCursorColor = Color.Red,
        selectionColors = TextSelectionColors(
            handleColor = greenColor,
            backgroundColor = greenColor.copy(alpha = 0.4f)
        ),
        focusedIndicatorColor = greenColor,
        unfocusedIndicatorColor = Color(0xFFCCCCCC),
        disabledIndicatorColor = Color.Gray,
        errorIndicatorColor = Color.Red,
        focusedLeadingIconColor = greenColor,
        unfocusedLeadingIconColor = Color.Gray,
        disabledLeadingIconColor = Color.Gray,
        errorLeadingIconColor = Color.Red,
        focusedTrailingIconColor = greenColor,
        unfocusedTrailingIconColor = Color.Gray,
        disabledTrailingIconColor = Color.Gray,
        errorTrailingIconColor = Color.Red,
        focusedLabelColor = greenColor,
        unfocusedLabelColor = Color.Gray,
        disabledLabelColor = Color.Gray,
        errorLabelColor = Color.Red,
        focusedPlaceholderColor = Color.Gray,
        unfocusedPlaceholderColor = Color.Gray,
        disabledPlaceholderColor = Color.LightGray,
        errorPlaceholderColor = Color.Red,
        focusedSupportingTextColor = greenColor,
        unfocusedSupportingTextColor = Color.Gray,
        disabledSupportingTextColor = Color.LightGray,
        errorSupportingTextColor = Color.Red,
        focusedPrefixColor = greenColor,
        unfocusedPrefixColor = Color.Gray,
        disabledPrefixColor = Color.LightGray,
        errorPrefixColor = Color.Red,
        focusedSuffixColor = greenColor,
        unfocusedSuffixColor = Color.Gray,
        disabledSuffixColor = Color.LightGray,
        errorSuffixColor = Color.Red
    )
}


@Composable
private fun FavoritesLoadingState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(color = Color(0xFF2E7D32), strokeWidth = 5.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Загружаем избранное...",
            fontFamily = Montserrat,
            fontSize = 16.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
private fun FavoritesErrorState(message: String, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Build,
            contentDescription = null,
            tint = Color(0xFFD32F2F),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            message,
            color = Color(0xFFD32F2F),
            fontFamily = Montserrat,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
        ) {
            Text("Повторить", color = Color.White, fontFamily = Montserrat)
        }
    }
}

@Composable
private fun FavoritesEmptyState() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.FavoriteBorder,
            contentDescription = null,
            tint = Color(0xFFAAAAAA),
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Нет избранных растений",
            color = Color(0xFF888888),
            fontFamily = Montserrat,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun FavoriteList(navController: NavController, items: List<FavoritePlant>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(items, key = { it.id ?: it.name }) { item ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(300)) + slideInVertically(animationSpec = tween(300)) { it / 2 },
            ) {
                FavoriteItemRow(navController, item)
            }
        }
    }
}

@Composable
fun FavoriteItemRow(navController: NavController, item: FavoritePlant) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                item.id?.let { id ->
                    navController.navigate("favoriteDetail/$id")
                }
            },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
    ) {
        Column {
            if (!item.photo_url.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(item.photo_url),
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .background(Color(0xFFE8F5E8), RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFFAAAAAA),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = item.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = Montserrat,
                    color = Color(0xFF1B5E20)
                )

                item.diagnosis?.takeIf { it.isNotBlank() }?.let { diag ->
                    Spacer(modifier = Modifier.height(6.dp))
                    val isHealthy = diag.contains("здоров", ignoreCase = true)
                    Text(
                        text = diag,
                        fontSize = 14.sp,
                        color = if (isHealthy) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Medium
                    )
                }

                item.description?.takeIf { it.isNotBlank() }?.let { desc ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        fontSize = 13.sp,
                        color = Color(0xFF666666),
                        fontFamily = Montserrat,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}




package com.example.plantscan.Presentation.Screen

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import com.example.plantscan.Presentation.Navigation.NavigationRoutes
import com.example.plantscan.Presentation.ViewModels.ArticleViewModel
import com.example.plantscan.apiconnect.model.ArticleItem
import com.example.plantscan.ui.theme.Montserrat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesScreen(navController: NavController, viewModel: ArticleViewModel = viewModel()) {

    val articles by viewModel.articlesList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
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
                            "Главная",
                            tint = if (currentRoute == NavigationRoutes.MainScreen) Color(0xFF2E7D32) else Color(0xFF555555)
                        )
                    }
                    IconButton(onClick = { navController.navigate(NavigationRoutes.FavoritesScreen) }) {
                        Icon(
                            Icons.Default.Favorite,
                            "Избранное",
                            tint = if (currentRoute == NavigationRoutes.FavoritesScreen) Color(0xFF2E7D32) else Color(0xFF555555)
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.List,
                            "Статьи",
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
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Статьи",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = Montserrat,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Поиск") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = articleSearchFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(articles.filter { it.title.contains(searchQuery, true) }) { article ->
                            ArticleItemRow(navController, article)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = Color(0xFF2E7D32))
        Spacer(modifier = Modifier.height(12.dp))
        Text("Загрузка статей...", fontFamily = Montserrat, color = Color.Gray)
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text(message, fontFamily = Montserrat, color = Color.Red)
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) {
            Text("Повторить", color = Color.White, fontFamily = Montserrat)
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.List, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text("Нет доступных статей", color = Color.Gray, fontFamily = Montserrat)
    }
}

@Composable
fun ArticleList(navController: NavController, articles: List<ArticleItem>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(articles) { article ->
            ArticleItemRow(navController, article)
        }
    }
}

@Composable
fun ArticleItemRow(navController: NavController, article: ArticleItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { navController.navigate("articleDetail/${article.id}") },
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            if (!article.imageUrl.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(article.imageUrl),
                    contentDescription = article.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.LightGray, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Фото", color = Color.DarkGray, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(article.title, fontSize = 16.sp, fontFamily = Montserrat, fontWeight = FontWeight.SemiBold, color = Color(0xFF1B5E20))
            Text(article.description, fontSize = 14.sp, fontFamily = Montserrat, color = Color.Gray)
        }
    }
}

@Composable
fun articleSearchFieldColors(): TextFieldColors {
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

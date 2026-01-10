package com.example.plantscan.Presentation.Screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.plantscan.Presentation.ViewModels.ArticleViewModel
import com.example.plantscan.apiconnect.model.ArticleItem
import com.example.plantscan.ui.theme.Montserrat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    navController: NavController,
    articleId: Int,
    viewModel: ArticleViewModel = viewModel()
) {
    val articles by viewModel.articlesList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val article = articles.find { it.id == articleId }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Детали статьи", fontFamily = Montserrat, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад", tint = Color(0xFF1B5E20))
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
                article == null && errorMessage != null -> ErrorDetailState(errorMessage!!)
                article != null -> ArticleDetailContent(article)
                else -> ErrorDetailState("Статья не найдена")
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
        CircularProgressIndicator(color = Color(0xFF2E7D32), strokeWidth = 5.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Загрузка статьи...", fontFamily = Montserrat, fontSize = 16.sp, color = Color(0xFF666666))
    }
}

@Composable
private fun ErrorDetailState(message: String = "Ошибка загрузки") {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            Icons.Default.ArrowBack,
            contentDescription = null,
            tint = Color(0xFFD32F2F),
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(message, color = Color(0xFFD32F2F), fontFamily = Montserrat, fontSize = 16.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ArticleDetailContent(article: ArticleItem) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(scrollState)
            .background(Color(0xFFF8FBF5))
    ) {

        if (!article.imageUrl.isNullOrEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(article.imageUrl),
                contentDescription = article.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(Color(0xFFE8F5E8), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("Фото отсутствует", color = Color.Gray)
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                article.title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat,
                color = Color(0xFF1B5E20),
                lineHeight = 34.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                article.description,
                fontSize = 16.sp,
                fontFamily = Montserrat,
                color = Color(0xFF444444),
                lineHeight = 24.sp,
                textAlign = TextAlign.Justify
            )

            Spacer(modifier = Modifier.height(16.dp))
                InfoSection("Содержание", listOf(article.content))
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
            Text(
                text = item,
                fontSize = 15.sp,
                color = Color(0xFF555555),
                fontFamily = Montserrat,
                lineHeight = 22.sp,
                textAlign = TextAlign.Justify,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )
        }
    }
}
package com.example.plantscan.Presentation.Screen

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.plantscan.Presentation.Navigation.NavigationRoutes
import com.example.plantscan.Presentation.ViewModels.MainViewModel
import com.example.plantscan.ui.theme.Montserrat
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val history by viewModel.plantHistory
    val photoFromGallery = remember { mutableStateOf<File?>(null) }

    fun Bitmap.scaleBitmap(maxSize: Int): Bitmap {
        val w = this.width
        val h = this.height
        if (w <= maxSize && h <= maxSize) return this
        val scale = maxOf(w, h).toFloat() / maxSize
        return Bitmap.createScaledBitmap(this, (w / scale).toInt(), (h / scale).toInt(), true)
    }

    fun uriToFile(uri: Uri): File? {
        return try {
            val stream = context.contentResolver.openInputStream(uri)
            val bmp = BitmapFactory.decodeStream(stream)
            stream?.close()
            if (bmp == null) return null
            val scaled = bmp.scaleBitmap(1024)
            val file = File(context.cacheDir, "gallery_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { scaled.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            file
        } catch (e: Exception) {
            Log.e("MainScreen", "Ошибка конвертации Uri в File", e)
            null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            viewModel.currentPhotoFile?.let { file ->
                navController.navigate(NavigationRoutes.LoadingAnalysisScreen + "?path=${file.absolutePath}")
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            val file = uriToFile(it)
            if (file != null) {
                photoFromGallery.value = file
                navController.navigate(NavigationRoutes.LoadingAnalysisScreen + "?path=${file.absolutePath}")
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.launchCamera(context, cameraLauncher)
        } else {
            Toast.makeText(context, "Разрешение на камеру отклонено", Toast.LENGTH_SHORT).show()
        }
    }

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F2))
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "PlantScan",
                    fontSize = 20.sp,
                    color = Color(0xFF222222),
                    fontFamily = Montserrat,
                    fontStyle = FontStyle.Italic
                )
                IconButton(onClick = { navController.navigate(NavigationRoutes.ProfileScreen) }) {
                    Icon(Icons.Default.Person, contentDescription = "Профиль")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
                            PermissionChecker.PERMISSION_GRANTED -> viewModel.launchCamera(context, cameraLauncher)
                            else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Сделать фото", color = Color.White, fontFamily = Montserrat)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Из галереи", color = Color.White, fontFamily = Montserrat)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "Последние растения:",
                fontSize = 16.sp,
                color = Color.Black,
                fontFamily = Montserrat,
                modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history.size) { index ->
                    val item = history[index]
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (item.image_url.isNotEmpty()) {
                                Image(
                                    painter = rememberAsyncImagePainter(
                                        ImageRequest.Builder(LocalContext.current)
                                            .data(item.image_url)
                                            .crossfade(true)
                                            .build()
                                    ),
                                    contentDescription = item.name,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = item.name,
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    fontFamily = Montserrat
                                )

                                val dateTime = item.created_at?.let {
                                    try {
                                        val odt = java.time.OffsetDateTime.parse(it)
                                        odt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
                                    } catch (e: Exception) {
                                        it
                                    }
                                } ?: ""

                                Text(
                                    text = dateTime,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadHistory(context)
    }
}

package com.example.app.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.plantscan.Presentation.Navigation.NavigationRoutes
import com.example.plantscan.R
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun SplashScreen(navController: NavHostController) {
    val pulse = rememberInfiniteTransition()
    val scale by pulse.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )

    val rotation by pulse.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        )
    )

    val logoAlpha = remember { Animatable(0f) }

    val particles = remember {
        List(20) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = Random.nextFloat() * 6 + 2,
                alpha = Random.nextFloat() * 0.6f + 0.4f
            )
        }
    }

    LaunchedEffect(Unit) {
        logoAlpha.animateTo(targetValue = 1f, animationSpec = tween(1200))
        delay(1800)

        val isUserLoggedIn = false
        if (isUserLoggedIn) {
            navController.navigate(NavigationRoutes.MainScreen) {
                popUpTo(NavigationRoutes.SplashScreen) { inclusive = true }
            }
        } else {
            navController.navigate(NavigationRoutes.SignInScreen) {
                popUpTo(NavigationRoutes.SplashScreen) { inclusive = true }
            }
        }
    }

    val animatedGradient by pulse.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val gradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFa8e063), Color(0xFF56ab2f), Color(0xFF34a853)),
        startY = animatedGradient,
        endY = animatedGradient + 1000f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { particle ->
                drawCircle(
                    color = Color.White.copy(alpha = particle.alpha),
                    radius = particle.size,
                    center = androidx.compose.ui.geometry.Offset(
                        x = size.width * particle.x,
                        y = size.height * particle.y
                    )
                )
            }
        }

        val logo: Painter = painterResource(id = R.drawable.logo)

        Box(
            modifier = Modifier
                .size(380.dp)
                .scale(scale)
                .rotate(rotation)
                .alpha(logoAlpha.value)
        ) {
            Image(
                painter = logo,
                contentDescription = "Logo",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private data class Particle(
    var x: Float,
    var y: Float,
    val size: Float,
    val alpha: Float
)

private val EaseInOutCubic = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)

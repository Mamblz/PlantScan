package com.example.app.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.example.app.presentation.screens.AnalysisScreen
import com.example.app.presentation.screens.SplashScreen
import com.example.plantscan.Presentation.Navigation.NavigationRoutes
import com.example.plantscan.Presentation.Screen.*
import com.example.plantscan.Presentation.Screens.*
import com.example.plantscan.Presentation.ViewModels.*

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(navController: NavHostController) {
    val analysisViewModel: AnalysisViewModel = viewModel()
    val forgotPasswordViewModel: ForgotPasswordViewModel = viewModel()

    AnimatedNavHost(
        navController = navController,
        startDestination = NavigationRoutes.SplashScreen
    ) {
        composable(
            route = NavigationRoutes.SplashScreen,
            enterTransition = { fadeIn(animationSpec = tween(800)) },
            exitTransition = { fadeOut(animationSpec = tween(800)) }
        ) { SplashScreen(navController) }

        composable(
            route = NavigationRoutes.SignInScreen,
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
        ) { SignInScreen(navController) }

        composable(
            route = NavigationRoutes.SignUpScreen,
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
        ) {
            val viewModel: SignUpViewModel = viewModel()
            SignUpScreen(navController, viewModel)
        }

        composable(
            route = NavigationRoutes.ForgotPasswordScreen,
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) + fadeOut() }
        ) { ForgotPasswordScreen(navController, forgotPasswordViewModel) }

        composable(
            route = NavigationRoutes.MainScreen,
            enterTransition = { scaleIn(initialScale = 0.8f, animationSpec = tween(500)) + fadeIn() },
            exitTransition = { scaleOut(targetScale = 0.8f, animationSpec = tween(500)) + fadeOut() }
        ) {
            val mainViewModel: MainViewModel = viewModel()
            MainScreen(navController, mainViewModel)
        }

        composable(
            route = NavigationRoutes.HistoryScreen,
            enterTransition = { slideInHorizontally(initialOffsetX = { 800 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -800 }) + fadeOut() }
        ) {
            val viewModel: HistoryViewModel = viewModel()
            HistoryScreen(navController, viewModel)
        }

        composable(
            route = NavigationRoutes.FavoritesScreen,
            enterTransition = { slideInHorizontally(initialOffsetX = { 800 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -800 }) + fadeOut() }
        ) { FavoritesScreen(navController) }

        composable(
            route = NavigationRoutes.ProfileScreen,
            enterTransition = { slideInHorizontally(initialOffsetX = { 800 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -800 }) + fadeOut() }
        ) { ProfileScreen(navController) }

        composable(
            route = NavigationRoutes.PersonalDataScreen,
            enterTransition = { slideInHorizontally(initialOffsetX = { 800 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -800 }) + fadeOut() }
        ) { PersonalDataScreen(navController) }

        composable(
            route = "${NavigationRoutes.FavoriteDetail}/{favoriteId}",
            arguments = listOf(navArgument("favoriteId") {
                type = NavType.StringType
            }),
            enterTransition = { scaleIn(initialScale = 0.8f) + fadeIn() },
            exitTransition = { scaleOut(targetScale = 0.8f) + fadeOut() }
        ) { backStackEntry ->
            val favoriteId = backStackEntry.arguments?.getString("favoriteId") ?: ""
            PlantDetailScreen(navController, favoriteId)
        }

        composable(
            route = "${NavigationRoutes.LoadingAnalysisScreen}?path={path}",
            arguments = listOf(navArgument("path") { type = NavType.StringType; defaultValue = "" }),
            enterTransition = { slideInVertically(initialOffsetY = { 1000 }) + fadeIn() },
            exitTransition = { slideOutVertically(targetOffsetY = { -1000 }) + fadeOut() }
        ) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("path") ?: ""
            LoadingAnalysisScreen(navController, analysisViewModel, path)
        }

        composable(
            route = "${NavigationRoutes.ArticleDetail}/{articleId}",
            arguments = listOf(navArgument("articleId") { type = NavType.IntType }),
            enterTransition = { scaleIn(initialScale = 0.8f) + fadeIn() },
            exitTransition = { scaleOut(targetScale = 0.8f) + fadeOut() }
        ) { backStackEntry ->
            val articleId = backStackEntry.arguments?.getInt("articleId") ?: -1
            ArticleDetailScreen(navController, articleId)
        }

        composable(
            route = NavigationRoutes.ArticlesScreen,
            enterTransition = { slideInHorizontally(initialOffsetX = { 800 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -800 }) + fadeOut() }
        ) {
            val viewModel: ArticleViewModel = viewModel()
            ArticlesScreen(navController, viewModel)
        }

        composable(
            route = NavigationRoutes.ChangePassword,
            enterTransition = { slideInVertically(initialOffsetY = { 1000 }) + fadeIn() },
            exitTransition = { slideOutVertically(targetOffsetY = { -1000 }) + fadeOut() }
        ) { ChangePasswordScreen(navController) }

        composable(
            route = NavigationRoutes.AnalysisScreen,
            enterTransition = { slideInVertically(initialOffsetY = { 1000 }) + fadeIn() },
            exitTransition = { slideOutVertically(targetOffsetY = { -1000 }) + fadeOut() }
        ) { AnalysisScreen(navController, analysisViewModel) }
    }
}

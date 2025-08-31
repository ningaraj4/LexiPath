package com.example.lexipath.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lexipath.ui.auth.AuthState
import com.example.lexipath.ui.auth.AuthViewModel
import com.example.lexipath.ui.auth.LoginScreen
import com.example.lexipath.ui.home.HomeScreen
import com.example.lexipath.ui.onboarding.OnboardingScreen
import com.example.lexipath.ui.quiz.QuizScreen
import com.example.lexipath.ui.history.HistoryScreen
import com.example.lexipath.ui.settings.SettingsScreen
import com.example.lexipath.ui.weeklyreview.WeeklyReviewScreen

@Composable
fun LexiPathNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = when (authState) {
            is AuthState.Authenticated -> Screen.Home.route
            is AuthState.Unauthenticated -> Screen.Login.route
            is AuthState.Loading -> Screen.Login.route
            is AuthState.Error -> Screen.Login.route
        }
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Onboarding.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onOnboardingComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToQuiz = { content ->
                    navController.navigate("${Screen.Quiz.route}/${content.id}")
                },
                onNavigateToHistory = {
                    navController.navigate(Screen.History.route)
                },
                onNavigateToWeeklyReview = {
                    navController.navigate(Screen.WeeklyReview.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable("${Screen.Quiz.route}/{contentId}") { backStackEntry ->
            val contentId = backStackEntry.arguments?.getString("contentId") ?: ""
            QuizScreen(
                contentId = contentId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.WeeklyReview.route) {
            WeeklyReviewScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSignOut = {
                    authViewModel.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Quiz : Screen("quiz")
    object History : Screen("history")
    object WeeklyReview : Screen("weekly_review")
    object Settings : Screen("settings")
}

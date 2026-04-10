package com.burnfat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.burnfat.ui.screens.*

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object FoodLog : Screen("food_log")
    object ExerciseLog : Screen("exercise_log")
    object PhotoCapture : Screen("photo_capture/{mealType}") {
        fun createRoute(mealType: String) = "photo_capture/$mealType"
    }
    object History : Screen("history")
    object WeightCurve : Screen("weight_curve")
    object AchievementWall : Screen("achievement_wall")
    object Settings : Screen("settings")
    object PlanManagement : Screen("plan_management")
}

@Composable
fun NavigationGraph(
    navController: NavHostController = rememberNavController()
) {
    // 检查是否已完成引导
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val onboardingState by onboardingViewModel.state.collectAsStateWithLifecycle()

    // 加载中显示空白页面
    if (onboardingState.isLoading) {
        // 返回空的Scaffold作为加载页面
        androidx.compose.material3.Scaffold { }
        return
    }

    // 根据是否已完成引导决定起始页面
    val startDestination = if (onboardingState.isCompleted) {
        Screen.Dashboard.route
    } else {
        Screen.Onboarding.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToFoodLog = { navController.navigate(Screen.FoodLog.route) },
                onNavigateToExerciseLog = { navController.navigate(Screen.ExerciseLog.route) },
                onNavigateToPhotoCapture = { mealType ->
                    // 根据时间自动匹配餐次，不再需要传递参数
                    navController.navigate(Screen.PhotoCapture.createRoute(mealType.name))
                },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToWeightCurve = { navController.navigate(Screen.WeightCurve.route) },
                onNavigateToAchievementWall = { navController.navigate(Screen.AchievementWall.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.FoodLog.route) {
            FoodLogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ExerciseLog.route) {
            ExerciseLogScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.PhotoCapture.route) { backStackEntry ->
            val mealTypeName = backStackEntry.arguments?.getString("mealType") ?: "SNACK"
            PhotoCaptureScreen(
                mealTypeName = mealTypeName,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.WeightCurve.route) {
            WeightCurveScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlanEdit = { planId ->
                    navController.navigate(Screen.PlanManagement.route)
                }
            )
        }

        composable(Screen.AchievementWall.route) {
            AchievementWallScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPlanManagement = { navController.navigate(Screen.PlanManagement.route) }
            )
        }

        composable(Screen.PlanManagement.route) {
            PlanManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
package com.karan.antiproductivity.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.karan.antiproductivity.ui.MainViewModel
import com.karan.antiproductivity.ui.screens.*

@Composable
fun NavGraph(viewModel: MainViewModel) {
    val navController = rememberNavController()

    val onboardingCompleted by viewModel.onboardingCompleted.collectAsState()
    val installedApps by viewModel.installedApps.collectAsState()
    val blockedApps by viewModel.blockedApps.collectAsState()
    val unlockSessions by viewModel.unlockSessions.collectAsState()
    val blockingEnabled by viewModel.blockingEnabled.collectAsState()
    val challengeEnabled by viewModel.challengeEnabled.collectAsState()
    val unlockDurationMinutes by viewModel.unlockDurationMinutes.collectAsState()
    val stats by viewModel.stats.collectAsState()

    val startDestination = if (onboardingCompleted) Screen.Home.route else Screen.Onboarding.route

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onNavigateToBlockedApps = { navController.navigate(Screen.BlockedApps.route) },
                onCompleteOnboarding = {
                    viewModel.completeOnboarding()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                installedApps = installedApps,
                blockedApps = blockedApps,
                stats = stats,
                onNavigateToBlockedApps = { navController.navigate(Screen.BlockedApps.route) },
                onNavigateToActiveUnlocks = { navController.navigate(Screen.ActiveUnlocks.route) },
                onNavigateToStatistics = { navController.navigate(Screen.Statistics.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToAllApps = { navController.navigate(Screen.AllApps.route) }
            )
        }

        composable(Screen.AllApps.route) {
            AllAppsScreen(
                installedApps = installedApps,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.BlockedApps.route) {
            BlockedAppsScreen(
                installedApps = installedApps,
                blockedApps = blockedApps,
                onToggleBlocked = { pkg, blocked -> viewModel.toggleAppBlocked(pkg, blocked) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Statistics.route) {
            StatisticsScreen(
                stats = stats,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.ActiveUnlocks.route) {
            ActiveUnlocksScreen(
                unlockSessions = unlockSessions,
                onRevokeUnlock = { pkg -> viewModel.revokeUnlock(pkg) },
                onClearAll = { viewModel.clearAllUnlocks() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                blockingEnabled = blockingEnabled,
                challengeEnabled = challengeEnabled,
                unlockDurationMinutes = unlockDurationMinutes,
                onToggleBlocking = { viewModel.setBlockingEnabled(it) },
                onToggleChallenge = { viewModel.setChallengeEnabled(it) },
                onUpdateUnlockDuration = { viewModel.setUnlockDuration(it) },
                onBack = { navController.popBackStack() }
            )
        }
    }
}

package com.karan.antiproductivity.ui.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object AllApps : Screen("all_apps")
    data object BlockedApps : Screen("blocked_apps")
    data object Statistics : Screen("statistics")
    data object ActiveUnlocks : Screen("active_unlocks")
    data object Settings : Screen("settings")
}

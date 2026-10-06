package com.karan.antiproductivity.data.model

data class AppItem(
    val packageName: String,
    val appName: String,
    val isSystemApp: Boolean = false
)

data class UnlockSession(
    val packageName: String,
    val startedAt: Long,
    val expiresAt: Long
)

data class DailyStats(
    val date: String,
    val blockedAttempts: Int = 0,
    val successfulChallenges: Int = 0,
    val failedChallenges: Int = 0,
    val minutesUnlocked: Int = 0
)

package com.karan.antiproductivity.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "anti_productivity_prefs")

class SettingsDataStore(private val context: Context) {

    companion object {
        val BLOCKED_APPS_KEY = stringSetPreferencesKey("blocked_apps")
        val UNLOCK_SESSIONS_KEY = stringPreferencesKey("unlock_sessions") // Format: pkg1:expiry1,pkg2:expiry2
        val BLOCKING_ENABLED_KEY = booleanPreferencesKey("blocking_enabled")
        val CHALLENGE_ENABLED_KEY = booleanPreferencesKey("challenge_enabled")
        val UNLOCK_DURATION_MINUTES_KEY = intPreferencesKey("unlock_duration_minutes")
        val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")

        // Stats keys
        val STATS_BLOCKED_ATTEMPTS = intPreferencesKey("stats_blocked_attempts")
        val STATS_SUCCESSFUL_CHALLENGES = intPreferencesKey("stats_successful_challenges")
        val STATS_FAILED_CHALLENGES = intPreferencesKey("stats_failed_challenges")
        val STATS_MINUTES_UNLOCKED = intPreferencesKey("stats_minutes_unlocked")
        val STATS_STREAK = intPreferencesKey("stats_streak")
    }

    val blockedAppsFlow: Flow<Set<String>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[BLOCKED_APPS_KEY] ?: emptySet() }

    val unlockSessionsFlow: Flow<Map<String, Long>> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            val raw = prefs[UNLOCK_SESSIONS_KEY] ?: ""
            parseUnlockSessions(raw)
        }

    val blockingEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[BLOCKING_ENABLED_KEY] ?: true }

    val challengeEnabledFlow: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[CHALLENGE_ENABLED_KEY] ?: true }

    val unlockDurationMinutesFlow: Flow<Int> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[UNLOCK_DURATION_MINUTES_KEY] ?: 15 }

    val onboardingCompletedFlow: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs -> prefs[ONBOARDING_COMPLETED_KEY] ?: false }

    val statsFlow: Flow<StatsData> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { prefs ->
            StatsData(
                blockedAttempts = prefs[STATS_BLOCKED_ATTEMPTS] ?: 0,
                successfulChallenges = prefs[STATS_SUCCESSFUL_CHALLENGES] ?: 0,
                failedChallenges = prefs[STATS_FAILED_CHALLENGES] ?: 0,
                minutesUnlocked = prefs[STATS_MINUTES_UNLOCKED] ?: 0,
                streak = prefs[STATS_STREAK] ?: 1
            )
        }

    suspend fun setBlockedApps(apps: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[BLOCKED_APPS_KEY] = apps
        }
    }

    suspend fun toggleAppBlocked(packageName: String, blocked: Boolean) {
        context.dataStore.edit { prefs ->
            val current = prefs[BLOCKED_APPS_KEY]?.toMutableSet() ?: mutableSetOf()
            if (blocked) current.add(packageName) else current.remove(packageName)
            prefs[BLOCKED_APPS_KEY] = current
        }
    }

    suspend fun addUnlockSession(packageName: String, expiryTime: Long) {
        context.dataStore.edit { prefs ->
            val raw = prefs[UNLOCK_SESSIONS_KEY] ?: ""
            val map = parseUnlockSessions(raw).toMutableMap()
            map[packageName] = expiryTime
            // Clean expired
            val now = System.currentTimeMillis()
            map.entries.removeIf { it.value <= now }
            prefs[UNLOCK_SESSIONS_KEY] = serializeUnlockSessions(map)
        }
    }

    suspend fun removeUnlockSession(packageName: String) {
        context.dataStore.edit { prefs ->
            val raw = prefs[UNLOCK_SESSIONS_KEY] ?: ""
            val map = parseUnlockSessions(raw).toMutableMap()
            map.remove(packageName)
            prefs[UNLOCK_SESSIONS_KEY] = serializeUnlockSessions(map)
        }
    }

    suspend fun clearAllUnlocks() {
        context.dataStore.edit { prefs ->
            prefs[UNLOCK_SESSIONS_KEY] = ""
        }
    }

    suspend fun setBlockingEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[BLOCKING_ENABLED_KEY] = enabled }
    }

    suspend fun setChallengeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[CHALLENGE_ENABLED_KEY] = enabled }
    }

    suspend fun setUnlockDurationMinutes(minutes: Int) {
        context.dataStore.edit { prefs -> prefs[UNLOCK_DURATION_MINUTES_KEY] = minutes }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs -> prefs[ONBOARDING_COMPLETED_KEY] = completed }
    }

    suspend fun incrementBlockedAttempt() {
        context.dataStore.edit { prefs ->
            val current = prefs[STATS_BLOCKED_ATTEMPTS] ?: 0
            prefs[STATS_BLOCKED_ATTEMPTS] = current + 1
        }
    }

    suspend fun incrementSuccessfulChallenge(minutesEarned: Int) {
        context.dataStore.edit { prefs ->
            val successes = prefs[STATS_SUCCESSFUL_CHALLENGES] ?: 0
            val minutes = prefs[STATS_MINUTES_UNLOCKED] ?: 0
            prefs[STATS_SUCCESSFUL_CHALLENGES] = successes + 1
            prefs[STATS_MINUTES_UNLOCKED] = minutes + minutesEarned
        }
    }

    suspend fun incrementFailedChallenge() {
        context.dataStore.edit { prefs ->
            val current = prefs[STATS_FAILED_CHALLENGES] ?: 0
            prefs[STATS_FAILED_CHALLENGES] = current + 1
        }
    }

    private fun parseUnlockSessions(raw: String): Map<String, Long> {
        if (raw.isBlank()) return emptyMap()
        val map = mutableMapOf<String, Long>()
        val now = System.currentTimeMillis()
        raw.split(",").forEach { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val pkg = parts[0]
                val expiry = parts[1].toLongOrNull() ?: 0L
                if (expiry > now) {
                    map[pkg] = expiry
                }
            }
        }
        return map
    }

    private fun serializeUnlockSessions(map: Map<String, Long>): String {
        return map.entries.joinToString(",") { "${it.key}:${it.value}" }
    }
}

data class StatsData(
    val blockedAttempts: Int,
    val successfulChallenges: Int,
    val failedChallenges: Int,
    val minutesUnlocked: Int,
    val streak: Int
)

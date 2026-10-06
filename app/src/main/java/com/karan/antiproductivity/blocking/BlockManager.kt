package com.karan.antiproductivity.blocking

import android.content.Context
import com.karan.antiproductivity.data.datastore.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

object BlockManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var blockedApps: Set<String> = emptySet()
    private var unlockSessions: Map<String, Long> = emptyMap()
    private var blockingEnabled: Boolean = true

    fun init(context: Context) {
        val dataStore = SettingsDataStore(context.applicationContext)
        scope.launch {
            dataStore.blockedAppsFlow.collectLatest { apps ->
                blockedApps = apps
            }
        }
        scope.launch {
            dataStore.unlockSessionsFlow.collectLatest { sessions ->
                unlockSessions = sessions
            }
        }
        scope.launch {
            dataStore.blockingEnabledFlow.collectLatest { enabled ->
                blockingEnabled = enabled
            }
        }
    }

    fun isBlockedAndLocked(packageName: String): Boolean {
        if (!blockingEnabled) return false
        if (!blockedApps.contains(packageName)) return false

        val expiry = unlockSessions[packageName] ?: 0L
        val now = System.currentTimeMillis()
        // If expired or not present, it's locked
        return expiry <= now
    }
}

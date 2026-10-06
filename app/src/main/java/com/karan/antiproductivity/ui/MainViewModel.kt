package com.karan.antiproductivity.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.karan.antiproductivity.data.datastore.SettingsDataStore
import com.karan.antiproductivity.data.datastore.StatsData
import com.karan.antiproductivity.data.repository.AppRepository
import com.karan.antiproductivity.data.repository.LaunchableApp
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = SettingsDataStore(application)
    private val appRepository = AppRepository(application)

    val onboardingCompleted: StateFlow<Boolean> = dataStore.onboardingCompletedFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val blockedApps: StateFlow<Set<String>> = dataStore.blockedAppsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val unlockSessions: StateFlow<Map<String, Long>> = dataStore.unlockSessionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val blockingEnabled: StateFlow<Boolean> = dataStore.blockingEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val challengeEnabled: StateFlow<Boolean> = dataStore.challengeEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val unlockDurationMinutes: StateFlow<Int> = dataStore.unlockDurationMinutesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 15)

    val stats: StateFlow<StatsData> = dataStore.statsFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            StatsData(0, 0, 0, 0, 1)
        )

    private val _installedApps = MutableStateFlow<List<LaunchableApp>>(emptyList())
    val installedApps: StateFlow<List<LaunchableApp>> = _installedApps.asStateFlow()

    init {
        loadInstalledApps()
    }

    fun loadInstalledApps() {
        viewModelScope.launch {
            _installedApps.value = appRepository.getInstalledApps()
        }
    }

    fun toggleAppBlocked(packageName: String, blocked: Boolean) {
        viewModelScope.launch {
            dataStore.toggleAppBlocked(packageName, blocked)
        }
    }

    fun revokeUnlock(packageName: String) {
        viewModelScope.launch {
            dataStore.removeUnlockSession(packageName)
        }
    }

    fun clearAllUnlocks() {
        viewModelScope.launch {
            dataStore.clearAllUnlocks()
        }
    }

    fun setBlockingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setBlockingEnabled(enabled)
        }
    }

    fun setChallengeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setChallengeEnabled(enabled)
        }
    }

    fun setUnlockDuration(minutes: Int) {
        viewModelScope.launch {
            dataStore.setUnlockDurationMinutes(minutes)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            dataStore.setOnboardingCompleted(true)
        }
    }
}

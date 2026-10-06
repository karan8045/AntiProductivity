package com.karan.antiproductivity.challenge

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.karan.antiproductivity.data.datastore.SettingsDataStore
import com.karan.antiproductivity.roast.RoastProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ChallengeState {
    data class Active(
        val targetString: String,
        val userInput: String = "",
        val errorMessage: String? = null
    ) : ChallengeState()

    data class Success(
        val packageName: String,
        val minutesUnlocked: Int
    ) : ChallengeState()

    data class Failure(
        val roastMessage: String
    ) : ChallengeState()
}

class ChallengeViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = SettingsDataStore(application)

    private val _state = MutableStateFlow<ChallengeState>(
        ChallengeState.Active(targetString = RandomStringChallenge.generateTargetString())
    )
    val state: StateFlow<ChallengeState> = _state.asStateFlow()

    fun updateUserInput(input: String) {
        val current = _state.value
        if (current is ChallengeState.Active) {
            _state.value = current.copy(userInput = input, errorMessage = null)
        }
    }

    fun regenerateChallenge() {
        _state.value = ChallengeState.Active(
            targetString = RandomStringChallenge.generateTargetString()
        )
    }

    fun submitChallenge(targetPackage: String) {
        val current = _state.value
        if (current is ChallengeState.Active) {
            if (RandomStringChallenge.validate(current.userInput.trim(), current.targetString)) {
                // Success!
                viewModelScope.launch {
                    val duration = 15 // 15 mins default
                    val expiry = System.currentTimeMillis() + (duration * 60 * 1000L)
                    dataStore.addUnlockSession(targetPackage, expiry)
                    dataStore.incrementSuccessfulChallenge(duration)
                    _state.value = ChallengeState.Success(targetPackage, duration)
                }
            } else {
                // Failure
                viewModelScope.launch {
                    dataStore.incrementFailedChallenge()
                    val roast = RoastProvider.getRandomRoast()
                    _state.value = ChallengeState.Failure(roast)
                }
            }
        }
    }

    fun retry() {
        regenerateChallenge()
    }
}

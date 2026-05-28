package com.example.primera.feature.splash.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primera.core.data.repository.PreferenceRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SplashViewModel(
    private val preferenceRepository: PreferenceRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Animating)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _effect = Channel<SplashEffect>(Channel.BUFFERED)
    val effect: Flow<SplashEffect> = _effect.receiveAsFlow()

    init {
        startSplashTimer()
    }

    private fun startSplashTimer() {
        viewModelScope.launch {
            delay(2500)  // 2.5 second splash duration
            _uiState.update { SplashUiState.Complete }
            
            if (preferenceRepository.shouldShowOnboarding()) {
                _effect.send(SplashEffect.Navigate)
            } else {
                _effect.send(SplashEffect.NavigateToAuth)
            }
        }
    }
}

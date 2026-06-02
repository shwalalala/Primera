package com.example.primera.feature.welcome.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.primera.core.data.PreferenceRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WelcomeViewModel(
) : ViewModel() {
    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState: StateFlow<WelcomeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<WelcomeEffect>(Channel.BUFFERED)
    val effect: Flow<WelcomeEffect> = _effect.receiveAsFlow()

    fun nextPage() {
        val currentState = _uiState.value
        if (currentState.currentPage < currentState.totalPages - 1) {
            _uiState.update { it.copy(currentPage = currentState.currentPage + 1) }
        }
    }

    fun previousPage() {
        val currentState = _uiState.value
        if (currentState.currentPage > 0) {
            _uiState.update { it.copy(currentPage = currentState.currentPage - 1) }
        }
    }

    fun onGetStarted() {
        viewModelScope.launch {
            // This is the initial walkthrough, we can use a "guest" or "walkthrough" ID 
            // to track if the app has shown the walkthrough once, or just skip per-user tracking here.
            // For now, let's just navigate to Auth as the user usually creates an account after this.
            _effect.send(WelcomeEffect.NavigateToAuth)
        }
    }

    fun onSkip() {
        onGetStarted()
    }
}

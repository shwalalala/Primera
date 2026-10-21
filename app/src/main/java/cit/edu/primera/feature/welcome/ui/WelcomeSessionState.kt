package cit.edu.primera.feature.welcome.ui

import java.util.concurrent.atomic.AtomicBoolean

object WelcomeSessionState {
    private val hasBeenShown = AtomicBoolean(false)

    fun wasShown(): Boolean = hasBeenShown.get()

    fun markShown() {
        hasBeenShown.set(true)
    }
}

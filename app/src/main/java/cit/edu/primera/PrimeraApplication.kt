package cit.edu.primera

import android.app.Application
import cit.edu.primera.core.di.AppContainer
import cit.edu.primera.core.di.AppContainerImpl
import com.facebook.appevents.AppEventsLogger

class PrimeraApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
        
        // Initialize Facebook SDK for App Events (auto-initialization is preferred in modern SDK, 
        // but explicit call handles edge cases)
        AppEventsLogger.activateApp(this)
    }
}

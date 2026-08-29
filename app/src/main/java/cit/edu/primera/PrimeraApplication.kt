package cit.edu.primera

import android.app.Application
import cit.edu.primera.core.di.AppContainer
import cit.edu.primera.core.di.AppContainerImpl

class PrimeraApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
    }
}

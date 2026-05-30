package com.example.primera

import android.app.Application
import com.example.primera.core.di.AppContainer
import com.example.primera.core.di.AppContainerImpl

class PrimeraApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
    }
}

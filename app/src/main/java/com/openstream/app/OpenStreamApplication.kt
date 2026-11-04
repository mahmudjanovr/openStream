package com.openstream.app

import android.app.Application
import com.openstream.app.core.di.AppContainer
import com.openstream.app.core.di.DefaultAppContainer

class OpenStreamApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(applicationContext)
    }
}

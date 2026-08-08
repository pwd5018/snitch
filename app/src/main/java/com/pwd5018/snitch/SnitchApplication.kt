package com.pwd5018.snitch

import android.app.Application
import com.pwd5018.snitch.di.AppContainer

class SnitchApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

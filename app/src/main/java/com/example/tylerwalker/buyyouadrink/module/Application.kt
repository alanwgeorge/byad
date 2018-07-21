package com.example.tylerwalker.buyyouadrink.module

import android.app.Application

class App: Application() {
    private lateinit var applicationComponent: ApplicationComponent

    fun getComponent(): ApplicationComponent {
        if (!(::applicationComponent.isInitialized))
            applicationComponent = DaggerApplicationComponent.create()

        return applicationComponent
    }

}
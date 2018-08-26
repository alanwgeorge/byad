package com.example.tylerwalker.buyyouadrink.module

import android.app.Application
import android.content.Context

class App: Application() {
    private lateinit var applicationComponent: ApplicationComponent

    fun getComponent(context: Context): ApplicationComponent {
        if (!(::applicationComponent.isInitialized))
            applicationComponent =
        DaggerApplicationComponent.builder().applicationModule(ApplicationModule(context)).build()

        return applicationComponent
    }
}
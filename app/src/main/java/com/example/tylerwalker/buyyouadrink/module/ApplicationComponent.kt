package com.example.tylerwalker.buyyouadrink.module

import com.example.tylerwalker.buyyouadrink.activity.home.HomeScreen
import com.example.tylerwalker.buyyouadrink.activity.login.LoginActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(loginActivity: LoginActivity)
    fun inject(homeActivity: HomeScreen)
}
package com.example.tylerwalker.buyyouadrink.module

import com.example.tylerwalker.buyyouadrink.activity.home.HomeScreen
import com.example.tylerwalker.buyyouadrink.activity.login.LoginActivity
import com.example.tylerwalker.buyyouadrink.activity.login.SignUpActivity
import com.example.tylerwalker.buyyouadrink.activity.profile.ProfileActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(loginActivity: LoginActivity)
    fun inject(signUpActivity: SignUpActivity)
    fun inject(homeActivity: HomeScreen)
    fun inject(homeActivity: ProfileActivity)
}
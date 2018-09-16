package com.example.tylerwalker.buyyouadrink.module

import android.content.Context
import com.example.tylerwalker.buyyouadrink.model.LocalStorage
import com.example.tylerwalker.buyyouadrink.model.UserRepository
import com.example.tylerwalker.buyyouadrink.service.AuthService
import com.example.tylerwalker.buyyouadrink.service.LocationService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule(val context: Context) {
    @Provides
    @Singleton
    fun provideAuthService(): AuthService {
        return AuthService()
    }

    @Provides
    @Singleton
    fun provideLocationService(): LocationService {
        return LocationService()
    }

    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository {
        return UserRepository()
    }

    @Provides
    fun provideContext(): App = App()

    @Provides
    @Singleton
    fun provideLocalStorage(): LocalStorage {
        return LocalStorage(context)
    }
}
package com.example.tylerwalker.buyyouadrink.module

import com.example.tylerwalker.buyyouadrink.service.AuthService
import com.example.tylerwalker.buyyouadrink.service.LocationService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApplicationModule {
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
}
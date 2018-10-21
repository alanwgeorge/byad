package com.example.tylerwalker.buyyouadrink.module

import android.content.Context
import com.example.tylerwalker.buyyouadrink.model.*
import com.example.tylerwalker.buyyouadrink.service.AuthService
import com.example.tylerwalker.buyyouadrink.service.LocationService
import dagger.Module
import dagger.Provides
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import javax.inject.Named
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

    @Provides
    @Singleton
    fun provideCurrentUser(localStorage: LocalStorage): User = localStorage.getCurrentUser() ?: User()

    @Provides
    @Singleton
    fun provideAuthEventsProcessor(): PublishProcessor<AuthEvent> = PublishProcessor.create()

    @Provides
    @Singleton
    fun provideAuthEventsFlowable(processor: PublishProcessor<AuthEvent>): Flowable<AuthEvent> = processor

    @Provides
    @Singleton
    fun provideNavigationEventsProcessor(): PublishProcessor<NavigationEvent> = PublishProcessor.create()

    @Provides
    @Singleton
    fun provideNavigationEventsFlowable(processor: PublishProcessor<NavigationEvent>): Flowable<NavigationEvent> = processor

    @Provides
    @Singleton
    fun provideProfileEventsProcessor(): PublishProcessor<ProfileEvent> = PublishProcessor.create()

    @Provides
    @Singleton
    fun provideProfileEventsFlowable(processor: PublishProcessor<ProfileEvent>): Flowable<ProfileEvent> = processor
}
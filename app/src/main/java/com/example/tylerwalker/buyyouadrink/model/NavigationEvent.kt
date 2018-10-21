package com.example.tylerwalker.buyyouadrink.model

sealed class NavigationEvent {
    object OnBoarding: NavigationEvent()
    object Home: NavigationEvent()
    object Settings: NavigationEvent()
    object Messages: NavigationEvent()
    object Profile: NavigationEvent()
}
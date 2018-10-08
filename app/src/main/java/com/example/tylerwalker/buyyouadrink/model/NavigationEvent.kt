package com.example.tylerwalker.buyyouadrink.model

sealed class NavigationEvent {
    object OnBoarding: NavigationEvent()
    object Home: NavigationEvent()
}
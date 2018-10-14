package com.example.tylerwalker.buyyouadrink.model

import android.view.View

sealed class ProfileEvent {
    object SaveProfile: ProfileEvent()
    object ChooseProfileImage: ProfileEvent()
    object ChooseCoverImage: ProfileEvent()
    class ToggleDrink(val drink: Drink): ProfileEvent()
}
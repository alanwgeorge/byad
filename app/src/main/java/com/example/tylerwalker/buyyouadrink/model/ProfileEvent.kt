package com.example.tylerwalker.buyyouadrink.model

import android.view.View

sealed class ProfileEvent {
    object SaveProfile: ProfileEvent()
    object ChooseProfileImage: ProfileEvent()
    object ChooseCoverImage: ProfileEvent()
    class ToggleDrink(val drink: Drink): ProfileEvent()
    object DismissFavoriteDrinkDialog: ProfileEvent()
    object ShowFavoriteDrinkDialog: ProfileEvent()
    class BuyUserADrink(val user: User): ProfileEvent()
    class UserError(val message: String?): ProfileEvent()
    class GoToConversation(val conversation: Conversation): ProfileEvent()
}
package com.example.tylerwalker.buyyouadrink.module

import com.example.tylerwalker.buyyouadrink.activity.home.HomeScreen
import com.example.tylerwalker.buyyouadrink.activity.home.HomeViewModel
import com.example.tylerwalker.buyyouadrink.activity.login.LoginActivity
import com.example.tylerwalker.buyyouadrink.activity.login.LoginViewModel
import com.example.tylerwalker.buyyouadrink.activity.login.SignUpActivity
import com.example.tylerwalker.buyyouadrink.activity.login.SignUpViewModel
import com.example.tylerwalker.buyyouadrink.activity.map.InvitationFragment
import com.example.tylerwalker.buyyouadrink.activity.map.InvitationViewModel
import com.example.tylerwalker.buyyouadrink.activity.map.MapActivity
import com.example.tylerwalker.buyyouadrink.activity.messages.BlockUserDialog
import com.example.tylerwalker.buyyouadrink.activity.messages.ConversationActivity
import com.example.tylerwalker.buyyouadrink.activity.messages.MessagesActivity
import com.example.tylerwalker.buyyouadrink.activity.profile.ProfileActivity
import com.example.tylerwalker.buyyouadrink.activity.profile.ProfileViewModel
import com.example.tylerwalker.buyyouadrink.activity.profile.SetupProfileActivity
import com.example.tylerwalker.buyyouadrink.activity.profile.SetupProfileViewModel
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ApplicationModule::class])
interface ApplicationComponent {
    fun inject(loginActivity: LoginActivity)
    fun inject(loginViewModel: LoginViewModel)
    fun inject(signUpActivity: SignUpActivity)
    fun inject(signUpViewModel: SignUpViewModel)
    fun inject(homeActivity: HomeScreen)
    fun inject(homeViewModel: HomeViewModel)
    fun inject(profileActivity: ProfileActivity)
    fun inject(profileViewModel: ProfileViewModel)
    fun inject(setupProfileActivity: SetupProfileActivity)
    fun inject(setupProfileViewModel: SetupProfileViewModel)
    fun inject(mapActivity: MapActivity)
    fun inject(invitationFragment: InvitationFragment)
    fun inject(invitationViewModel: InvitationViewModel)
    fun inject(messagesActivity: MessagesActivity)
    fun inject(conversationActivity: ConversationActivity)
    fun inject(blockUserDialog: BlockUserDialog)
}
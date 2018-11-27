package io.tylerwalker.buyyouadrink.module

import dagger.Component
import io.tylerwalker.buyyouadrink.activity.home.HomeScreen
import io.tylerwalker.buyyouadrink.activity.home.HomeViewModel
import io.tylerwalker.buyyouadrink.activity.login.LoginActivity
import io.tylerwalker.buyyouadrink.activity.login.LoginViewModel
import io.tylerwalker.buyyouadrink.activity.login.SignUpActivity
import io.tylerwalker.buyyouadrink.activity.login.SignUpViewModel
import io.tylerwalker.buyyouadrink.activity.map.InvitationFragment
import io.tylerwalker.buyyouadrink.activity.map.InvitationViewModel
import io.tylerwalker.buyyouadrink.activity.map.MapActivity
import io.tylerwalker.buyyouadrink.activity.messages.BlockUserDialog
import io.tylerwalker.buyyouadrink.activity.messages.ConversationActivity
import io.tylerwalker.buyyouadrink.activity.messages.MessagesActivity
import io.tylerwalker.buyyouadrink.activity.profile.ProfileActivity
import io.tylerwalker.buyyouadrink.activity.profile.ProfileViewModel
import io.tylerwalker.buyyouadrink.activity.profile.SetupProfileActivity
import io.tylerwalker.buyyouadrink.activity.profile.SetupProfileViewModel
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
package io.tylerwalker.buyyouadrink.model

sealed class AuthEvent(val uid: String? = null, val credentials: Credentials? = null, val registerInformation: RegisterInformation? = null) {

    class SignOn(credentials: Credentials): AuthEvent(credentials = credentials)
    class SignOnSuccess(uid: String): AuthEvent(uid = uid)
    object Register: AuthEvent()
    object RegisterSuccess: AuthEvent()
    object FirstTimeUserSetup: AuthEvent()
}
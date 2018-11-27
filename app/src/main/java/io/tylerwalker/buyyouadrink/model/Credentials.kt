package io.tylerwalker.buyyouadrink.model

data class Credentials(val email: String, val password: String)
data class RegisterInformation(val email: String, val password: String, val name: String)
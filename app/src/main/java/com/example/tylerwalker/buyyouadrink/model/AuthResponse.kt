package com.example.tylerwalker.buyyouadrink.model

data class AuthResponse (
        val uid: String?,
        val status: Boolean = false,
        val error: Throwable? = null
)
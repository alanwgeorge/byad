package com.example.tylerwalker.buyyouadrink.service

import com.example.tylerwalker.buyyouadrink.model.AuthResponse
import com.example.tylerwalker.buyyouadrink.model.Coordinates
import com.example.tylerwalker.buyyouadrink.model.User
import io.reactivex.Single
import java.util.concurrent.TimeUnit

class AuthService {
    fun login(username: String, password: String): Single<AuthResponse> {
        val loc = Coordinates(0F, 0F)
        val user = User(0,"John", "Doe", loc)
        return Single.timer(1L, TimeUnit.SECONDS).flatMap {
            Single.just(AuthResponse(user, true))
        }
    }
}
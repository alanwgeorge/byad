package com.example.tylerwalker.buyyouadrink.model

import android.util.Log
import com.example.tylerwalker.buyyouadrink.R.drawable.user
import durdinapps.rxfirebase2.RxFirestore
import io.reactivex.Single
import java.util.concurrent.TimeUnit

class UserRepository {

    fun getUser(user: User): Single<UserResponse> {
        val user = User(user.user_id.toString(), "", "", Coordinates(0F, 0F), "")
        return Single.timer(0L, TimeUnit.SECONDS).flatMap {
            Single.just(UserResponse(user, true))
        }
    }

    fun createUser(user: User): Single<UserResponse> {
        val userMap = mutableMapOf<String, Any>(
                "uid" to user.user_id
        )
        return getUser(user)
    }

    fun updateUser(user: User): Single<UserResponse> {
        return Single.timer(0L, TimeUnit.SECONDS).flatMap {
            Single.just(UserResponse(user, true))
        }
    }
}

data class UserResponse(val user: User?, val status: Boolean, val error: String? = null)
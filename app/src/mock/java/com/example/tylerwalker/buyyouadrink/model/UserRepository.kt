package com.example.tylerwalker.buyyouadrink.model

import android.util.Log
import com.example.tylerwalker.buyyouadrink.R.drawable.user
import durdinapps.rxfirebase2.RxFirestore
import io.reactivex.Single
import java.util.concurrent.TimeUnit

class UserRepository {

    fun getUser(uid: String?): Single<User> {
        val user = User(uid.toString(), "", "", Coordinates(0F, 0F), "")
        return Single.timer(0L, TimeUnit.SECONDS).flatMap {
            Single.just(user)
        }
    }

    fun createUser(uid: String, user: User): Single<User> {
        val user = mutableMapOf<String, Any>(
                "uid" to uid
        )
        return getUser(uid)
    }

    fun updateUser(user: User): Single<UserResponse> {
        return Single.timer(0L, TimeUnit.SECONDS).flatMap {
            Single.just(UserResponse(user, true))
        }
    }
}

data class UserResponse(val user: User, val status: Boolean)
package com.example.tylerwalker.buyyouadrink.service

import android.util.Log
import com.example.tylerwalker.buyyouadrink.model.AuthResponse
import com.example.tylerwalker.buyyouadrink.model.Coordinates
import com.example.tylerwalker.buyyouadrink.model.User
import com.google.firebase.auth.FirebaseAuth
import durdinapps.rxfirebase2.RxFirebaseAuth
import io.reactivex.Single
import javax.inject.Singleton

@Singleton
class AuthService {
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun login(email: String, password: String): Single<AuthResponse> {
        val user = User("0","John", "Doe", Coordinates(37.7749F, -122.4194F), "Tea")
        Log.d("AuthService", "logging in...")
        return RxFirebaseAuth.signInWithEmailAndPassword(firebaseAuth, email, password)
                .map {
                    Log.d("AuthService", "login success: ${it.user.email}")
                    AuthResponse(User(it.user.uid, it.user.displayName.toString(), it.user.displayName.toString(), Coordinates(37.7749F, -122.4194F), "Coffee", email = email), true)
                }
                .toSingle()
                .onErrorReturn {
                    Log.d("AuthService", "login error: $it")
                    AuthResponse(null, false)
                }
    }

    fun register(name: String, email: String, password: String): Single<AuthResponse> {
        Log.d("AuthService", "registering...")
        return RxFirebaseAuth.createUserWithEmailAndPassword(firebaseAuth, email, password)
                .map {
                    Log.d("AuthService", "result: ${it.user}")
                    AuthResponse(User(it.user.uid, it.user.displayName.toString(), it.user.displayName.toString(), Coordinates(37.7749F, -122.4194F), "Coffee"), true)
                }
                .toSingle()
                .onErrorReturn {
                    Log.d("AuthService", "error ${it.message}")
                    if (it.message == "The email address is already in use by another account.") {
                        AuthResponse(null, false, "email in use")
                    } else {
                        AuthResponse(null, false, "unknown error")
                    }
                }
    }
}

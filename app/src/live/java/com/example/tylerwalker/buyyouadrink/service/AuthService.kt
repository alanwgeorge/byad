package com.example.tylerwalker.buyyouadrink.service

import android.util.Log
import com.example.tylerwalker.buyyouadrink.R.id.email
import com.example.tylerwalker.buyyouadrink.model.AuthResponse
import com.example.tylerwalker.buyyouadrink.model.Coordinates
import com.example.tylerwalker.buyyouadrink.model.Credentials
import com.example.tylerwalker.buyyouadrink.model.User
import com.google.firebase.auth.FirebaseAuth
import durdinapps.rxfirebase2.RxFirebaseAuth
import io.reactivex.Single
import javax.inject.Singleton

@Singleton
class AuthService {
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun login(credentials: Credentials): Single<AuthResponse> {
        Log.d("AuthService", "logging in with credentials: $credentials")
        return RxFirebaseAuth.signInWithEmailAndPassword(firebaseAuth, credentials.email, credentials.password)
                .map { result ->
                    result.user.uid.let {
                        Log.d("AuthService", "login success for user id: $it")
                        AuthResponse(it, true)
                    }
                }
                .toSingle()
                .onErrorReturn {
                    Log.d("AuthService", "login error: $it")
                    AuthResponse(null, false, it)
                }
    }

    fun register(credentials: Credentials): Single<AuthResponse> {
        Log.d("AuthService", "registering with credentials: $credentials")
        return RxFirebaseAuth.createUserWithEmailAndPassword(firebaseAuth, credentials.email, credentials.password)
                .map { result ->
                    result.user.uid.let {
                        Log.d("AuthService", "user registered with id: $it")
                        AuthResponse(it, true)
                    }
                }
                .toSingle()
                .onErrorReturn {
                    Log.d("AuthService", "error ${it.message}")
                    if (it.message == "The email address is already in use by another account.") {
                        AuthResponse(null, false, it)
                    } else {
                        AuthResponse(null, false, it)
                    }
                }
    }

}

package com.example.tylerwalker.buyyouadrink.model

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.GeoPoint
import durdinapps.rxfirebase2.RxFirestore
import io.reactivex.Single

class UserRepository {
    private val settings = FirebaseFirestoreSettings.Builder()
        .setTimestampsInSnapshotsEnabled(true)
        .build()
    private val firestore = FirebaseFirestore.getInstance()

    constructor() {
        firestore.firestoreSettings = settings
    }

    fun getUser(uid: String?): Single<UserResponse> = uid?.let {
            val doc = firestore.collection("users").document(uid)
            Log.d("UserRepository", "get user with uid: $uid, doc: $doc")

            return RxFirestore.getDocument(doc)
                    .map {
                        Log.d("UserRepository", "user data: ${it.data}")
                        it.data?.let {
                            val user = parseUser(it)
                            Log.d("UserRepository", "parsed user: $user")

                            UserResponse(user, true)
                        } ?: UserResponse(null, false)
                    }
                    .toSingle()
                    .onErrorReturn {
                        Log.d("UserRepository", "error: ${it.message}")
                        UserResponse(null, false)
                    }
        } ?: Single.just(UserResponse(null, false))


    fun createUser(uid: String, user: User): Single<UserResponse> {
        val user = mutableMapOf<String, Any>(
                "uid" to uid
        )
        val userDoc = firestore.collection("users").document(uid)
        RxFirestore.setDocument(userDoc, user).subscribe()
        return getUser(uid)
    }

    fun updateUser(user: User): Single<UserResponse> {
        val oldDoc = firestore.collection("users").document(user.user_id)
        val newDoc = mutableMapOf<String, Any>(
                "uid" to user.user_id,
                "display_name" to user.display_name,
                "email" to user.email,
                "phone" to user.phone,
                "bio" to user.bio,
                "location" to GeoPoint(user.location.latitude.toDouble(), user.location.longitude.toDouble())
        )
        return RxFirestore.setDocument(oldDoc, newDoc)
                .toSingle {
                    UserResponse(user, true)
                }
                .onErrorReturn {
                    UserResponse(user, false)
                }
    }


    private fun parseUser(userMap: MutableMap<String, Any>): User {
        val user = User("", "", "",     Coordinates(0F, 0F), "", "", "", "", "", "", "")
        user.user_id = userMap["user_id"] as String? ?: ""
        user.display_name = userMap["display_name"] as String
        user.location = (userMap["location"] as GeoPoint).let { Coordinates(it.latitude.toFloat(), it.longitude.toFloat()) }
        user.email = userMap["email"] as String? ?: ""
        user.phone = userMap["phone"] as String? ?: ""
        user.bio = userMap["bio"] as String? ?: ""

        return user
    }
}

data class UserResponse(val user: User?, val status: Boolean, val error: String? = null)
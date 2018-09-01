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

    fun getUser(user: User): Single<UserResponse> {
        val uid = user.user_id
        val doc = firestore.collection("users").document(uid)
        Log.d("UserRepository", "get user with uid: $uid, doc: $doc")

        return RxFirestore.getDocument(doc)
                .map {
                    Log.d("UserRepository", "got user: ${it.data}")
                    it.data?.let { UserResponse(parseUser(it), true) }
                            ?: throw Error("No user found")
                }
                .toSingle()
                .onErrorResumeNext {
                    Log.d("UserRepository", "error: ${it.message}")
                    if (it.message == "The MaybeSource is empty") {
                        createUser(user)
                    } else {
                        Single.just(UserResponse(null, false))
                    }
                }
    }


    fun createUser(user: User): Single<UserResponse> {
        val userMap = mutableMapOf<String, Any>(
                "uid" to user.user_id,
                "email" to user.email
        )
        val userDoc = firestore.collection("users").document(user.user_id)
        RxFirestore.setDocument(userDoc, userMap).subscribe()
        return getUser(user)
    }

    fun updateUser(user: User): Single<UserResponse> {
        Log.d("UserRepository", "updateUser: $user")

        val oldDoc = firestore.collection("users").document(user.user_id)
        val newDoc = mutableMapOf<String, Any>(
                "uid" to user.user_id,
                "display_name" to user.display_name,
                "email" to user.email,
                "phone" to user.phone,
                "bio" to user.bio,
                "location" to GeoPoint(user.location.latitude.toDouble(), user.location.longitude.toDouble()),
                "profile_image" to user.profile_image,
                "cover_image" to user.cover_image
        )
        return RxFirestore.setDocument(oldDoc, newDoc)
                .toSingle {
                    UserResponse(user, true)
                }
                .onErrorReturn {
                    UserResponse(user, false, it.message)
                }
    }


    private fun parseUser(userMap: MutableMap<String, Any>): User {
        val user = User("", "", "",     Coordinates(0F, 0F), "", "", "", "", "", "", "")
        user.user_id = userMap["uid"] as String? ?: ""
        user.display_name = userMap["display_name"] as String? ?: ""
        user.location = (userMap["location"] as GeoPoint?).let { Coordinates(it?.latitude?.toFloat() ?: 0F, it?.longitude?.toFloat() ?: 0F) }
        user.email = userMap["email"] as String? ?: ""
        user.phone = userMap["phone"] as String? ?: ""
        user.bio = userMap["bio"] as String? ?: ""
        user.profile_image = userMap["profile_image"] as String? ?: ""
        user.cover_image = userMap["cover_image"] as String? ?: ""

        return user
    }
}

data class UserResponse(val user: User?, val status: Boolean, val error: String? = null)
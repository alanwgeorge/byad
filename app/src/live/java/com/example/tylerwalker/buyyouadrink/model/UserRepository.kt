package com.example.tylerwalker.buyyouadrink.model

import android.util.Log
import com.example.tylerwalker.buyyouadrink.R.drawable.user
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.GeoPoint
import durdinapps.rxfirebase2.RxFirestore
import io.reactivex.Flowable
import io.reactivex.Single

class UserRepository {
    private val settings = FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()
    private val firestore = FirebaseFirestore.getInstance()

    private val logTag = "UserRepository"

    constructor() {
        firestore.firestoreSettings = settings
    }

    fun getUser(uid: String): Flowable<UserResponse> {
        val doc = firestore.collection("users").document(uid)
        Log.d(logTag, "getUser(): uid $uid")

        return RxFirestore.getDocument(doc)
                .map {
                    it.data?.let { userMap ->
                        Log.d(logTag, "getUser() success: $userMap")
                        try {
                            val parsed = parseUser(userMap)
                            UserResponse(parsed, true)
                        } catch (e: Throwable) {
                            Log.d(logTag, "getUser() error parsing user: $userMap")
                            UserResponse(null, false)
                        }
                    } ?: UserResponse(null, false)
                }
                .toFlowable()
    }

    fun getAllUsers(): Flowable<UsersResponse> {
        return RxFirestore.getCollection(firestore.collection("users"))
                .map {
                    val users = mutableListOf<User>()
                    it.documents.forEach {
                        it.data?.let { userMap ->
                            try {
                                val user = parseUser(userMap)
                                users.add(user)
                            } catch (e: Throwable) {
                                Log.d(logTag, "getAllUsers() error parsing user: $userMap")
                            }
                        }
                    }
                    UsersResponse(users, true, null)
                }
                .toFlowable()
    }


    fun createUser(email: String, uid: String): Flowable<UserResponse> {
        val userMap = mutableMapOf<String, Any>(
                "uid" to uid,
                "email" to email
        )
        val userDoc = firestore.collection("users").document(uid)
        RxFirestore.setDocument(userDoc, userMap).subscribe()
        return getUser(uid)
    }

    fun updateUser(user: User): Single<UserResponse> {
        Log.d(logTag, "updateUser(): $user")

        val oldDoc = firestore.collection("users").document(user.user_id)
        val newDoc = mutableMapOf<String, Any>(
                "uid" to user.user_id,
                "display_name" to user.display_name,
                "email" to user.email,
                "phone" to user.phone,
                "bio" to user.bio,
                "location" to GeoPoint(user.location.latitude.toDouble(), user.location.longitude.toDouble()),
                "profile_image" to user.profile_image,
                "cover_image" to user.cover_image,
                "drinks" to user.drinks
        )
        return RxFirestore.setDocument(oldDoc, newDoc)
                .doOnComplete {
                    Log.d(logTag, "updateUser(): success $user")
                }
                .doOnError {
                    Log.d(logTag, "updateUser(): error $it")
                }
                .toSingle {
                    UserResponse(user, true)
                }
    }


    private fun parseUser(userMap: MutableMap<String, Any>): User {
        val user = User()
        user.user_id = userMap["uid"] as String? ?: ""
        user.display_name = userMap["display_name"] as String? ?: ""
        user.location = (userMap["location"] as GeoPoint?).let { Coordinates(it?.latitude?.toFloat() ?: 0F, it?.longitude?.toFloat() ?: 0F) }
        user.email = userMap["email"] as String? ?: ""
        user.phone = userMap["phone"] as String? ?: ""
        user.bio = userMap["bio"] as String? ?: ""
        user.profile_image = userMap["profile_image"] as String? ?: ""
        user.cover_image = userMap["cover_image"] as String? ?: ""
        user.drinks = userMap["drinks"] as String? ?: ""

        return user
    }
}

data class UserResponse(val user: User?, val status: Boolean, val error: Throwable? = null)
data class UsersResponse(val users: List<User>?, val status: Boolean, val error: Throwable? = null)
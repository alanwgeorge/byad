package com.example.tylerwalker.buyyouadrink.activity.profile

import android.app.Application
import android.arch.lifecycle.*
import android.graphics.PorterDuff
import android.media.Image
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.tylerwalker.buyyouadrink.R
import com.example.tylerwalker.buyyouadrink.model.*
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SetupProfileViewModel(app: Application): AndroidViewModel(app), LifecycleObserver {
    val name = MutableLiveData<String>()
    val location = MutableLiveData<Coordinates>()
    val email = MutableLiveData<String>()
    val phone = MutableLiveData<String>()
    val bio = MutableLiveData<String>()
    val likes = MutableLiveData<String>()
    val loves = MutableLiveData<String>()
    val coverImage = MutableLiveData<String>()
    val profileImage = MutableLiveData<String>()

    val drinkSelections = MutableLiveData<MutableList<Drink>>()

    lateinit var activity: SetupProfileActivity

    @Inject
    lateinit var localStorage: LocalStorage

    @Inject
    lateinit var userRepository: UserRepository

    var trash = CompositeDisposable()

    fun save(view: View) {
        Log.d("SetupProfileViewModel", "name: ${name.value}")
        Log.d("SetupProfileViewModel", "location: ${location.value}")
        Log.d("SetupProfileViewModel", "email: ${email.value}")
        Log.d("SetupProfileViewModel", "phone: ${phone.value}")
        Log.d("SetupProfileViewModel", "bio: ${bio.value}")
        Log.d("SetupProfileViewModel", "likes: ${likes.value}")
        Log.d("SetupProfileViewModel", "loves: ${bio.value}")
        Log.d("SetupProfileViewModel", "profile_image: ${profileImage.value}")
        Log.d("SetupProfileViewModel", "cover image: ${coverImage.value}")

        val user = localStorage.getCurrentUser() ?: return

        Log.d("SetupProfileViewModel", "current user: $user")

        var hasShownError = false

        name.value?.let {
            if (it.length < 30) {
                user.display_name = it
            } else {
                if (!hasShownError) {
                    hasShownError = true
                    Toast.makeText(activity, "Display name should be less than 30 characters.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        location.value?.let {
            user.location = it
        }

        email.value?.let {
            if (Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                user.email = it
            } else {
                if (!hasShownError) {
                    hasShownError = true
                    Toast.makeText(activity, "Invalid Email Address.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        phone.value?.let {
            if (it.length == 10 && it.all { it.isDigit() }) {
                user.phone = it
            } else {
                if (!hasShownError) {
                    hasShownError = true
                    Toast.makeText(activity, "Invalid Phone Number.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        bio.value?.let {
            if (it.length > 15) {
                user.bio = it
            } else {
                if (!hasShownError) {
                    hasShownError = true
                    Toast.makeText(activity, "Bio should be at least 15 characters.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        likes.value?.let { user.likes = it }
        loves.value?.let { user.loves = it }

        profileImage.value?.let { user.profile_image = it }
        coverImage.value?.let { user.cover_image = it }

        drinkSelections.value?.map {
            it.name
        }?.let {
            user.drinks = it.joinToString(",")
        }

        if (hasShownError) {
            hasShownError = false
        } else {
            Log.d("user", "update user: $user")
            userRepository.updateUser(user)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(UserRepositoryObserver())
        }
    }

    fun setColorFilter(view: View, drinkName: String) {
        val drink = getDrink(drinkName)
        view as ConstraintLayout

        val darkColor = activity.resources.getColor(android.R.color.holo_blue_dark, null)

        if (!isDrinkSelected(drink)) {
            addDrinkToSelections(drink)

            view.getChildAt(0).let {
                it as ImageView
                it.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN)
            }

            view.getChildAt(1).let {
                it as TextView
                it.setTextColor(darkColor)
            }

            view.getChildAt(2).let {
                it as ImageView
                it.setColorFilter(darkColor, PorterDuff.Mode.SRC_IN)
            }
        } else {
            removeDrinkFromSelections(drink)

            view.getChildAt(0).let {
                it as ImageView
                it.clearColorFilter()
            }

            view.getChildAt(1).let {
                it as TextView
                it.setTextColor(activity.resources.getColor(android.R.color.black, null))
            }

            view.getChildAt(2).let {
                it as ImageView
                it.clearColorFilter()
            }
        }
    }

    private fun getDrink(drinkName: String): Drink {
        return when(drinkName) {
            "Coffee" -> Drink.Coffee
            "BubbleTea" -> Drink.BubbleTea
            "Beer" -> Drink.Beer
            "Juice" -> Drink.Juice
            else -> Drink.Coffee
        }
    }

    private fun addDrinkToSelections(drink: Drink) =
        drinkSelections.value?.run {
            if (!contains(drink)) {
                add(drink)
            }
        }

    private fun removeDrinkFromSelections(drink: Drink) =
            drinkSelections.value?.run {
                if (contains(drink)) {
                    remove(drink)
                }
            }

    fun isDrinkSelected(drink: Drink) = drinkSelections.value?.contains(drink) ?: false

    inner class UserRepositoryObserver: SingleObserver<UserResponse> {
        override fun onSubscribe(d: Disposable) {
            trash.add(d)
        }

        override fun onError(e: Throwable) {
            Log.d("NETWORK", e.message)
        }

        override fun onSuccess(t: UserResponse) {
            Log.d("SetupProfileViewModel", "userResponse: $t")
            if (t.status) {
                Toast.makeText(activity, "Profile Updated.", Toast.LENGTH_LONG).show()
                activity.transitionToHome()

            } else {
                Log.d("SetupProfileViewModel", "${t.error}")
                Toast.makeText(activity, "Oops, Something went wrong...", Toast.LENGTH_LONG).show()
            }
        }
    }

    inner class GooglePlaceSelectionListener: PlaceSelectionListener {
        override fun onPlaceSelected(place: Place?) {
            place?.latLng?.let {
                location.value = Coordinates(it.latitude.toFloat(), it.longitude.toFloat())
            }
        }

        override fun onError(p0: Status?) {
            Log.e("SetupProfileViewModel", "error: ${p0?.status}")
        }
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun newTrash() {
        trash = CompositeDisposable()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun emptyTrash() {
        trash.clear()
        trash.dispose()
    }
}
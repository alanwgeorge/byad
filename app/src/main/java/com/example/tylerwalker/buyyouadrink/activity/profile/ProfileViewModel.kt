package com.example.tylerwalker.buyyouadrink.activity.profile

import android.app.Application
import android.arch.lifecycle.*
import android.graphics.Bitmap
import com.example.tylerwalker.buyyouadrink.R
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import com.example.tylerwalker.buyyouadrink.R.drawable.user
import com.example.tylerwalker.buyyouadrink.R.id.profile_cover_image

import com.example.tylerwalker.buyyouadrink.activity.map.InvitationViewModel.Companion.logTag
import com.example.tylerwalker.buyyouadrink.model.*
import com.example.tylerwalker.buyyouadrink.service.ConversationService
import com.example.tylerwalker.buyyouadrink.service.LocationService
import com.example.tylerwalker.buyyouadrink.util.rotate
import com.example.tylerwalker.buyyouadrink.util.toBitmap
import com.example.tylerwalker.buyyouadrink.util.toDrawable
import com.example.tylerwalker.buyyouadrink.util.toRoundedDrawable
import com.example.tylerwalker.buyyouadrink.view.RoundedMask
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import kotlinx.android.synthetic.main.activity_profile.profile_cover_image

import javax.inject.Inject

class ProfileViewModel(app: Application): AndroidViewModel(app), LifecycleObserver {
    @Inject
    lateinit var conversationService: ConversationService

    @Inject
    lateinit var currentUser: User

    lateinit var userId: String
    lateinit var user: User
    val displayName = MutableLiveData<String>()
    val bio = MutableLiveData<String>()
    val drinks = MutableLiveData<MutableList<Drink>>()
    val favoriteDrink = MutableLiveData<Drink>()
    val favoriteDrinkDrawable = MediatorLiveData<Drawable>().apply {
        addSource(favoriteDrink) {
            it?.let { drink ->
                when (drink) {
                    Drink.Coffee -> { postValue(getApplication<Application>().getDrawable(R.drawable.ic_coffee))}
                    Drink.Beer -> { postValue(getApplication<Application>().getDrawable(R.drawable.ic_beer))}
                    Drink.Juice -> { postValue(getApplication<Application>().getDrawable(R.drawable.ic_juice))}
                    Drink.BubbleTea -> { postValue(getApplication<Application>().getDrawable(R.drawable.ic_bubble_tea))}
                }
            }
        }
    }
    val coffeeVisibility = MediatorLiveData<Int>().apply {
        addSource(drinks) {
            it?.let {
                Log.d("visibility:", "some drinks: ${it}")
                when (it.contains(Drink.Coffee)) {
                    true -> postValue(View.VISIBLE)
                    else -> postValue(View.GONE)
                }
            }
        }
    }
    val bubbleTeaVisibility = MediatorLiveData<Int>().apply {
        addSource(drinks) {
            it?.let {
                when (it.contains(Drink.BubbleTea)) {
                    true -> postValue(View.VISIBLE)
                    else -> postValue(View.GONE)
                }
            }
        }
    }
    val juiceVisibility = MediatorLiveData<Int>().apply {
        addSource(drinks) {
            it?.let {
                when (it.contains(Drink.Juice)) {
                    true -> postValue(View.VISIBLE)
                    else -> postValue(View.GONE)
                }
            }
        }
    }
    val beerVisibility = MediatorLiveData<Int>().apply {
        addSource(drinks) {
            it?.let {
                when (it.contains(Drink.Beer)) {
                    true -> postValue(View.VISIBLE)
                    else -> postValue(View.GONE)
                }
            }
        }
    }
    val location = MutableLiveData<String>()
    val profileImage = MutableLiveData<Drawable>()
    val coverImage = MutableLiveData<Bitmap>()
    val buttonText = MutableLiveData<String>()
    var primaryButtonAction: () -> Unit = { buyUserADrink() }

    fun primaryClickListener(view: View) {
        primaryButtonAction()
    }

    @Inject
    lateinit var profileEventsProcessor: PublishProcessor<ProfileEvent>

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var locationService: LocationService

    private var trash = CompositeDisposable()

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        trash.add(getUser(userId))
    }

    private fun getUser(userId: String): Disposable =
            userRepository.getUser(userId)
                    .doOnNext {userResponse ->
                        userResponse.user?.apply {
                            user = this

                            display_name?.let {
                                displayName.value = it
                            }

                            bio?.let {
                                this@ProfileViewModel.bio.value = it
                            }

                            val userDrinks = mutableListOf<Drink>()
                            drinks
                                    .split(",")
                                    .forEach { drinkName ->
                                        userDrinks.add(Drink.getDrink(drinkName))
                                    }
                            if (userDrinks.size > 0) {
                                this@ProfileViewModel.drinks.value = userDrinks
                            }

                            favoriteDrink?.let {
                                this@ProfileViewModel.favoriteDrink.value = Drink.getDrink(it)
                            }

                            location?.let {
                                locationService.getLocationName(getApplication(), it)?.let {locationName ->
                                    this@ProfileViewModel.location.value = locationName
                                }
                            }

                            if (!profile_image.isEmpty()) {
                                val res = this@ProfileViewModel.getApplication<Application>().resources
                                val round = profile_image.toBitmap()?.rotate()?.toRoundedDrawable(res)
                                round?.let { roundedProfileDrawable ->
                                    profileImage.value = roundedProfileDrawable
                                }
                            }

                            if (!cover_image.isEmpty()) {
                                cover_image.toBitmap()?.let { bitmap ->
                                    coverImage.value = bitmap
                                }

                            }
                        } ?: handleUserError()
                    }
                    .doOnError {err ->
                        Log.d(logTag, "get user error: ${err.localizedMessage}")
                        handleUserError()
                    }
                    .subscribe {
                        getConversations()
                    }

    private fun getConversations(): Disposable = conversationService.getConversations(currentUser.user_id)
            .map { it.conversations ?: throw Exception("No conversations") }
            .doOnNext { Log.d(logTag, "got conversations: $it") }
            .map { filterRejectedConversations(it) }
            .subscribe({ existingConversations ->
                existingConversations.find { it.withId == user.user_id }?.let {
                    buttonText.value = "View Conversation"
                    primaryButtonAction = { viewConversation(it) }
                }
            }, {
                Log.d(logTag, "getConversations() error ${it.localizedMessage}")
            })

    private fun filterRejectedConversations(conversations: List<Conversation>): List<Conversation> {
        return conversations.filter {
            !it.isRejected
        }
    }

    private fun handleUserError() {
        profileEventsProcessor.onNext(ProfileEvent.UserError(null))
    }

    fun buyUserADrink() {
        profileEventsProcessor.onNext(ProfileEvent.BuyUserADrink(user))
    }

    fun viewConversation(conversation: Conversation) {
        profileEventsProcessor.onNext(ProfileEvent.GoToConversation(conversation))
    }


    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        trash.clear()
        trash = CompositeDisposable()
    }
}
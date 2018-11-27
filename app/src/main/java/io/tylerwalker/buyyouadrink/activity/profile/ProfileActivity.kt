package io.tylerwalker.buyyouadrink.activity.profile

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.view.View
import android.widget.Toast
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.tylerwalker.buyyouadrink.R
import io.tylerwalker.buyyouadrink.activity.home.HomeScreen
import io.tylerwalker.buyyouadrink.activity.map.MapActivity
import io.tylerwalker.buyyouadrink.activity.messages.ConversationActivity
import io.tylerwalker.buyyouadrink.model.Conversation
import io.tylerwalker.buyyouadrink.model.ProfileEvent
import io.tylerwalker.buyyouadrink.model.User
import io.tylerwalker.buyyouadrink.module.App
import kotlinx.android.synthetic.main.activity_profile.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import io.tylerwalker.buyyouadrink.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    lateinit var viewModel: ProfileViewModel

    @Inject
    lateinit var profileEventsFlowable: Flowable<ProfileEvent>

    private val logTag = "ProfileActivity"
    private var trash = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val component = App().getComponent(this)
        component.inject(this)

        val binding = DataBindingUtil.setContentView<ActivityProfileBinding>(this, R.layout.activity_profile)
        binding.setLifecycleOwner(this)

        val user_id = intent?.extras?.getString("user_id")

        if (user_id == null) {
            handleNoUserError()
            return
        }

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java).apply {
            component.inject(this)
            lifecycle.addObserver(this)
            binding.viewModel = this

            userId = user_id
            drinks.value = mutableListOf()
            profileImage.value = getDrawable(R.drawable.ic_user)
            favoriteDrinkDrawable.value = getDrawable(R.drawable.ic_bubble_tea)

            coffeeVisibility.value = View.VISIBLE
            beerVisibility.value = View.VISIBLE
            bubbleTeaVisibility.value = View.VISIBLE
            juiceVisibility.value = View.VISIBLE

            coverImage.observe(this@ProfileActivity, Observer {
                it?.let { bitmap ->
                    profile_cover_image.updateBitmap(bitmap)
                }
            })

            buttonText.value = "Buy Them A Drink"
        }

        trash.add(profileEventsFlowable
                .filter { it is ProfileEvent.UserError }
                .doOnNext { handleNoUserError() }
                .subscribe()
        )

        trash.add(profileEventsFlowable
                .filter { it is ProfileEvent.BuyUserADrink }
                .map { it as ProfileEvent.BuyUserADrink }
                .doOnNext { transitionToMap(it.user)}
                .subscribe()
        )

        trash.add(profileEventsFlowable
                .filter { it is ProfileEvent.GoToConversation }
                .map { it as ProfileEvent.GoToConversation }
                .doOnNext { transitionToConversation(it.conversation)}
                .subscribe()
        )

        resetDrinkDrawableFilters()
    }

    private fun resetDrinkDrawableFilters() {
        getDrawable(R.drawable.ic_coffee)?.clearColorFilter()
        getDrawable(R.drawable.ic_beer)?.clearColorFilter()
        getDrawable(R.drawable.ic_juice)?.clearColorFilter()
        getDrawable(R.drawable.ic_bubble_tea)?.clearColorFilter()
    }

    fun handleNoUserError() {
        Toast.makeText(this, "Sorry, we could not find that user's information!", Toast.LENGTH_SHORT).show()
        Flowable.timer(2000L, TimeUnit.SECONDS)
                .doOnComplete { transitionToHome() }
                .subscribe()
    }

    fun transitionToMap(user: User) {
        val intent = Intent(this, MapActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
    }

    fun transitionToHome() {
        val intent = Intent(this, HomeScreen::class.java)
        startActivity(intent)
    }

    fun transitionToConversation(conversation: Conversation) {
        val intent = Intent(this, ConversationActivity::class.java)
        intent.putExtra("conversation", conversation)
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        trash.clear()
        trash = CompositeDisposable()
    }

}

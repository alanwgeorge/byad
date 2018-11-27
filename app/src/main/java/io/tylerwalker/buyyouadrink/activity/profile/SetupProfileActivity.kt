package io.tylerwalker.buyyouadrink.activity.profile

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.graphics.PorterDuff
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.tylerwalker.buyyouadrink.R
import io.tylerwalker.buyyouadrink.activity.home.HomeScreen
import io.tylerwalker.buyyouadrink.model.Drink
import io.tylerwalker.buyyouadrink.model.NavigationEvent
import io.tylerwalker.buyyouadrink.model.ProfileEvent
import io.tylerwalker.buyyouadrink.module.App
import io.tylerwalker.buyyouadrink.service.LocationService
import io.tylerwalker.buyyouadrink.util.rotate
import io.tylerwalker.buyyouadrink.util.toBitmap
import io.tylerwalker.buyyouadrink.util.toEncodedString
import io.tylerwalker.buyyouadrink.util.toRoundedDrawable
import kotlinx.android.synthetic.main.activity_setup_profile.*
import java.io.IOException
import javax.inject.Inject
import io.tylerwalker.buyyouadrink.databinding.ActivitySetupProfileBinding

class SetupProfileActivity : AppCompatActivity() {
    @Inject
    lateinit var locationService: LocationService

    @Inject
    lateinit var profileEventsFlowable: Flowable<ProfileEvent>
    @Inject
    lateinit var navigationEventsFlowable: Flowable<NavigationEvent>

    private val favoriteDrinkDialogTag = "favoriteDrinkDialog"
    private val favoriteDrinkDialog: FavoriteDrinkDialog by lazy {
        FavoriteDrinkDialog()
    }

    var trash = CompositeDisposable()

    private val logTag = "SetupProfileActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dagger 2 component
        val component = App().getComponent(this)
        component.inject(this)

        val binding: ActivitySetupProfileBinding = DataBindingUtil.setContentView(this, R.layout.activity_setup_profile)
        binding.setLifecycleOwner(this)

        ViewModelProviders.of(this).get(SetupProfileViewModel::class.java).apply {
            component.inject(this)
            lifecycle.addObserver(this)
            binding.viewmodel = this

            val user = localStorage.getCurrentUser()

            val autocomplete = fragmentManager.findFragmentById(R.id.location_text) as PlaceAutocompleteFragment
            autocomplete.setOnPlaceSelectedListener(GooglePlaceSelectionListener())


            drinkSelections.value = mutableListOf()

            user?.let {
                Log.d(logTag, it.toString())
                it.display_name.let { name.value = it; name_text.setText(it) }
                it.location.let { location.value = it; autocomplete.setText(locationService.getLocationName(this@SetupProfileActivity, it)) }
                it.email.let { email.value = it; email_text.setText(it) }
                it.phone.let { phone.value = it; phone_text.setText(it) }
                it.bio.let { bio.value = it; bio_text.setText(it) }
                it.profile_image.let {
                    profileImage.value = it
                    val round = it.toBitmap()?.rotate()?.toRoundedDrawable(resources)
                    profile_image.setImageDrawable(round)
                    profile_image.scaleType = ImageView.ScaleType.CENTER_CROP
                }
                it.cover_image.let { coverImage.value = it; profile_cover_image.updateBitmap(it.toBitmap()) }
                it.drinks
                    .split(",")
                    .forEach { drinkName -> addDrinkToSelections(getDrink(drinkName)) }
                it.favoriteDrink?.let {  drinkName ->
                    if (!drinkName.isNullOrEmpty()) {
                        favoriteDrink.value = getDrink(drinkName)
                    }
                }
            }

            favoriteDrink.observe(this@SetupProfileActivity, Observer {
                it?.let {
                    when (it) {
                        Drink.Coffee -> {
                            favorite_drink_text.text = "Coffee"
                            favorite_drink_icon.setImageDrawable(getDrawable(R.drawable.ic_coffee))
                            favorite_drink_icon.scaleType = ImageView.ScaleType.FIT_XY
                        }
                        Drink.Juice -> {
                            favorite_drink_text.text = "Juice"
                            favorite_drink_icon.setImageDrawable(getDrawable(R.drawable.ic_juice))
                            favorite_drink_icon.scaleType = ImageView.ScaleType.FIT_XY
                        }
                        Drink.BubbleTea -> {
                            favorite_drink_text.text = "BubbleTea"
                            favorite_drink_icon.setImageDrawable(getDrawable(R.drawable.ic_bubble_tea))
                            favorite_drink_icon.scaleType = ImageView.ScaleType.FIT_XY
                        }
                        Drink.Beer -> {
                            favorite_drink_text.text = "Beer"
                            favorite_drink_icon.setImageDrawable(getDrawable(R.drawable.ic_beer))
                            favorite_drink_icon.scaleType = ImageView.ScaleType.FIT_XY
                        }
                    }
                    favorite_drink_text.setTextColor(getColor(R.color.colorPrimary))
                }
            })

            favoriteDrink.value.let {
                if (it == null) {
                    favoriteDrinkDialog.show(supportFragmentManager, favoriteDrinkDialogTag)
                }
            }
        }

        trash.add(observeDrinkToggleEvents())
        trash.add(observeNavigationEvents())
        trash.add(observeChooseProfileImageEvents())
        trash.add(observeChooseCoverImageEvents())
        trash.add(observeDismissFavoriteDrinkDialogEvents())
        trash.add(observeShowFavoriteDrinkDialogEvents())

    }

    private fun observeDrinkToggleEvents(): Disposable = profileEventsFlowable
            .filter { it is ProfileEvent.ToggleDrink }
            .map { it as ProfileEvent.ToggleDrink }
            .doOnNext { Log.d(logTag, "ProfileEvent: ToggleDrink: ${it.drink}, ${it.drink.isSelected}") }
            .map { updateUIForDrink(it.drink) }
            .subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()

    private fun observeChooseProfileImageEvents(): Disposable = profileEventsFlowable
            .filter { it === ProfileEvent.ChooseProfileImage }
            .map { chooseProfileImage() }
            .subscribe()

    private fun observeChooseCoverImageEvents(): Disposable = profileEventsFlowable
            .filter { it === ProfileEvent.ChooseCoverImage }
            .map { chooseCoverImage() }
            .subscribe()

    private fun observeNavigationEvents(): Disposable = navigationEventsFlowable
            .filter { it === NavigationEvent.Home }
            .doOnNext { transitionToHome() }
            .subscribe()

    private fun observeShowFavoriteDrinkDialogEvents(): Disposable = profileEventsFlowable
            .filter { it === ProfileEvent.ShowFavoriteDrinkDialog }
            .doOnNext {
                if (!favoriteDrinkDialog.isAdded) {
                    favoriteDrinkDialog.show(supportFragmentManager, favoriteDrinkDialogTag)
                }
            }
            .subscribe()

    private fun observeDismissFavoriteDrinkDialogEvents(): Disposable = profileEventsFlowable
            .filter { it === ProfileEvent.DismissFavoriteDrinkDialog }
            .doOnNext { favoriteDrinkDialog.dismiss() }
            .subscribe()

    private fun updateUIForDrink(drink: Drink) {
        Log.d(logTag, "updateUIForDrink(): $drink")

        val view = getLayout(drink)

        val primary = ContextCompat.getColor(this, R.color.colorPrimary)
        val black = resources.getColor(android.R.color.black, null)

        if (drink.isSelected) {
            view.getChildAt(0).let {
                it as ImageView
                it.setColorFilter(primary, PorterDuff.Mode.SRC_IN)
            }

            when (drink) {
                Drink.Coffee -> { coffee_text.setTextColor(primary) }
                Drink.Juice -> { juice_text.setTextColor(primary)}
                Drink.BubbleTea -> { bubble_tea_text.setTextColor(primary)}
                Drink.Beer -> { beer_text.setTextColor(primary)}
            }

            view.getChildAt(2).let {
                it as ImageView
                it.setColorFilter(primary, PorterDuff.Mode.SRC_IN)
            }
        } else {
            view.getChildAt(0).let {
                it as ImageView
                it.clearColorFilter()
            }

            when (drink) {
                Drink.Coffee -> { coffee_text.setTextColor(black) }
                Drink.Juice -> { juice_text.setTextColor(black)}
                Drink.BubbleTea -> { bubble_tea_text.setTextColor(black)}
                Drink.Beer -> { beer_text.setTextColor(black)}
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

    private fun getLayout(drink: Drink): ConstraintLayout = when (drink) {
        Drink.BubbleTea -> bubble_tea_layout
        Drink.Juice -> juice_layout
        Drink.Coffee -> coffee_layout
        Drink.Beer -> beer_layout
    }

    fun chooseProfileImage() {
        val profileImageRequest = 1
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), profileImageRequest)
    }

    fun chooseCoverImage() {
        val coverImageRequest = 2
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(Intent.createChooser(intent, "Select Cover Image"), coverImageRequest)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val profileImageRequest = 1
        val coverImageRequest = 2

        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            // result code is success
            val fileUri = data?.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, fileUri)
                Log.d(logTag, "bitmap: $bitmap")

                ViewModelProviders.of(this).get(SetupProfileViewModel::class.java).apply {
                    when (requestCode) {
                        profileImageRequest -> {
                            profileImage.value = bitmap.toEncodedString()

                            profile_image.setImageBitmap(bitmap)
                            val round = bitmap.toRoundedDrawable(resources)
                            profile_image.setImageDrawable(round)
                            profile_image.scaleType = ImageView.ScaleType.CENTER_CROP
                        }
                        coverImageRequest -> {
                            profile_cover_image.updateBitmap(bitmap)
                            coverImage.value = bitmap.toEncodedString()
                        }
                        else -> {}
                    }
                }
            } catch (e: IOException) {
                Log.d(logTag, "exception: ${e.message}")
                Toast.makeText(this, "There was a problem with that image.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Something went wrong.", Toast.LENGTH_SHORT).show()
        }
    }

    fun transitionToHome() {
        val intent = Intent(this, HomeScreen::class.java)
        startActivity(intent)
    }

    sealed class ProfileImage {
        object Avatar: ProfileImage()
        object CoverImage: ProfileImage()

    }
}

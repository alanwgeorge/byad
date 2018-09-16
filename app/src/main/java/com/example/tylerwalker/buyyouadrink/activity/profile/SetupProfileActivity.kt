package com.example.tylerwalker.buyyouadrink.activity.profile

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.example.tylerwalker.buyyouadrink.R
import com.example.tylerwalker.buyyouadrink.activity.home.HomeScreen
import com.example.tylerwalker.buyyouadrink.databinding.ActivitySetupProfileBinding
import com.example.tylerwalker.buyyouadrink.model.Drink
import com.example.tylerwalker.buyyouadrink.module.App
import com.example.tylerwalker.buyyouadrink.service.LocationService
import com.example.tylerwalker.buyyouadrink.util.toBitmap
import com.example.tylerwalker.buyyouadrink.util.toEncodedString
import com.example.tylerwalker.buyyouadrink.util.toRoundedDrawable
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import kotlinx.android.synthetic.main.activity_setup_profile.*
import java.io.IOException
import javax.inject.Inject

class SetupProfileActivity : AppCompatActivity() {
    @Inject
    lateinit var locationService: LocationService

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

            val user = localStorage.getCurrentUser()

            val autocomplete = fragmentManager.findFragmentById(R.id.location_text) as PlaceAutocompleteFragment
            autocomplete.setOnPlaceSelectedListener(GooglePlaceSelectionListener())

            name.value = null
            location.value = null
            email.value = null
            phone.value = null
            bio.value = null
            likes.value = null
            loves.value = null
            drinkSelections.value = mutableListOf()

            user?.let {
                Log.d("SetupProfileActivity", it.toString())
                it.display_name?.let { name.value = it; name_text.setText(it) }
                it.location?.let { location.value = it; autocomplete.setText(locationService.getLocationName(this@SetupProfileActivity, it)) }
                it.email?.let { email.value = it; email_text.setText(it) }
                it.phone?.let { phone.value = it; phone_text.setText(it) }
                it.bio?.let { bio.value = it; bio_text.setText(it) }
                it.likes?.let { likes.value = it; likes_text.setText(it) }
                it.loves?.let { loves.value = it; loves_text.setText(it) }
                it.profile_image?.let {
                    profileImage.value = it
                    val round = it.toBitmap()?.toRoundedDrawable(resources)
                    profile_image.setImageDrawable(round)
                    profile_image.scaleType = ImageView.ScaleType.CENTER_CROP
                }
                it.cover_image?.let { coverImage.value = it; profile_cover_image.updateBitmap(it.toBitmap()) }
                val drinks = mutableListOf<Drink>()
                it.drinks?.let {
                    it.split(",").forEach {
                        drinks.add(getDrink(it))
                    }
                }
                drinkSelections.value = drinks
            }

            activity = this@SetupProfileActivity
            binding.viewmodel = this
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
                Log.d("SetupProfileActivity", "bitmap: $bitmap")

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
                Log.d("SetupProfileActivity", "exception: ${e.message}")
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

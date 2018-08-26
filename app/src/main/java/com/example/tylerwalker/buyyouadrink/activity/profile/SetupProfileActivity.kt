package com.example.tylerwalker.buyyouadrink.activity.profile

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.tylerwalker.buyyouadrink.R
import com.example.tylerwalker.buyyouadrink.databinding.ActivitySetupProfileBinding
import com.example.tylerwalker.buyyouadrink.model.LocalStorage
import com.example.tylerwalker.buyyouadrink.model.UserRepository
import com.example.tylerwalker.buyyouadrink.module.App
import com.example.tylerwalker.buyyouadrink.service.LocationService
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import kotlinx.android.synthetic.main.activity_setup_profile.*
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
            val user = localStorage.getCurrentUser()

            val autocomplete = fragmentManager.findFragmentById(R.id.location_text) as PlaceAutocompleteFragment
            autocomplete.setOnPlaceSelectedListener(GooglePlaceSelectionListener())

            name.value = null
            location.value = null
            email.value = null
            phone.value = null
            bio.value = null

            user?.let {
                Log.d("SetupProfileActivity", it.toString())
                it.display_name?.let { name.value = it; name_text.setText(it) }
                it.location?.let { location.value = it; autocomplete.setText(locationService.getLocationName(this@SetupProfileActivity, it)) }
                it.email?.let { email.value = it; email_text.setText(it) }
                it.phone?.let { phone.value = it; phone_text.setText(it) }
                it.bio?.let { bio.value = it; bio_text.setText(it) }
            }

            activity = this@SetupProfileActivity
            binding.viewmodel = this
        }
    }
}

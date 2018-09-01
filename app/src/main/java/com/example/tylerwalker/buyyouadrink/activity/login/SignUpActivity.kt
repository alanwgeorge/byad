package com.example.tylerwalker.buyyouadrink.activity.login

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.tylerwalker.buyyouadrink.R
import com.example.tylerwalker.buyyouadrink.databinding.ActivitySignUpBinding
import com.example.tylerwalker.buyyouadrink.module.App
import com.example.tylerwalker.buyyouadrink.service.AuthService
import javax.inject.Inject

class SignUpActivity : AppCompatActivity() {

    @Inject
    lateinit var authService: AuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dagger 2 component
        val component = App().getComponent(this)
        component.inject(this)

        val binding: ActivitySignUpBinding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        binding.setLifecycleOwner(this)

        ViewModelProviders.of(this).get(SignUpViewModel::class.java).apply {
            component.inject(this)
            activity = this@SignUpActivity
            binding.viewmodel = this

            name.value = ""
            email.value = ""
            password.value = ""
            confirm.value = ""

        }
    }
}

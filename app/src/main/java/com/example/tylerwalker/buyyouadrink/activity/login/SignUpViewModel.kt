package com.example.tylerwalker.buyyouadrink.activity.login

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v4.content.ContextCompat.startActivity
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.tylerwalker.buyyouadrink.activity.home.HomeScreen
import com.example.tylerwalker.buyyouadrink.activity.onboarding.OnBoarding
import com.example.tylerwalker.buyyouadrink.model.AuthResponse
import com.example.tylerwalker.buyyouadrink.model.Credentials
import com.example.tylerwalker.buyyouadrink.model.LocalStorage
import com.example.tylerwalker.buyyouadrink.service.AuthService
import com.google.android.gms.flags.impl.SharedPreferencesFactory.getSharedPreferences
import com.google.gson.Gson
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.regex.Pattern
import javax.inject.Inject

class SignUpViewModel(app: Application): AndroidViewModel(app), LifecycleObserver {
    lateinit var activity: SignUpActivity

    @Inject
    lateinit var authService: AuthService

    @Inject
    lateinit var localStorage: LocalStorage

    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val name = MutableLiveData<String>()
    val confirm = MutableLiveData<String>()


    fun register(view: View) {
        if (!verifyInput()) return
        Log.d("user", "name: ${name.value}, email: ${email.value}, password: ${password.value}")
        asyncLogin(name.value!!, email.value!!, password.value!!)
    }

    private fun verifyInput(): Boolean {
        val emailIsValid = email.value?.let { Patterns.EMAIL_ADDRESS.matcher(it).matches() } ?: false
        val passwordIsValid = password.value?.let {
            it.length >= 8 && it == confirm.value
        } ?: false
        val nameIsValid = name.value?.let { it.length >= 8 } ?: false

        Log.d("user", "validation, name: ${nameIsValid}, email: ${emailIsValid}, password: ${passwordIsValid}")

        return emailIsValid && passwordIsValid && nameIsValid
    }

    private fun asyncLogin(name: String, email: String, password: String) {
        try {
            authService.register(Credentials(email, password))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(RegisterObserver())
        } catch(error: Exception) {
            Log.d("NETWORK", error.message)
        }
    }

    inner class RegisterObserver: SingleObserver<AuthResponse> {
        override fun onSubscribe(d: Disposable) {
        }

        override fun onError(e: Throwable) {
            Log.d("SignUpViewModel", e.message)
        }

        override fun onSuccess(t: AuthResponse) {
            handleResponse(t)
        }
    }

    private fun handleResponse(response: AuthResponse) {
        Log.d("user", "response: ${response}")
        if (!response.status) {
            return
        }

        var intent: Intent?

        if (localStorage.isFirstRun()) {
            intent = Intent(activity, OnBoarding::class.java)
        } else {
            intent = Intent(activity, HomeScreen::class.java)
        }

        activity.startActivity(intent)



    }

}


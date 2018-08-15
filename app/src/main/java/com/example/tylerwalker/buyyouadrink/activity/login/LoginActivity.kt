package com.example.tylerwalker.buyyouadrink.activity.login

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View

import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.example.tylerwalker.buyyouadrink.R
import com.example.tylerwalker.buyyouadrink.activity.home.HomeScreen
import com.example.tylerwalker.buyyouadrink.activity.onboarding.OnBoarding
import com.example.tylerwalker.buyyouadrink.model.AuthResponse
import com.example.tylerwalker.buyyouadrink.module.App
import com.example.tylerwalker.buyyouadrink.service.AuthService
import com.google.gson.Gson
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

import kotlinx.android.synthetic.main.activity_new_login.*
import javax.inject.Inject

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {
    val SHARED_PREFERENCES_CURRENT_USER_KEY = "current_user"

    @Inject lateinit var authService: AuthService

    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_login)

        // Dagger 2 component
        App().getComponent().inject(this)

        sharedPreferences  = getSharedPreferences("com.example.tylerwalker.buyyouadrink", Context.MODE_PRIVATE)

        sign_in_button.setOnClickListener { attemptLogin() }
    }

    private fun attemptLogin() {
        username_text.error = null
        password_text.error = null

        // Store values at the time of the login attempt.
        val usernameStr = username_text.text.toString()
        val passwordStr = password_text.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(passwordStr) && !isPasswordValid(passwordStr)) {
            password_text.error = getString(R.string.error_invalid_password)
            focusView = password_text
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(usernameStr)) {
            username_text.error = getString(R.string.error_field_required)
            focusView = username_text
            cancel = true
        } else if (!isEmailValid(usernameStr)) {
            username_text.error = getString(R.string.error_invalid_email)
            focusView = username_text
            cancel = true
        }

        if (cancel) {
            focusView?.requestFocus()
        } else {
            asyncLogin(usernameStr, passwordStr)
        }
    }

    private fun isEmailValid(username: String): Boolean {
        //TODO: Replace this with your own logic
        return true
    }

    private fun isPasswordValid(password: String): Boolean {
        //TODO: Replace this with your own logic
        return true
    }

    /**
     * Shows the progress UI and hides the login form.
     */


    private fun asyncLogin(email: String, password: String) {
        try {
            authService.login(email, password)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(LoginObserver())
        } catch(error: Exception) {
            Log.d("NETWORK", error.message)
        }
    }

    inner class LoginObserver: SingleObserver<AuthResponse> {
        override fun onSubscribe(d: Disposable) {
        }

        override fun onError(e: Throwable) {
            Log.d("NETWORK", e.message)
        }

        override fun onSuccess(t: AuthResponse) {
            handleLoginResponse(t)
        }
    }

    private fun handleLoginResponse(response: AuthResponse) {
        var intent: Intent

        val json = Gson().toJson(response.user)
        sharedPreferences.edit().putString(SHARED_PREFERENCES_CURRENT_USER_KEY, json).apply()

        if (sharedPreferences.getBoolean("firstrun", true)) {
            sharedPreferences.edit().putBoolean("firstrun", false).commit()
            intent = Intent(this, OnBoarding::class.java)
        } else {
            intent = Intent(this, HomeScreen::class.java)
        }
        startActivity(intent)
    }

    fun transitionToSignUp(view: View) {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

}

package com.example.tylerwalker.buyyouadrink.activity.login

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View

import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Button
import com.example.tylerwalker.buyyouadrink.R
import com.example.tylerwalker.buyyouadrink.R.drawable.user
import com.example.tylerwalker.buyyouadrink.activity.home.HomeScreen
import com.example.tylerwalker.buyyouadrink.activity.onboarding.OnBoarding
import com.example.tylerwalker.buyyouadrink.model.*
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

    @Inject lateinit var userRepository: UserRepository

    @Inject lateinit var localStorage: LocalStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_login)

        // Dagger 2 component
        App().getComponent(this).inject(this)


        sign_in_button.setOnClickListener { attemptLogin() }
        sign_up_button.findViewById<Button>(R.id.primary_button)?.let { it.text = "REGISTER"; it.setOnClickListener { transitionToSignUp(it) } }
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


    private fun handleLoginResponse(response: AuthResponse) {
        if (!response.status) {
            clearForms()
            return
        }

        response.user?.let {

            userRepository.getUser(it)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(UserRepositoryObserver())
        }

    }

    fun start() {
        var intent: Intent

        intent = if (localStorage.isFirstRun()) {
            localStorage.setFirstRun()
            Intent(this, OnBoarding::class.java)
        } else {
            Intent(this, HomeScreen::class.java)
        }

        intent = Intent(this, OnBoarding::class.java)

        startActivity(intent)
    }

    private fun transitionToSignUp(view: View) {
        val intent = Intent(this, SignUpActivity::class.java)
        startActivity(intent)
    }

    private fun clearForms() {
        password_text.setText("")
        username_text.setText("")
    }

    inner class LoginObserver: SingleObserver<AuthResponse> {
        override fun onSubscribe(d: Disposable) {
        }

        override fun onError(e: Throwable) {
            Log.d("LoginActivity", "onError: ${e.message}")
        }

        override fun onSuccess(t: AuthResponse) {
            Log.d("LoginActivity", "auth onSuccess: $t")
            handleLoginResponse(t)
        }
    }

    inner class UserRepositoryObserver: SingleObserver<UserResponse> {
        override fun onSubscribe(d: Disposable) {
        }

        override fun onError(e: Throwable) {
            Log.d("NETWORK", e.message)
        }

        override fun onSuccess(t: UserResponse) {
            Log.d("LoginActivity", "user onSuccess: $t")
            if (t.status) {
                localStorage.setCurrentUser(t.user)
                start()
            }
        }
    }

}

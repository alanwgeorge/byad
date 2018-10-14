package com.example.tylerwalker.buyyouadrink.activity.login

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.support.v7.app.AppCompatActivity

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.tylerwalker.buyyouadrink.R
import com.example.tylerwalker.buyyouadrink.R.id.password_text
import com.example.tylerwalker.buyyouadrink.R.id.username_text
import com.example.tylerwalker.buyyouadrink.activity.home.HomeScreen
import com.example.tylerwalker.buyyouadrink.activity.onboarding.OnBoarding
import com.example.tylerwalker.buyyouadrink.databinding.ActivityNewLoginBinding
import com.example.tylerwalker.buyyouadrink.model.*
import com.example.tylerwalker.buyyouadrink.module.App
import com.example.tylerwalker.buyyouadrink.service.AuthService
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

import kotlinx.android.synthetic.main.activity_new_login.*
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {
    private var compositeDisposable = CompositeDisposable()

    @Inject lateinit var authService: AuthService

    @Inject lateinit var userRepository: UserRepository

    @Inject lateinit var localStorage: LocalStorage

    @Inject
    lateinit var authEventsFlowable: Flowable<AuthEvent>

    @Inject
    lateinit var navigationEventsFlowable: Flowable<NavigationEvent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dagger 2 component
        val component = App().getComponent(this)
        component.inject(this)

        val binding: ActivityNewLoginBinding = DataBindingUtil.setContentView(this, R.layout.activity_new_login)
        binding.setLifecycleOwner(this)

        ViewModelProviders.of(this).get(LoginViewModel::class.java).apply {
            component.inject(this)
            binding.viewModel = this
            lifecycle.addObserver(this)

            localStorage.rememberMe()?.let { shouldRememberMe ->
                if (shouldRememberMe) {
                    localStorage.getSavedCredentials()?.let {
                        email.value = it.email
                        password.value = it.password
                    }
                }

                rememberMe.value = shouldRememberMe
            }

            email.observe(this@LoginActivity, Observer {
                Log.d("LoginActivity", "email: $it")
            })

            password.observe(this@LoginActivity, Observer {
                Log.d("LoginActivity", "password: $it")
            })

            isFormValid.observe(this@LoginActivity, Observer {
                Log.d("LoginActivity", "isFormValid(): $it")
            })

            rememberMe.observe(this@LoginActivity, Observer {
                Log.d("LoginActivity", "rememberMe: $it")
            })
        }

        setupRegisterButton()
    }

    override fun onStart() {
        super.onStart()

        compositeDisposable.add(observeForms())
        compositeDisposable.add(observeNavigateHome())
        compositeDisposable.add(observeNavigateOnBoarding())
    }

    private fun observeForms(): Disposable = authEventsFlowable
            .filter { it is AuthEvent.SignOn }
            .subscribe { clearForms() }

    private fun observeNavigateHome(): Disposable = navigationEventsFlowable
            .filter { it === NavigationEvent.Home }
            .subscribe { transitionToHome() }

    private fun observeNavigateOnBoarding(): Disposable = navigationEventsFlowable
            .filter { it === NavigationEvent.OnBoarding }
            .subscribe { transitionToOnBoarding() }

    private fun setupRegisterButton() = sign_up_button.findViewById<Button>(R.id.primary_button)?.let {
        it.text = getString(R.string.register)
        it.setOnClickListener { transitionToSignUp() }
    }

    private fun transitionToSignUp() = startActivity(Intent(this, SignUpActivity::class.java))
    private fun transitionToHome() = startActivity(Intent(this, HomeScreen::class.java))
    private fun transitionToOnBoarding() = startActivity(Intent(this, OnBoarding::class.java))

    private fun clearForms() {
        password_text.setText("")
        username_text.setText("")
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
        compositeDisposable = CompositeDisposable()
    }

}

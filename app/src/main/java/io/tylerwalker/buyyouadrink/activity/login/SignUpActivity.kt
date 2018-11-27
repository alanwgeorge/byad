package io.tylerwalker.buyyouadrink.activity.login

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.tylerwalker.buyyouadrink.R
import io.tylerwalker.buyyouadrink.activity.home.HomeScreen
import io.tylerwalker.buyyouadrink.activity.onboarding.OnBoarding
import io.tylerwalker.buyyouadrink.model.AuthEvent
import io.tylerwalker.buyyouadrink.model.LocalStorage
import io.tylerwalker.buyyouadrink.module.App
import javax.inject.Inject
import io.tylerwalker.buyyouadrink.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    @Inject
    lateinit var authEventsFlowable: Flowable<AuthEvent>

    @Inject
    lateinit var localStorage: LocalStorage

    private var trash = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dagger 2 component
        val component = App().getComponent(this)
        component.inject(this)

        val binding: ActivitySignUpBinding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up)
        binding.setLifecycleOwner(this)

        ViewModelProviders.of(this).get(SignUpViewModel::class.java).apply {
            component.inject(this)
            binding.viewmodel = this
            lifecycle.addObserver(this)

            name.value = ""
            email.value = ""
            password.value = ""
            confirm.value = ""
        }

        trash.add(authEventsFlowable
                .filter { it === AuthEvent.RegisterSuccess }
                .doOnNext { start() }
                .subscribe())
    }

    private fun start() {
        var intent: Intent?

        if (localStorage.isFirstRun()) {
            intent = Intent(this, OnBoarding::class.java)
        } else {
            intent = Intent(this, HomeScreen::class.java)
        }

        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        trash.clear()
        trash = CompositeDisposable()
    }
}

package com.example.tylerwalker.buyyouadrink.activity.login

import android.app.Application
import android.arch.lifecycle.*
import android.content.Intent
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.tylerwalker.buyyouadrink.activity.home.HomeScreen
import com.example.tylerwalker.buyyouadrink.activity.onboarding.OnBoarding
import com.example.tylerwalker.buyyouadrink.model.*
import com.example.tylerwalker.buyyouadrink.service.AuthService
import io.reactivex.Flowable
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class SignUpViewModel(app: Application): AndroidViewModel(app), LifecycleObserver {
    companion object {
        private const val logTag = "SignUpViewModel"
    }
    @Inject
    lateinit var authEventsProcessor: PublishProcessor<AuthEvent>

    @Inject
    lateinit var authEventsFlowable: Flowable<AuthEvent>

    @Inject
    lateinit var authService: AuthService

    private var trash = CompositeDisposable()

    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val name = MutableLiveData<String>()
    val confirm = MutableLiveData<String>()

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        trash.add(observeRegisterEvents())
    }

    fun register(view: View) {
        authEventsProcessor.onNext(AuthEvent.Register)
    }

    private fun verifyInput(): Boolean {

        if (email.value == null || name.value == null || password.value == null || confirm.value == null) {
            Toast.makeText(getApplication(), "All fields are required.", Toast.LENGTH_LONG).show()
            return false
        }

        if (name.value!!.length < 4) {
            Toast.makeText(getApplication(), "Name must be at least 4 characters.", Toast.LENGTH_LONG).show()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email.value).matches()) {
            Toast.makeText(getApplication(), "Must be a valid email address.", Toast.LENGTH_LONG).show()
            return false
        }

        if (password.value!!.length < 8) {
            Toast.makeText(getApplication(), "Password must be at least 8 characters.", Toast.LENGTH_LONG).show()
            return false
        }

        if (password.value!! != confirm.value!!) {
            Toast.makeText(getApplication(), "Confirm password did not match.", Toast.LENGTH_LONG).show()
            return false
        }

        return true
    }

    private fun observeRegisterEvents(): Disposable = authEventsFlowable
            .filter { it === AuthEvent.Register }
            .doOnNext { Log.d(logTag, "AuthEvent: Register") }
            .flatMap {
                if (verifyInput()) {
                    Flowable.just(Credentials(email.value!!, password.value!!))
                } else {
                    Flowable.empty()
                }
            }
            .doOnNext { Toast.makeText(getApplication(), "Registering...", Toast.LENGTH_SHORT).show() }
            .flatMap {
                authService.register(it)
                        .toFlowable()
            }
            .subscribe({
                if (!it.status) {
                    Toast.makeText(getApplication(), it.error?.localizedMessage ?: "Something went wrong...", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(getApplication(), "Register success!", Toast.LENGTH_SHORT).show()
                    authEventsProcessor.onNext(AuthEvent.RegisterSuccess)
                }
            }, {
                Log.d(logTag, it.localizedMessage)
                Toast.makeText(getApplication(), "Something went wrong...", Toast.LENGTH_LONG).show()
            })

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        trash.clear()
        trash = CompositeDisposable()
    }

}


package io.tylerwalker.buyyouadrink.activity.login

import android.app.Application
import android.arch.lifecycle.*
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.tylerwalker.buyyouadrink.activity.map.InvitationViewModel.Companion.logTag
import io.tylerwalker.buyyouadrink.model.*
import io.tylerwalker.buyyouadrink.service.AuthService
import java.util.NoSuchElementException
import javax.inject.Inject

class LoginViewModel(app: Application): AndroidViewModel(app), LifecycleObserver {
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val isFormValid = MediatorLiveData<Boolean>().apply {
        addSource(email) { postValue(validateEmailAddress(it)) }
        addSource(password) { postValue(validatePassword(it)) }
    }
    val rememberMe = MutableLiveData<Boolean>()

    private var compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var localStorage: LocalStorage

    @Inject
    lateinit var authService: AuthService

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var authEventsProcessor: PublishProcessor<AuthEvent>

    @Inject
    lateinit var authEventsFlowable: Flowable<AuthEvent>

    @Inject
    lateinit var navigationEventsProcessor: PublishProcessor<NavigationEvent>

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        compositeDisposable.add(createAuthFlowDisposable())
        compositeDisposable.add(getUserDisposable())
        compositeDisposable.add(createUserDisposable())
    }

    private fun createAuthFlowDisposable(): Disposable = authEventsFlowable
            .filter { it is AuthEvent.SignOn }
            .doOnNext { Log.d("LoginViewModel", "Auth event: $it") }
            .map { it as AuthEvent.SignOn }
            .map { getCredentials(it) }
            .map { rememberCredentials(it) }
            .flatMap { attemptSignOn(it) }
            .subscribe({
                Log.d("LoginViewModel", "Auth Response: $it")
                if (it.status) {
                    Toast.makeText(this.getApplication(), "Login Success.", Toast.LENGTH_SHORT).show()

                    rememberMe.value?.let {
                        if (!it) localStorage.clearCredentials()
                    }

                    publishAuthEvent(AuthEvent.SignOnSuccess(uid = it.uid!!))
                } else {
                    if (it.error != null) {
                        throw it.error
                    } else {
                        localStorage.clearCredentials()
                        Toast.makeText(this.getApplication(), "Something went wrong...", Toast.LENGTH_SHORT).show()
                    }
                }
            }, {
                localStorage.clearCredentials()

                if (it is FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(this.getApplication(), "The credentials you entered are indicated as invalid.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this.getApplication(), "Something went wrong...", Toast.LENGTH_SHORT).show()
                }
            })

    private fun getUserDisposable(): Disposable = authEventsFlowable
            .filter { it is AuthEvent.SignOnSuccess }
            .doOnNext { Log.d("LoginViewModel", "Auth Event: $it") }
            .map { it as AuthEvent.SignOnSuccess }
            .map { rememberUid(it.uid!!) }
            .flatMap { getUser(it) }
            .doOnNext {  Log.d("LoginViewModel", "getUser(): $it") }
            .subscribe({
                if (it.status) {
                    it.user?.let {
                        localStorage.putCurrentUser(it)

                        if (localStorage.isFirstRun()) {
                            publishNavigationEvent(NavigationEvent.OnBoarding)
                        } else {
                            publishNavigationEvent(NavigationEvent.Home)
                        }

                    } ?: publishAuthEvent(AuthEvent.FirstTimeUserSetup)
                } else {
                    Toast.makeText(this.getApplication(), "Something went wrong...", Toast.LENGTH_SHORT).show()
                }
            }, {
                if (it is NoSuchElementException) {
                    publishAuthEvent(AuthEvent.FirstTimeUserSetup)
                } else {
                    Toast.makeText(this.getApplication(), "Something went wrong...", Toast.LENGTH_SHORT).show()
                }
            })


    private fun createUserDisposable(): Disposable = authEventsFlowable
            .filter { it === AuthEvent.FirstTimeUserSetup }
            .map { it as AuthEvent.FirstTimeUserSetup }
            .doOnNext { Log.d("LoginViewModel", it.toString()) }
            .flatMap { createUser() }
            .doOnNext { Log.d("LoginViewModel", "create user response: $it") }
            .subscribe({
                if (it.error != null) throw it.error

                it.user?.let {
                    localStorage.putCurrentUser(it)
                    publishNavigationEvent(NavigationEvent.OnBoarding)
                } ?: localStorage.clearCurrentUser()
            }, {
                Log.d("LoginViewModel", "create user error: $it")
                Toast.makeText(this.getApplication(), "Something went wrong...", Toast.LENGTH_SHORT).show()
            })



    private fun rememberUid(uid: String): String = uid.apply { localStorage.putCurrentUid(uid) }

    private fun getCredentials(authEvent: AuthEvent.SignOn): Credentials {
        authEvent.credentials?.let {
            return it
        }

        throw Exception("Bad credentials")
    }

    private fun rememberCredentials(credentials: Credentials): Credentials = credentials.apply {
        rememberMe.value?.let {
            if (it) {
                localStorage.shouldRememberMe(true)
            } else {
                localStorage.shouldRememberMe(false)
            }
        }

        localStorage.putCredentials(this)
    }

    private fun attemptSignOn(credentials: Credentials): Flowable<AuthResponse> = authService.login(credentials).toFlowable()

    private fun getUser(uid: String?): Flowable<UserResponse> {
        if (uid == null) return Flowable.error(Exception("UID was null"))
        return userRepository.getUser(uid)
    }

    private fun createUser(): Flowable<UserResponse> {
        val currentUid = localStorage.getCurrentUid()
        val credentials = localStorage.getSavedCredentials()

        if (currentUid == null) return Flowable.error(Exception("Bad UID"))
        if (credentials == null) return Flowable.error(Exception("Bad credentials"))

        return userRepository.createUser(credentials.email, currentUid)
    }


    private fun validateEmailAddress(email: String?): Boolean = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun validatePassword(password: String?): Boolean = password != null && password.length >= 8

    fun publishAuthEvent(event: AuthEvent) = authEventsProcessor.onNext(event)

    fun publishNavigationEvent(event: NavigationEvent) = navigationEventsProcessor.onNext(event)

    /*
    * Databinding function
    * */
    fun attemptLogin(view: View) {
        isFormValid.value?.let {
            if (it) {
                email.value?.let { email ->
                    password.value?.let { password ->
                        Toast.makeText(this.getApplication(), "Logging in...", Toast.LENGTH_SHORT).show()
                        publishAuthEvent(AuthEvent.SignOn(Credentials(email.trim(), password.trim())))
                    }
                }
            } else {
                email.value?.let { email ->
                    password.value?.let { password ->
                        if (!validateEmailAddress(email)) {
                            Toast.makeText(this.getApplication(), "Should be a valid email address.", Toast.LENGTH_SHORT).show()
                        }

                        if (!validatePassword(password)) {
                            Toast.makeText(this.getApplication(), "Password must be at least 8 characters.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        compositeDisposable.clear()
        compositeDisposable = CompositeDisposable()
    }
}



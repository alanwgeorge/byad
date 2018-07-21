package com.example.tylerwalker.buyyouadrink

import com.example.tylerwalker.buyyouadrink.model.AuthResponse
import com.example.tylerwalker.buyyouadrink.service.AuthService
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.junit.Rule
import org.junit.Test
import org.mockito.InjectMocks
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import java.util.concurrent.TimeUnit

class AuthServiceUnitTest {
    @Rule
    @JvmField
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @InjectMocks
    lateinit var authService: AuthService

    @Test
    fun testLoginFun() {
        val username = "johndoe@email.com"
        val password = "123password"
        val res = authService.login(username, password)
        assert (res is Single<AuthResponse>)

        res
                .subscribeOn(Schedulers.single())
                .test()
                .awaitDone(2, TimeUnit.SECONDS)
                .assertComplete()
                .assertOf {
                    val auth = it.values()[0]
                    assert(auth.status)
                }
    }
}
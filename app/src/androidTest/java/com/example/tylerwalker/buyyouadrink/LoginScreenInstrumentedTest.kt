package com.example.tylerwalker.buyyouadrink

import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.typeText
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.ViewMatchers.withId
import com.example.tylerwalker.buyyouadrink.activity.home.HomeScreen
import com.example.tylerwalker.buyyouadrink.activity.login.LoginActivity

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginScreenInstrumentedTest {
    @Rule
    @JvmField
    val testRule: IntentsTestRule<LoginActivity> = IntentsTestRule(LoginActivity::class.java)

    @Test
    fun shouldLogin() {
        onView(withId(R.id.email)).perform(typeText("johndoe@email.com"))
        onView(withId(R.id.password)).perform(typeText("password"))
        onView(withId(R.id.sign_in_button)).perform(click())

        Thread.sleep(2000)

        intended(hasComponent(HomeScreen::class.qualifiedName))
    }
}

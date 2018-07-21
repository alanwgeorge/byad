package com.example.tylerwalker.buyyouadrink

import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.espresso.matcher.ViewMatchers.withId
import com.example.tylerwalker.buyyouadrink.activity.login.LoginActivity
import com.example.tylerwalker.buyyouadrink.activity.splash.SplashScreen

@RunWith(AndroidJUnit4::class)
@LargeTest
class SplashScreenInstrumentedTest {
    @Rule
    @JvmField
    val testRule: IntentsTestRule<SplashScreen> = IntentsTestRule(SplashScreen::class.java)

    @Test
    fun shouldTransitionToLoginScreen() {
        onView(withId(R.id.start_button)).perform(click())

        intended(hasComponent(LoginActivity::class.qualifiedName))
    }
}

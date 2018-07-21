package com.example.tylerwalker.buyyouadrink

import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import org.junit.Rule
import org.junit.runner.RunWith
import android.support.test.espresso.intent.rule.IntentsTestRule
import com.example.tylerwalker.buyyouadrink.activity.home.HomeScreen

@RunWith(AndroidJUnit4::class)
@LargeTest
class HomeScreenInstrumentedTest {
    @Rule
    @JvmField
    val testRule: IntentsTestRule<HomeScreen> = IntentsTestRule(HomeScreen::class.java)

}

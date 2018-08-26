package com.example.tylerwalker.buyyouadrink.activity.onboarding

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import com.example.tylerwalker.buyyouadrink.R
import com.example.tylerwalker.buyyouadrink.activity.home.HomeScreen
import com.example.tylerwalker.buyyouadrink.activity.profile.SetupProfileActivity

class OnBoarding : AppCompatActivity() {

    private companion object {
        @JvmStatic
        private val NUM_PAGES = 3
    }

    private lateinit var pager: ViewPager
    private lateinit var adapter: PagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_on_boarding)

        pager = findViewById(R.id.onboarding_pager)
        adapter = OnBoardingPagerAdapter(supportFragmentManager)
        pager.adapter = adapter
    }

    fun handleNext(view: View) =
            when(pager.currentItem) {
                0 -> pager.setCurrentItem(1)
                1 -> pager.setCurrentItem(2)
                else -> transitionToSetupProfile(view)
            }

    fun transitionToHome(view: View) {
        val intent = Intent(this, HomeScreen::class.java)
        startActivity(intent)
    }

    fun transitionToSetupProfile(view: View) {
        val intent = Intent(this, SetupProfileActivity::class.java)
        startActivity(intent)
    }

    inner class OnBoardingPagerAdapter(fragmentManager: FragmentManager): FragmentStatePagerAdapter(fragmentManager) {
        override fun getItem(index: Int): Fragment =
                when (index) {
                    0 -> OnBoarding1()
                    1 -> OnBoarding2()
                    2 -> OnBoarding3()
                    else -> OnBoarding1()
                }

        override fun getCount(): Int {
            return NUM_PAGES
        }
    }
}

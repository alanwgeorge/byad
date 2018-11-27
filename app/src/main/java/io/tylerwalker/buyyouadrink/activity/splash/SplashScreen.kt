package io.tylerwalker.buyyouadrink.activity.splash

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import io.tylerwalker.buyyouadrink.activity.login.LoginActivity

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

}

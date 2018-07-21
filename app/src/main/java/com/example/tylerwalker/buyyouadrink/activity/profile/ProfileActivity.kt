package com.example.tylerwalker.buyyouadrink.activity.profile

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.example.tylerwalker.buyyouadrink.R
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {
    var user_id: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        user_id = intent.extras.getInt("user_id")
        user_id_temp.text = user_id.toString()
    }
}

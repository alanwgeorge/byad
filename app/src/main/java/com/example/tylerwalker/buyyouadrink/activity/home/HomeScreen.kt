package com.example.tylerwalker.buyyouadrink.activity.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.example.tylerwalker.buyyouadrink.R
import com.example.tylerwalker.buyyouadrink.activity.Settings.SettingsActivity
import com.example.tylerwalker.buyyouadrink.activity.messages.MessagesActivity
import com.example.tylerwalker.buyyouadrink.activity.profile.ProfileActivity
import com.example.tylerwalker.buyyouadrink.activity.profile.SetupProfileActivity
import com.example.tylerwalker.buyyouadrink.module.App
import com.example.tylerwalker.buyyouadrink.model.Coordinates
import com.example.tylerwalker.buyyouadrink.model.User
import com.example.tylerwalker.buyyouadrink.service.LocationService
import javax.inject.Inject

class HomeScreen : AppCompatActivity() {

    @Inject lateinit var locationService: LocationService

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    val presenter: HomePresenter = HomePresenter()

    val image_url = "https://bloximages.chicago2.vip.townnews.com/willmarradio.com/content/tncms/assets/v3/editorial/8/73/873d38ba-8bf1-11e7-80ce-93f5c9c5517d/59a41515a69cd.image.jpg"
    val image_url_2 = "https://res.cloudinary.com/wells-fargo/image/upload/v1531196082/selfie_400_400.jpg"

    val testUsers = arrayOf(
            User("1", Coordinates(37.8716F, -122.2727F), "I like coffee.", "Tyler Walker", "abc@123.com", "1234567890", "Yo", "Coffee", image_url, ""),
            User("2", Coordinates(37.7749F, 122.4194F), "Let's have some cocktails tonight!", "Kelsi Yuan", "1234567890", "Hi", "BubbleTea", "BubbleTea", image_url_2))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()

        setContentView(R.layout.activity_home)

        App().getComponent(this).inject(this)

        setupPresenter()
        setupRecyclerView()
        setupToolbar()

    }

    private fun setupPresenter() {
        presenter.activity = this
        presenter.locationService = locationService
    }

    private fun setupRecyclerView() {
        viewManager = LinearLayoutManager(this)
        viewAdapter = Adapter(testUsers, this)

        recyclerView = findViewById<RecyclerView>(R.id.recycler).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    private fun setupToolbar() {
        val settingsImage = findViewById<ImageView>(R.id.toolbar_image_left)
        val messagesImage = findViewById<ImageView>(R.id.toolbar_image_right)

        settingsImage.setOnClickListener {
            transitionToSettings(it)
        }

        messagesImage.setOnClickListener {
            transitionToMessages(it)
        }
    }

    fun transitionToProfile(user_id: String) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("user_id", user_id)
        startActivity(intent)
    }

    fun transitionToSettings(view: View) {
        val intent = Intent(this, SetupProfileActivity::class.java)
        startActivity(intent)
    }

    fun transitionToMessages(view: View) {
        val intent = Intent(this, MessagesActivity::class.java)
        startActivity(intent)
    }

    fun needsPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
    }

    fun promptForPermissions(): Unit {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Log.d("RESULT", "Do something about location")
                }
            }
        }
    }
}

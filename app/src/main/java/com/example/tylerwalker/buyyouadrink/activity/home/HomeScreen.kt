package com.example.tylerwalker.buyyouadrink.activity.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.example.tylerwalker.buyyouadrink.R
import com.example.tylerwalker.buyyouadrink.activity.Settings.SettingsActivity
import com.example.tylerwalker.buyyouadrink.activity.profile.ProfileActivity
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
            User(1,"Tyler", "Walker", Coordinates(0F, 0F), "I like to drink coffee.", image_url = image_url),
            User(2,"Kelsi", "Yuan", Coordinates(1F, 1F), "Let's have some cocktails tonight!", image_url = image_url_2))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        App().getComponent().inject(this)

        setupPresenter()
        setupRecyclerView()

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

    fun transitionToProfile(user_id: Int) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("user_id", user_id)
        startActivity(intent)
    }

    fun transitionToSettings(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
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

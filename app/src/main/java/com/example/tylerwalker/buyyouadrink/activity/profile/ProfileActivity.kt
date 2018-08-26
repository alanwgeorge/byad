package com.example.tylerwalker.buyyouadrink.activity.profile

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.tylerwalker.buyyouadrink.R
import com.example.tylerwalker.buyyouadrink.activity.map.MapActivity
import com.example.tylerwalker.buyyouadrink.model.Coordinates
import com.example.tylerwalker.buyyouadrink.model.User
import com.example.tylerwalker.buyyouadrink.module.App
import com.example.tylerwalker.buyyouadrink.service.LocationService
import java.io.InputStream
import javax.inject.Inject

class ProfileActivity : AppCompatActivity() {
    @Inject
    lateinit var locationService: LocationService

    var user_id: String = "0"
    var user: User? = null

    val mock_image_url = "https://bloximages.chicago2.vip.townnews.com/willmarradio.com/content/tncms/assets/v3/editorial/8/73/873d38ba-8bf1-11e7-80ce-93f5c9c5517d/59a41515a69cd.image.jpg"
    val mock_image_url_2 = "https://res.cloudinary.com/wells-fargo/image/upload/v1531196082/selfie_400_400.jpg"

    val users = arrayOf(User("1", "Tyler", "Walker", Coordinates(37.8716F, -122.2727F), "Beer", "Drinking beer is the best", mock_image_url),
            User("2", "Kelsi", "Yuan", Coordinates(37.7749F, -122.4194F), "Tea", "Tea is the best!", mock_image_url_2))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        App().getComponent(this).inject(this)

        val context: Context = this
        user_id = intent.extras.getString("user_id")
        user = users.find { it.user_id == user_id }

        user?.apply {
            val profile_image_view = findViewById<ImageView>(R.id.profile_image)
            val profile_name_text_view = findViewById<TextView>(R.id.profile_name_text)
            val profile_location_text_view = findViewById<TextView>(R.id.profile_location_text)
            val profile_caption_text_view = findViewById<TextView>(R.id.profile_caption_text)

            image_url?.let {
                ImageLoader(profile_image_view).execute(it)
            }

            profile_name_text_view.text = "$first_name $last_name"
            profile_caption_text_view.text = caption

            val location = locationService.getLocationName(context, location)
            location?.let {
                profile_location_text_view.text = it
            }
        }
    }


fun transitionToMap(view: View) {
    val intent = Intent(this, MapActivity::class.java)
    intent.putExtra("user", user)
    startActivity(intent)
}








    inner class ImageLoader(val image: ImageView): AsyncTask<String, Unit, Bitmap>() {

        override fun doInBackground(vararg urls: String): Bitmap {
            var bitmap: Bitmap? = null
            try {
                val inputStream: InputStream = java.net.URL(urls[0]).openStream()
                bitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                Log.e("ImageLoader()", e.message)
                e.printStackTrace()
            }
            return bitmap!!
        }

        override fun onPostExecute(result: Bitmap?) {
            val roundDrawable = RoundedBitmapDrawableFactory.create(resources, result)
            roundDrawable.isCircular = true
            image.setImageDrawable(roundDrawable)
            image.adjustViewBounds = true
            image.scaleType = ImageView.ScaleType.FIT_CENTER
        }
    }
}

package com.example.tylerwalker.buyyouadrink.activity.messages

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.tylerwalker.buyyouadrink.R
import com.example.tylerwalker.buyyouadrink.activity.Settings.SettingsActivity
import com.example.tylerwalker.buyyouadrink.activity.home.HomeScreen
import com.example.tylerwalker.buyyouadrink.model.Conversation
import java.util.*

class MessagesActivity : AppCompatActivity() {

    val image_url = "https://bloximages.chicago2.vip.townnews.com/willmarradio.com/content/tncms/assets/v3/editorial/8/73/873d38ba-8bf1-11e7-80ce-93f5c9c5517d/59a41515a69cd.image.jpg"
    val image_url_2 = "https://res.cloudinary.com/wells-fargo/image/upload/v1531196082/selfie_400_400.jpg"

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    val testConversations = arrayOf(
            Conversation(1, "Tyler", "Walker", Date(), "Hello", image_url),
            Conversation(2, "Kelsi", "Kelsi", Date(), "Hey this is K", image_url_2)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.hide()

        setContentView(R.layout.activity_messages)

        setupRecyclerView()
        setupToolbar()
    }

    private fun setupRecyclerView() {
        viewManager = LinearLayoutManager(this)
        viewAdapter = Adapter(testConversations, this)

        recyclerView = findViewById<RecyclerView>(R.id.recycler).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    private fun setupToolbar() {
        val settingsImage = findViewById<ImageView>(R.id.toolbar_image_left)
        val messagesImage = findViewById<ImageView>(R.id.toolbar_image_right)
        val title = findViewById<TextView>(R.id.toolbar_text)

        title.text = "Messages"

        val homeDrawable = BitmapFactory.decodeResource(resources, R.drawable.main_logo)
        messagesImage.setImageBitmap(homeDrawable)
        messagesImage.adjustViewBounds = true
        messagesImage.scaleType = ImageView.ScaleType.FIT_CENTER

        settingsImage.setOnClickListener {
            transitionToSettings(it)
        }

        messagesImage.setOnClickListener {
            transitionToHome(it)
        }
    }


    private fun transitionToSettings(view: View) {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    private fun transitionToHome(view: View) {
        val intent = Intent(this, HomeScreen::class.java)
        startActivity(intent)
    }

}

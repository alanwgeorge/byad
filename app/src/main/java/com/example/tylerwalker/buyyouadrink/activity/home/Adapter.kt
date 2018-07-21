package com.example.tylerwalker.buyyouadrink.activity.home

import android.app.Activity
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.support.constraint.ConstraintLayout
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.tylerwalker.buyyouadrink.R
import com.example.tylerwalker.buyyouadrink.R.id.textView
import com.example.tylerwalker.buyyouadrink.model.User
import java.io.InputStream

class Adapter(private val data: Array<User>, val activity: HomeScreen): RecyclerView.Adapter<Adapter.ViewHolder>() {
    class ViewHolder(val element: ConstraintLayout): RecyclerView.ViewHolder(element)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val element = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_element, parent, false) as ConstraintLayout
        return ViewHolder(element)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val nameTextView = holder.element.findViewById<TextView>(R.id.recycler_element_name_text)
        val captionTextView = holder.element.findViewById<TextView>(R.id.recycler_element_caption_text)
        val imageView = holder.element.findViewById<ImageView>(R.id.recycler_element_image)
        val button = holder.element.findViewById<Button>(R.id.recycler_element_button)
        val user = data[position]
        nameTextView.text = "${user.first_name} ${user.last_name}"
        captionTextView.text = "${user.caption}"

        if (user.image_url != null) {
            ImageLoader(imageView).execute(user.image_url)
        }

        button.setOnClickListener({
            activity.transitionToProfile(data[position].user_id)
        })
    }

    override fun getItemCount(): Int = data.size

    inner class ImageLoader(val image: ImageView): AsyncTask<String, Unit, Bitmap>() {

        override fun doInBackground(vararg urls: String): Bitmap {
            var bitmap: Bitmap? = null
            try {
                val inputStream: InputStream = java.net.URL(urls[0]).openStream()
                bitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: Exception) {
                Log.e("ERROR", e.message)
                e.printStackTrace()
            }
            return bitmap!!
        }

        override fun onPostExecute(result: Bitmap?) {
            val roundDrawable = RoundedBitmapDrawableFactory.create(activity.resources, result)
            roundDrawable.isCircular = true
            image.setImageDrawable(roundDrawable)
            image.adjustViewBounds = true
            image.scaleType = ImageView.ScaleType.FIT_CENTER
        }
    }
}
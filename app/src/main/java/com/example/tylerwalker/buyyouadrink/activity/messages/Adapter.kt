package com.example.tylerwalker.buyyouadrink.activity.messages

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.support.constraint.ConstraintLayout
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.tylerwalker.buyyouadrink.R
import com.example.tylerwalker.buyyouadrink.R.drawable.user
import com.example.tylerwalker.buyyouadrink.model.Conversation
import com.example.tylerwalker.buyyouadrink.util.toBitmap
import com.example.tylerwalker.buyyouadrink.util.toRounded
import kotlinx.android.synthetic.main.recycler_element.view.*
import java.io.InputStream

class Adapter(val activity: MessagesActivity): RecyclerView.Adapter<Adapter.ViewHolder>() {
    private var conversations: List<Conversation> = ArrayList()

    class ViewHolder(val element: ConstraintLayout): RecyclerView.ViewHolder(element)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val element = LayoutInflater.from(parent.context)
                .inflate(R.layout.recycler_element, parent, false) as ConstraintLayout
        return ViewHolder(element)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with (holder.element) {
            recycler_element_name_text.text = conversations[position].with
            recycler_element_caption_text.text = conversations[position].placeName

            val drinkDrawable: Drawable? = when (conversations[position].beverageType) {
                "BubbleTea" -> { activity.getDrawable(R.drawable.ic_bubble_tea) }
                "Beer" -> { activity.getDrawable(R.drawable.ic_beer) }
                "Juice" -> { activity.getDrawable(R.drawable.ic_juice) }
                else -> { activity.getDrawable(R.drawable.ic_coffee) }
            }

            conversations[position].withImage.let {
                if (it.isEmpty()) {
                    drinkDrawable?.let { drink -> recycler_element_image.setImageDrawable(drink) }
                } else {
                    recycler_element_image.setImageBitmap(it.toBitmap()?.toRounded())
                }
            }


            recycler_element_button.setOnClickListener {
                activity.transitionToConversation(conversations[position])
            }
        }
    }

    override fun getItemCount(): Int = conversations.size

    fun updateMessages(conversations: List<Conversation>) {
        this.conversations = conversations
        notifyDataSetChanged()
    }
}
package io.tylerwalker.chat.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import android.widget.FrameLayout
import io.tylerwalker.chat.R


/**
 * MessageView is used to help support custom views without defining a custom viewholder,
 * any view that implements this interface is guaranteed to work with the chat-ui.
 *
 *
 * Created by James Lendrem
 */

abstract class MessageView : FrameLayout {

    private var senderTextView: TextView? = null

    /**
     * Method to set the messages text in the view so it can be displayed on the screen.
     *
     * @param message The message that you want to be displayed.
     */
    abstract fun setMessage(message: String)

    /**
     * Method to set the timestamp that the message was received or sent on the screen.
     *
     * @param timestamp The timestamp that you want to be displayed.
     */
    abstract fun setTimestamp(timestamp: String)

    /**
     * Method to set the background color that you want to use in your message.
     *
     * @param background The background that you want to be displayed.
     */
    abstract fun setBackground(background: Int)

    /**
     * Method to set the elevation of the view.
     *
     * @param elevation The elevation that you want the view to be displayed at.
     */
    abstract override fun setElevation(elevation: Float)


    /**
     * Method to set the message's sender name.
     *
     * @param sender The name of the sender to be displayed.
     * The view is only visible if the sender is set
     */
    fun setSender(sender: String) {
        if (senderTextView == null) {
            this.senderTextView = findViewById(R.id.sender_text_view)
        }

        senderTextView!!.visibility = View.VISIBLE
        senderTextView!!.text = sender
    }

    /**
     * Constructs a new message view.
     *
     * @param context
     */
    constructor(context: Context) : super(context) {

    }

    /**
     * Constructs a new message view with attributes, this constructor is used when we create a
     * message view using XML.
     *
     * @param context
     * @param attrs
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

    }

}
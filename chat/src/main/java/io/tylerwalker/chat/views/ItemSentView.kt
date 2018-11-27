package io.tylerwalker.chat.views

import android.content.Context
import android.widget.TextView
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.support.annotation.ColorInt
import android.util.AttributeSet
import io.tylerwalker.chat.R


/**
 * View to display the messages that have been sent through the chat-ui.
 *
 * Created by James Lendrem
 */
class ItemSentView : MessageView {

    private var bubble: CardView? = null
    private var messageTextView: TextView? = null
    private var timestampTextView: TextView? = null


    /**
     * Method to set the messages text in the view so it can be displayed on the screen.
     * @param message   The message that you want to be displayed.
     */
    override fun setMessage(message: String) {

        if (messageTextView == null) {

            messageTextView = findViewById(R.id.message_text_view)

        }

        messageTextView!!.text = message


    }

    /**
     * Method to set the timestamp that the message was received or sent on the screen.
     * @param timestamp The timestamp that you want to be displayed.
     */
    override fun setTimestamp(timestamp: String) {

        if (timestampTextView == null) {

            timestampTextView = findViewById(R.id.timestamp_text_view)

        }

        timestampTextView!!.text = timestamp

    }

    /**
     * Method to set the background color that you want to use in your message.
     * @param background The background that you want to be displayed.
     */
    override fun setBackground(@ColorInt background: Int) {

        if (bubble == null) {

            this.bubble = findViewById(R.id.bubble)

        }

        bubble!!.setCardBackgroundColor(background)

    }

    /**
     * Method to set the elevation of the view.
     * @param elevation The elevation that you want the view to be displayed at.
     */
    override fun setElevation(elevation: Float) {

        if (bubble == null) {

            this.bubble = findViewById(R.id.bubble)

        }

        bubble!!.cardElevation = elevation

    }

    /**
     * Constructs a new message view.
     * @param context
     */
    constructor(context: Context) : super(context) {
        initializeView(context)

    }

    /**
     * Constructs a new message view with attributes, this constructor is used when we create a
     * message view using XML.
     * @param context
     * @param attrs
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initializeView(context)

    }

    /**
     * Inflates the view so it can be displayed and grabs any child views that we may require
     * later on.
     * @param context   The context that is used to inflate the view.
     */
    private fun initializeView(context: Context) {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.chat_item_sent, this)

        this.bubble = findViewById(R.id.bubble)
        this.messageTextView = findViewById(R.id.message_text_view)
        this.timestampTextView = findViewById(R.id.timestamp_text_view)

    }

}
package io.tylerwalker.chat.views

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.support.annotation.ColorInt
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import io.tylerwalker.chat.R
import io.tylerwalker.chat.models.ChatMessageType
import kotlinx.android.synthetic.main.chat_item_invitation.view.*

/**
 * Display the initial Invitation Message
 *
 * Created by Tyler Walker
 */

class ItemInvitationView: MessageView {

    /**
     * Method to set the messages text in the view so it can be displayed on the screen.
     * @param message   The message that you want to be displayed.
     */
    override fun setMessage(message: String) {
        message_text_view.text = message
    }

    /**
     * Method to set the timestamp that the message was received or sent on the screen.
     * @param timestamp The timestamp that you want to be displayed.
     */
    override fun setTimestamp(timestamp: String) {
        timestamp_text_view.text = timestamp
    }

    /**
     * Method to set the background color that you want to use in your message.
     * @param background The background that you want to be displayed.
     */
    override fun setBackground(@ColorInt background: Int) {
        bubble.setCardBackgroundColor(background)
    }

    /**
     * Method to set the elevation of the view.
     * @param elevation The elevation that you want the view to be displayed at.
     */
    override fun setElevation(elevation: Float) {
        bubble.cardElevation = elevation
    }

    /**
     * Method for setting the click handler to perform action when Accept button clicked
     * @param action A lambda
     */
    fun setAcceptAction(action: () -> Unit) {
        acceptButton.setOnClickListener { action() }
    }

    /**
     * Method for setting the click handler to perform action when Reject button clicked
     * @param action A lambda
     */
    fun setRejectAction(action: () -> Unit) {
        rejectButton.setOnClickListener { action() }
    }

    fun setFrom(text: String) {
        sender_text_view.text = text
    }

    fun setDrinkType(text: String) {
        what_text_view.text = text
    }

    fun setLocationName(text: String) {
        where_text_view.text = text
    }

    fun setLocationAddress(text: String) {
        address_text_view.text = text
    }


    /**
     * Constructs a new message view.
     * @param context
     */
    constructor(context: Context, invitation: ChatMessageType.Invitation) : super(context) {
        initializeView(context)
        with (invitation) {
            setFrom(from)
            setDrinkType(what)
            setLocationName(where)
            setLocationAddress(whereAddress)
            setAcceptAction(acceptAction)
            setRejectAction(rejectAction)
            address_text_view.setOnClickListener { startGoogleMaps(context, fromAddress, whereAddress) }

            if (isInitiatedByMe) {
                findViewById<ConstraintLayout>(R.id.buttonLayout)?.let {
                    it.visibility = View.GONE
                }
            }

            if (isAccepted) {
                findViewById<ConstraintLayout>(R.id.buttonLayout)?.let {
                    it.visibility = View.GONE
                }

                findViewById<TextView>(R.id.confirmationText)?.let {
                    it.visibility = View.VISIBLE

                    if (isInitiatedByMe) {
                        it.text = "They accepted your invitation!"
                    } else {
                        it.text = "You accepted this invitation!"
                    }
                }
            }
        }

    }

    private fun startGoogleMaps(context: Context, fromAddress: String, toAddress: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?saddr=$fromAddress&daddr=$toAddress"))
        context.startActivity(intent)
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
     * Inflate the view
     * @param context   The context that is used to inflate the view.
     */
    private fun initializeView(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.chat_item_invitation, this)
    }

}
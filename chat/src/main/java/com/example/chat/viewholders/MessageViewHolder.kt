package com.example.chat.viewholders

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewCompat.setElevation
import android.view.View
import com.example.chat.R
import com.example.chat.views.ItemInvitationView
import com.example.chat.views.MessageView


/**
 * View Holder for the Chat UI. Interfaces with the Received and Sent views and sets them up
 * with any messages required.
 *
 *
 * Original Code by Timi
 * Extended by James Lendrem, Michael Obi, Samuel Ojo
 */

class MessageViewHolder(internal var row: View, private val backgroundRcv: Int, private val backgroundSend: Int, private val bubbleBackgroundRcv: Int, private val bubbleBackgroundSend: Int) {

    val STATUS_SENT = 0
    val STATUS_RECEIVED = 1
    internal var context: Context

    private val messageView: MessageView

    init {
        context = row.getContext()
        messageView = row as MessageView
    }

    fun setMessage(message: String) {

        messageView.setMessage(message)

    }

    fun setTimestamp(timestamp: String) {

        messageView.setTimestamp(timestamp)

    }

    fun setElevation(elevation: Float) {

        messageView.setElevation(elevation)

    }

    fun setSender(sender: String) {
        messageView.setSender(sender)
    }

    /**
     * Set Accept action -- Only for [ItemInvitationView]
     */
    fun setAcceptAction(action: () -> Unit) {
        messageView as ItemInvitationView
        messageView.setAcceptAction(action)
    }


    /**
     * Set Reject action -- Only for [ItemInvitationView]
     */
    fun setRejectAction(action: () -> Unit) {
        messageView as ItemInvitationView
        messageView.setRejectAction(action)
    }

    fun setBackground(messageType: Int) {

        var chatMessageBackground = ContextCompat.getColor(context, R.color.cardview_light_background)
        var bubbleBackground = ContextCompat.getColor(context, R.color.cardview_light_background)

        when (messageType) {
            STATUS_RECEIVED -> {
                chatMessageBackground = backgroundRcv
                bubbleBackground = bubbleBackgroundRcv
            }
            STATUS_SENT -> {
                chatMessageBackground = backgroundSend
                bubbleBackground = bubbleBackgroundSend
            }
        }

        messageView.setBackgroundColor(chatMessageBackground)
        messageView.setBackground(bubbleBackground)

    }

}
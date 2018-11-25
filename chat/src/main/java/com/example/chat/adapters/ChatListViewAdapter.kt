package com.example.chat.adapters

import android.content.Context
import android.support.v4.view.ViewCompat.setElevation
import android.support.design.widget.CoordinatorLayout.Behavior.setTag
import android.util.Log
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.BaseAdapter
import com.example.chat.models.ChatMessage
import com.example.chat.models.ChatMessageType
import com.example.chat.viewholders.MessageViewHolder
import com.example.chat.views.ViewBuilderInterface
import java.util.*


/**
 * List Adapter for use in the recycler view to display messages using the Message View Holder
 *
 *
 * Created by Timi
 * Extended by James Lendrem, Samuel Ojo
 */

class ChatViewListAdapter(internal var context: Context, private val viewBuilder: ViewBuilderInterface, private val backgroundRcv: Int, private val backgroundSend: Int, private val bubbleBackgroundRcv: Int, private val bubbleBackgroundSend: Int, private val bubbleElevation: Float) : BaseAdapter() {

    private var chatMessages: ArrayList<ChatMessage> = ArrayList()
    private var inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int = chatMessages.size

    override fun getItem(position: Int): Any = chatMessages[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemViewType(position: Int): Int = chatMessages[position].type.ordinal

    private fun getChatMessageType(position: Int): ChatMessageType = chatMessages[position].type

    override fun getViewTypeCount(): Int = 3

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        val holder: MessageViewHolder

        val messageType = getChatMessageType(position)
        val convertView = when (messageType) {
            ChatMessageType.Sent -> viewBuilder.buildSentView(context)
            ChatMessageType.Received -> viewBuilder.buildRecvView(context)
            else -> {
                val invitation = messageType as ChatMessageType.Invitation
                viewBuilder.buildInvitationView(context, invitation)
            }
        }

        holder = MessageViewHolder(convertView, backgroundRcv, backgroundSend, bubbleBackgroundRcv, bubbleBackgroundSend)
        convertView.tag = holder

        holder.setMessage(chatMessages[position].message)
        holder.setTimestamp(chatMessages[position].formattedTime)
        holder.setElevation(bubbleElevation)
        holder.setBackground(getItemViewType(position))

        val sender = chatMessages[position].sender
        if (sender != null) {
            holder.setSender(sender)
        }

        return convertView
    }

    fun addMessage(message: ChatMessage) {
        chatMessages.add(message)
        notifyDataSetChanged()
    }

    fun addMessages(chatMessages: ArrayList<ChatMessage>) {
        this.chatMessages.addAll(chatMessages)
        notifyDataSetChanged()
    }

    fun removeMessage(position: Int) {
        if (this.chatMessages.size > position) {
            this.chatMessages.removeAt(position)
        }
    }

    fun clearMessages() {
        this.chatMessages.clear()
        notifyDataSetChanged()
    }
}
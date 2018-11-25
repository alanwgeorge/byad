package com.example.chat.views

import android.content.Context
import com.example.chat.models.ChatMessageType

/**
 * Builder to create views for the Chat Client that are used to display sent and
 * received messages.
 *
 * Created by James Lendrem
 */
class ViewBuilder : ViewBuilderInterface {

    /**
     * Returns a MessageView object which is used to display messages that the chat-ui
     * has received.
     * @param context A context that is used to instantiate the view.
     * @return        MessageView object for displaying received messages.
     */
    override fun buildRecvView(context: Context): MessageView {

        return ItemRecvView(context)

    }

    /**
     * Returns a MessageView object which is used to display messages that the chat-ui
     * has sent.
     * @param context A context that is used to instantiate the view.
     * @return        MessageView object for displaying sent messages.
     */
    override fun buildSentView(context: Context): MessageView {

        return ItemSentView(context)

    }

    /**
     * @param context A context that is used to instantiate the view.
     * @return        MessageView object for displaying the invitation.
     *
     * Created by Tyler Walker
     */
    override fun buildInvitationView(context: Context, invitation: ChatMessageType.Invitation): MessageView {

        return ItemInvitationView(context, invitation)

    }

}
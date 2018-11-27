package io.tylerwalker.chat.views

import android.content.Context
import io.tylerwalker.chat.models.ChatMessageType

/**
 * Interface for the viewbuilder, is used so that people can create their own
 * ViewBuilders to create custom views.
 *
 * Created by James Lendrem
 */
interface ViewBuilderInterface {
    /**
     * Returns a MessageView object which is used to display messages that the chat-ui
     * has received.
     * @param context A context that is used to instantiate the view.
     * @return        MessageView object for displaying received messages.
     */
    fun buildRecvView(context: Context): MessageView

    /**
     * Returns a MessageView object which is used to display messages that the chat-ui
     * has sent.
     * @param context A context that is used to instantiate the view.
     * @return        MessageView object for displaying sent messages.
     */
    fun buildSentView(context: Context): MessageView

    /**
     * Returns a MessageView object which is used to display the initial invitation to
     * have a drink
     * @param context A context that is used to instantiate the view.
     * @return        MessageView object for displaying the initial Invitation.
     *
     * Created by Tyler Walker
     */
    fun buildInvitationView(context: Context, invitation: ChatMessageType.Invitation): MessageView

}
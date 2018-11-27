package io.tylerwalker.chat.models

import android.text.format.DateFormat
import java.util.concurrent.TimeUnit


/**
 * Chat Message model used when ChatMessages are required, either to be sent or received,
 * all messages that are to be shown in the chat-ui must be contained in this model.
 */
class ChatMessage(var message: String, var timestamp: Long, var type: ChatMessageType) {
    var sender: String? = null


    // 24 * 60 * 60 * 1000;
    val formattedTime: String
        get() {

            val oneDayInMillis = TimeUnit.DAYS.toMillis(1)

            val timeDifference = System.currentTimeMillis() - timestamp

            return if (timeDifference < oneDayInMillis)
                DateFormat.format("hh:mm a", timestamp).toString()
            else
                DateFormat.format("dd MMM - hh:mm a", timestamp).toString()
        }

}
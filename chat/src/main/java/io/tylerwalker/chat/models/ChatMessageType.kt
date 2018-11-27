package io.tylerwalker.chat.models

sealed class ChatMessageType(val ordinal: Int) {
    object Sent: ChatMessageType(0)
    object Received: ChatMessageType(1)
    class Invitation(
            val from: String,
            val fromAddress: String,
            val what: String,
            val where: String,
            val whereAddress: String,
            val acceptAction: () -> Unit,
            val rejectAction: () -> Unit,
            val isInitiatedByMe: Boolean,
            val isAccepted: Boolean): ChatMessageType(2)
}
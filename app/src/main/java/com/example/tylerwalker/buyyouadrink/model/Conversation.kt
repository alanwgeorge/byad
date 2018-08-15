package com.example.tylerwalker.buyyouadrink.model

import java.util.*

data class Conversation(
        val user_id: Int,
        val first_name: String,
        val last_name: String,
        val last_message_at: Date,
        val last_message: String,
        val image_url: String? = null
)
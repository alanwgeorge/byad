package com.example.tylerwalker.buyyouadrink.model

data class User(
        val user_id: Int,
        val first_name: String,
        val last_name: String,
        val location: Coordinates,
        val caption: String = "",

        val image_url: String? = null
)

data class Coordinates (
        val latitude: Float,
        val longitude: Float
)
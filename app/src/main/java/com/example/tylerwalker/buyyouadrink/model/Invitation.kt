package com.example.tylerwalker.buyyouadrink.model

data class Invitation(
        val fromId: String,
        val toId: String,
        val fromName: String,
        val toName: String,
        val fromImage: String,
        val toImage: String,
        val location: Coordinates,
        val locationName: String,
        val beverageType: String,
        val placeName: String,
        val messageBody: String
)
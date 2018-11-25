package com.example.tylerwalker.buyyouadrink.model

sealed class InvitationEvent {
    object Send: InvitationEvent()
}
package io.tylerwalker.buyyouadrink.model

sealed class InvitationEvent {
    object Send: InvitationEvent()
}
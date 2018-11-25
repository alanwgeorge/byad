package com.example.tylerwalker.buyyouadrink.service

import android.util.Log
import com.example.tylerwalker.buyyouadrink.model.Invitation
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.GeoPoint
import durdinapps.rxfirebase2.RxFirestore
import io.reactivex.CompletableObserver
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import java.util.*
import java.util.concurrent.TimeUnit

class InvitationService {
    private val settings = FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()
    private val firestore = FirebaseFirestore.getInstance()

    private val logTag = "InvitationService"

    init {
        firestore.firestoreSettings = settings
    }

    fun sendInvitation(invitation: Invitation): Flowable<InvitationResponse> {
        val completionProcessor = PublishProcessor.create<InvitationResponse>()

        val fromConversationRef = firestore
                .collection("users")
                .document(invitation.fromId)
                .collection("conversations")
                .document(invitation.toId)

        val toConversationRef = firestore
                .collection("users")
                .document(invitation.toId)
                .collection("conversations")
                .document(invitation.fromId)

        val from = RxFirestore.setDocument(fromConversationRef, mapOf(
                "with" to invitation.toName,
                "withId" to invitation.toId,
                "withImage" to invitation.toImage,
                "placeName" to invitation.placeName,
                "beverageType" to invitation.beverageType,
                "location" to GeoPoint(invitation.location.latitude.toDouble(), invitation.location.longitude.toDouble()),
                "locationName" to invitation.locationName,
                "timestamp" to Timestamp(System.currentTimeMillis() / 1000, 0),
                "invitationMessage" to invitation.messageBody,
                "isInitiatedByMe" to true,
                "isAccepted" to false,
                "isRejected" to false
        ))


        val to = RxFirestore.setDocument(toConversationRef, mapOf(
                "with" to invitation.fromName,
                "withId" to invitation.fromId,
                "withImage" to invitation.fromImage,
                "placeName" to invitation.placeName,
                "beverageType" to invitation.beverageType,
                "location" to GeoPoint(invitation.location.latitude.toDouble(), invitation.location.longitude.toDouble()),
                "locationName" to invitation.locationName,
                "invitationMessage" to invitation.messageBody,
                "timestamp" to Timestamp(System.currentTimeMillis() / 1000, 0),
                "isInitiatedByMe" to false,
                "isAccepted" to false,
                "isRejected" to false
        ))

        from.mergeWith(to)
                .doOnComplete {
                    completionProcessor.onNext(InvitationResponse(true))
                }
                .doOnError {
                    completionProcessor.onNext(InvitationResponse(false, it))
                }
                .subscribe()

        return completionProcessor as Flowable<InvitationResponse>
    }
}

data class InvitationResponse (
        val status: Boolean = false,
        val error: Throwable? = null
)
package io.tylerwalker.buyyouadrink.service

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.GeoPoint
import durdinapps.rxfirebase2.RxFirestore
import io.reactivex.Flowable
import io.reactivex.processors.PublishProcessor
import io.tylerwalker.buyyouadrink.model.Conversation
import io.tylerwalker.buyyouadrink.model.Coordinates
import io.tylerwalker.buyyouadrink.model.Message

class ConversationService {
    private val settings = FirebaseFirestoreSettings.Builder()
            .setTimestampsInSnapshotsEnabled(true)
            .build()
    private val firestore = FirebaseFirestore.getInstance()

    private val logTag = "ConversationService"

    init {
        firestore.firestoreSettings = settings
    }

    fun getConversations(uid: String): Flowable<ConversationsResponse> {
        val completionProcessor = PublishProcessor.create<ConversationsResponse>()

        val collection = firestore
                .collection("users")
                .document(uid)
                .collection("conversations")

        RxFirestore.getCollection(collection)
                .doOnSuccess {
                    val conversations = mutableListOf<Conversation>()
                    it.documents.forEach { t: DocumentSnapshot ->
                        t.data?.let {
                            val conversation = Conversation(
                                    with = it["with"] as String,
                                    withId = it["withId"] as String,
                                    withImage = it["withImage"] as String,
                                    initiated = it["timestamp"] as Timestamp,
                                    isInitiatedByMe = it["isInitiatedByMe"] as Boolean,
                                    location = Coordinates((it["location"] as GeoPoint).latitude.toFloat(), (it["location"] as GeoPoint).longitude.toFloat()),
                                    locationName = it["locationName"] as String,
                                    placeName = it["placeName"] as String,
                                    beverageType = it["beverageType"] as String,
                                    invitationMessage = it["invitationMessage"] as String,
                                    isAccepted = it["isAccepted"] as Boolean,
                                    isRejected = it["isRejected"] as Boolean
                            )

                            conversations.add(conversation)
                        }
                    }
                    completionProcessor.onNext(ConversationsResponse(status = true, conversations = conversations, error = null))
                }
                .doOnError {
                    completionProcessor.onNext(ConversationsResponse(status = false, conversations = null, error = it))
                }
                .subscribe()

        return completionProcessor as Flowable<ConversationsResponse>
    }

    fun getConversation(currentUser: String, with: String): Flowable<ConversationsResponse> {
        val completionProcessor = PublishProcessor.create<ConversationsResponse>()

        val doc = firestore
                .collection("users")
                .document(currentUser)
                .collection("conversations")
                .document(with)

        RxFirestore.getDocument(doc)
                .doOnSuccess {
                    it.data?.let {
                        val conversation = Conversation(
                                with = it["with"] as String,
                                withId = it["withId"] as String,
                                withImage = it["withImage"] as String,
                                initiated = it["timestamp"] as Timestamp,
                                isInitiatedByMe = it["isInitiatedByMe"] as Boolean,
                                location = Coordinates((it["location"] as GeoPoint).latitude.toFloat(), (it["location"] as GeoPoint).longitude.toFloat()),
                                locationName = it["locationName"] as String,
                                placeName = it["placeName"] as String,
                                beverageType = it["beverageType"] as String,
                                invitationMessage = it["invitationMessage"] as String,
                                isAccepted = it["isAccepted"] as Boolean,
                                isRejected = it["isRejected"] as Boolean
                        )
                        completionProcessor.onNext(ConversationsResponse(status = true, conversation = conversation, error = null))
                    } ?: completionProcessor.onNext(ConversationsResponse(status = false, conversations = null, error = Throwable("No data")))
                }
                .doOnError {
                    completionProcessor.onNext(ConversationsResponse(status = false, conversations = null, error = it))
                }
                .subscribe()

        return completionProcessor
    }

    fun sendMessage(from: String, to: String, message: Message): Flowable<ConversationsResponse> {
        val completionProcessor = PublishProcessor.create<ConversationsResponse>()

        val fromCollection = firestore
                .collection("users")
                .document(from)
                .collection("conversations")
                .document(to)
                .collection("messages")

        val toCollection = firestore
                .collection("users")
                .document(to)
                .collection("conversations")
                .document(from)
                .collection("messages")

        RxFirestore.addDocument(fromCollection, mapOf(
                "from" to message.from,
                "timestamp" to message.timestamp,
                "body" to message.body
        ))
        .mergeWith {
            RxFirestore.addDocument(toCollection, mapOf(
                    "from" to message.from,
                    "timestamp" to message.timestamp,
                    "body" to message.body
            ))
        }
        .doOnComplete {
            completionProcessor.onNext(ConversationsResponse(true))
        }
        .doOnError {
            completionProcessor.onNext(ConversationsResponse(false))
        }
        .subscribe()


        return completionProcessor as Flowable<ConversationsResponse>

    }

    fun getMessages(currentUser: String, with: String): Flowable<ConversationsResponse> {
        val completionProcessor = PublishProcessor.create<ConversationsResponse>()

        val messagesRef = firestore
                .collection("users")
                .document(currentUser)
                .collection("conversations")
                .document(with)
                .collection("messages")

        RxFirestore.getCollection(messagesRef)
                .doOnSuccess {
                    val messages = mutableListOf<Message>()
                    it.documents.forEach { document: DocumentSnapshot ->
                        Log.d(logTag, "getMessages() message: ${document["timestamp"]}")

                        val message = Message(
                                from = document["from"] as String,
                                timestamp = Timestamp((document["timestamp"] as Timestamp).seconds, 0),
                                body = document["body"] as String
                        )

                        messages.add(message)
                    }
                    completionProcessor.onNext(ConversationsResponse(true, messages = messages))
                }
                .doOnError {
                    Log.d(logTag, "getMessages() error: ${it.localizedMessage}")
                    completionProcessor.onNext(ConversationsResponse(false, error = it))
                }
                .subscribe()

        return completionProcessor as Flowable<ConversationsResponse>
    }

    fun acceptInvitation(currentUser: String, from: String): Flowable<ConversationsResponse> {
        val completionProcessor = PublishProcessor.create<ConversationsResponse>()

        val toConversation = firestore
                .collection("users")
                .document(currentUser)
                .collection("conversations")
                .document(from)

        val fromConversation = firestore
                .collection("users")
                .document(from)
                .collection("conversations")
                .document(currentUser)

        RxFirestore.updateDocument(toConversation, mapOf("isAccepted" to true))
                .mergeWith(RxFirestore.updateDocument(fromConversation, mapOf("isAccepted" to true)))
                .doOnComplete { completionProcessor.onNext(ConversationsResponse(true)) }
                .doOnError { completionProcessor.onNext(ConversationsResponse(false)) }
                .subscribe()

        return completionProcessor
    }

    fun rejectInvitation(currentUser: String, from: String): Flowable<ConversationsResponse> {
        val completionProcessor = PublishProcessor.create<ConversationsResponse>()

        val toConversation = firestore
                .collection("users")
                .document(currentUser)
                .collection("conversations")
                .document(from)

        val fromConversation = firestore
                .collection("users")
                .document(from)
                .collection("conversations")
                .document(currentUser)

        RxFirestore.updateDocument(toConversation, mapOf("isRejected" to true))
                .mergeWith(RxFirestore.updateDocument(fromConversation, mapOf("isRejected" to true)))
                .doOnComplete { completionProcessor.onNext(ConversationsResponse(true)) }
                .doOnError { completionProcessor.onNext(ConversationsResponse(false)) }
                .subscribe()

        return completionProcessor
    }
}

data class ConversationsResponse(
        val status: Boolean,
        val conversations: List<Conversation>? = null,
        val conversation: Conversation? = null,
        val messages: List<Message>? = null,
        val error: Throwable? = null
)
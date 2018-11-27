package io.tylerwalker.buyyouadrink.activity.messages

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.Timestamp
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.tylerwalker.buyyouadrink.R
import io.tylerwalker.buyyouadrink.activity.profile.ProfileActivity
import io.tylerwalker.buyyouadrink.model.Conversation
import io.tylerwalker.buyyouadrink.model.Message
import io.tylerwalker.buyyouadrink.model.User
import io.tylerwalker.buyyouadrink.model.UserRepository
import io.tylerwalker.buyyouadrink.module.App
import io.tylerwalker.buyyouadrink.service.ConversationService
import io.tylerwalker.buyyouadrink.service.LocationService
import io.tylerwalker.buyyouadrink.util.rotate
import io.tylerwalker.buyyouadrink.util.toBitmap
import io.tylerwalker.buyyouadrink.util.toRounded
import io.tylerwalker.chat.models.ChatMessage
import io.tylerwalker.chat.models.ChatMessageType
import kotlinx.android.synthetic.main.activity_conversation.*
import kotlinx.android.synthetic.main.chat_toolbar.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ConversationActivity : AppCompatActivity() {
    @Inject
    lateinit var messageEventsProcessor: PublishProcessor<MessageEvent>
    @Inject
    lateinit var messageEventsFlowable: Flowable<MessageEvent>

    @Inject
    lateinit var locationService: LocationService

    lateinit var conversation: Conversation

    private val blockUserDialogTag = BlockUserDialog::class.java.simpleName
    private val blockUserDialog: BlockUserDialog by lazy {
        BlockUserDialog().apply {
            arguments = Bundle().apply {
                putParcelable("conversation", conversation ?: intent.getParcelableExtra("conversation"))
            }
        }
    }

    @Inject
    lateinit var currentUser: User

    @Inject
    lateinit var conversationService: ConversationService

    @Inject
    lateinit var userRepository: UserRepository

    private var trash = CompositeDisposable()
    private val logTag = "ConversationActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation)

        val component = App().getComponent(this)
        component.inject(this)

       conversation = intent.getParcelableExtra("conversation")

        chat_toolbar.apply {
            conversation.with.let {
                toolbar_text.text = it
            }

            conversation.withImage.let {
                if (it.isNotEmpty()) {
                    chat_image.setImageBitmap(it.toBitmap()?.rotate()?.toRounded())
                    chat_image.scaleType = ImageView.ScaleType.CENTER_CROP
                }
            }

            chat_image.setOnClickListener { navigateToProfile(conversation.withId) }
        }

        chat_view.apply {
            updateMessages(format(conversation.messages))

            setOnSentMessageListener {
                val message = Message(currentUser.user_id, Timestamp(System.currentTimeMillis() / 1000, 0), it.message)
                messageEventsProcessor.onNext(MessageEvent.Send(message))
                true
            }
        }

        trash.add(observeSendEvents())
        trash.add(observeAcceptEvents())
        trash.add(observeRejectEvents())
        trash.add(observeBlockUserEvents())
        trash.add(observeKeepUserEvents())
        trash.add(queryForMessages())

        trash.add(queryOnce().subscribe())
    }

    private fun format(messages: List<Message>): ArrayList<ChatMessage> = ArrayList(messages.map {
        ChatMessage(
                it.body,
                it.timestamp.seconds * 1000,
                if (it.from == currentUser.user_id) ChatMessageType.Sent else ChatMessageType.Received
        )
    })

    private fun observeSendEvents(): Disposable = messageEventsFlowable
            .filter { it is MessageEvent.Send }
            .map { it as MessageEvent.Send }
            .flatMap { conversationService.sendMessage(currentUser.user_id, conversation.withId, it.message) }
            .subscribe({
                Log.d(logTag, "Message sent.")

            }, {
                Log.d(logTag, "Error sending message: ${it.localizedMessage}")
            })

    private fun observeAcceptEvents(): Disposable = messageEventsFlowable
            .filter { it === MessageEvent.Accept }
            .map { it as MessageEvent.Accept }
            .flatMap { conversationService.acceptInvitation(currentUser.user_id, conversation.withId) }
            .flatMap { conversationService.getConversation(currentUser.user_id, conversation.withId) }
            .subscribe({ res ->
                Log.d(logTag, "Invitation accepted.")
                res.conversation?.let {
                    conversation = it
                    queryOnce()
                } ?: Log.e(logTag, "No Conversation")

            }, {
                Log.d(logTag, "Error accepting invitation: ${it.localizedMessage}")
            })

    private fun observeRejectEvents(): Disposable = messageEventsFlowable
            .filter { it === MessageEvent.Reject }
            .map { it as MessageEvent.Reject }
            .flatMap { conversationService.rejectInvitation(currentUser.user_id, conversation.withId) }
            .subscribe({
                Log.d(logTag, "Invitation rejected.")
                blockUserDialog.show(supportFragmentManager, blockUserDialogTag)
            }, {
                Log.d(logTag, "Error rejecting invitation: ${it.localizedMessage}")
                Toast.makeText(this, "Something went wrong...", Toast.LENGTH_LONG).show()
                navigateToMessages()
            })

    private fun observeBlockUserEvents(): Disposable = messageEventsFlowable
            .filter { it === MessageEvent.BlockUser }
            .doOnNext { Log.d(logTag, "block user event") }
            .flatMap { userRepository.blockUser(currentUser.user_id, conversation.withId) }
            .doOnNext {
                Log.d(logTag, "block user success")
                if (!it.status) {
                    Log.d(logTag, "block user error")
                    Toast.makeText(this, "Something went wrong... We could not block that user.", Toast.LENGTH_LONG).show()
                }
                blockUserDialog.dismiss()
                navigateToMessages()
            }
            .subscribe({}, {
                Log.d(logTag, "block user catch")
                blockUserDialog.dismiss()
                navigateToMessages()
            })

    private fun observeKeepUserEvents(): Disposable = messageEventsFlowable
            .filter { it === MessageEvent.KeepUser }
            .doOnNext { Log.d(logTag, "keep user event") }
            .doOnNext {
                blockUserDialog.dismiss()
                navigateToMessages()
            }
            .subscribe()

    private fun queryForMessages() = Flowable.interval(15L, TimeUnit.SECONDS)
            .doOnNext { Log.d(logTag, "queryForMessages()") }
            .flatMap { queryOnce() }
            .subscribe( {}, { it: Throwable ->
                Log.d(logTag, "Error querying messages: ${it.localizedMessage}")
            })

    private fun queryOnce() = conversationService.getMessages(currentUser.user_id, conversation.withId)
            .doOnNext {
                if (it.status) {
                    chat_view.apply {
                        updateMessages(format(it.messages!!))
                        Log.d(logTag, "got messages: $it.messages")
                    }
                } else {
                    throw Exception("No messages.")
                }
            }
            .doOnError {
                Log.d(logTag, "error getting messages: $conversation")

            }

    private fun updateMessages(messages: ArrayList<ChatMessage>) {
        with (chat_view) {
            clearMessages()

            addMessage(ChatMessage(
                    conversation.invitationMessage,
                    conversation.initiated.seconds,
                    ChatMessageType.Invitation(
                            conversation.with,
                            locationService.getLocationAddress(this@ConversationActivity, currentUser.location) ?: "",
                            conversation.beverageType,
                            conversation.placeName,
                            locationService.getLocationAddress(this@ConversationActivity, conversation.location) ?: "",
                            acceptInvitation,
                            rejectInvitation,
                            isInitiatedByMe = conversation.isInitiatedByMe,
                            isAccepted = conversation.isAccepted
                    )))

            addMessages(messages)
        }
    }

    private val acceptInvitation: () -> Unit = {
        Log.d(logTag, "acceptInvitation()")
        messageEventsProcessor.onNext(MessageEvent.Accept)
    }
    private val rejectInvitation: () -> Unit = {
        Log.d(logTag, "rejectInvitation()")
        messageEventsProcessor.onNext(MessageEvent.Reject)
    }

    private fun navigateToMessages() {
        Log.d(logTag, "navigateToMessages()")
        val intent = Intent(this, MessagesActivity::class.java)
        startActivity(intent)
    }

    fun navigateToProfile(user_id: String) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("user_id", user_id)
        startActivity(intent)
    }

    override fun onStop() {
        super.onStop()
        trash.clear()
        trash = CompositeDisposable()
    }
}

sealed class MessageEvent {
    class Send(val message: Message): MessageEvent()
    object Accept: MessageEvent()
    object Reject: MessageEvent()
    object BlockUser: MessageEvent()
    object KeepUser: MessageEvent()
}
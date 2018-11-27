package io.tylerwalker.buyyouadrink.activity.messages

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import io.tylerwalker.buyyouadrink.R
import io.tylerwalker.buyyouadrink.activity.map.InvitationViewModel.Companion.logTag
import io.tylerwalker.buyyouadrink.model.Conversation
import io.tylerwalker.buyyouadrink.model.User
import io.tylerwalker.buyyouadrink.model.UserRepository
import io.tylerwalker.buyyouadrink.module.App
import kotlinx.android.synthetic.main.dialog_block_user.*
import javax.inject.Inject

class BlockUserDialog: DialogFragment() {
    @Inject
    lateinit var messageEventsProcessor: PublishProcessor<MessageEvent>
    @Inject
    lateinit var messageEventsFlowable: Flowable<MessageEvent>

    @Inject
    lateinit var currentUser: User

    @Inject
    lateinit var userRepository: UserRepository

    var conversation: Conversation? = null

    private var trash = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        conversation = arguments?.getParcelable("conversation")
        if (conversation == null) dismiss()

        return inflater.inflate(R.layout.dialog_block_user, container)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        App().getComponent(context).inject(this)
    }

    override fun onStart() {
        super.onStart()

        dialog?.run {
            window?.run {
                setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        }

        trash.add(observeBlockUserEvents())
        trash.add(observeKeepUserEvents())

        block_button.setOnClickListener { handleBlockUser() }
        keep_button.setOnClickListener { handleKeepUser() }
    }

    override fun dismiss() {
        super.dismiss()
        navigateToMessages()
    }

    private fun observeBlockUserEvents(): Disposable = messageEventsFlowable
            .filter { it === io.tylerwalker.buyyouadrink.activity.messages.MessageEvent.BlockUser }
            .doOnNext { Log.d(logTag, "block user event") }
            .flatMap { userRepository.blockUser(currentUser.user_id, conversation!!.withId) }
            .doOnNext {
                Log.d(logTag, "block user success")
                if (!it.status) {
                    Log.d(logTag, "block user error")
                    Toast.makeText(context, "Something went wrong... We could not block that user.", Toast.LENGTH_LONG).show()
                }
                dismiss()
            }
            .subscribe({}, {
                Log.d(logTag, "block user catch")
                dismiss()
            })

    private fun observeKeepUserEvents(): Disposable = messageEventsFlowable
            .filter { it === io.tylerwalker.buyyouadrink.activity.messages.MessageEvent.KeepUser }
            .doOnNext { Log.d(logTag, "keep user event") }
            .doOnNext {
                dismiss()
                navigateToMessages()
            }
            .subscribe()

    private fun navigateToMessages() {
        Log.d(logTag, "navigateToMessages()")
        val intent = Intent(context, MessagesActivity::class.java)
        startActivity(intent)
    }


    private fun handleBlockUser() {
        Log.d("BlockUserDialog", "handleBlockUser()")
        messageEventsProcessor.onNext(MessageEvent.BlockUser)
    }

    private fun handleKeepUser() {
        Log.d("BlockUserDialog", "handleKeepUser()")
        messageEventsProcessor.onNext(MessageEvent.KeepUser)
    }
}
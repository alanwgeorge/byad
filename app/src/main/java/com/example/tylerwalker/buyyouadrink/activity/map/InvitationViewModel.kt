package com.example.tylerwalker.buyyouadrink.activity.map

import android.app.Application
import android.arch.lifecycle.*
import android.icu.lang.UCharacter.GraphemeClusterBreak.L
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.tylerwalker.buyyouadrink.model.Coordinates
import com.example.tylerwalker.buyyouadrink.model.Invitation
import com.example.tylerwalker.buyyouadrink.model.InvitationEvent
import com.example.tylerwalker.buyyouadrink.model.User
import com.example.tylerwalker.buyyouadrink.service.InvitationResponse
import com.example.tylerwalker.buyyouadrink.service.InvitationService
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import javax.inject.Inject

class InvitationViewModel(app: Application): AndroidViewModel(app), LifecycleObserver {
    companion object {
        const val logTag = "InvitationViewModel"
        sealed class InvitationStage {
            object Compose: InvitationStage()
            object Loading: InvitationStage()
            object Confirm: InvitationStage()
            object Failure: InvitationStage()
        }
    }

    val placeName = MutableLiveData<String>()
    val beverageType = MutableLiveData<String>()
    val locationName = MutableLiveData<String>()
    val location = MutableLiveData<Coordinates>()
    val inviteeName = MutableLiveData<String>()
    val inviteeId = MutableLiveData<String>()
    val inviteeImage = MutableLiveData<String>()
    val messageBody = MutableLiveData<String>()
    val stage = MutableLiveData<InvitationStage>()
    val loadingVisibility = MediatorLiveData<Int>().apply {
        addSource(stage) {
            when (it) {
                InvitationStage.Loading -> this.value = View.VISIBLE
                else -> this.value = View.GONE
            }
        }
    }
    val invitationVisibility = MediatorLiveData<Int>().apply {
        addSource(stage) {
            when (it) {
                InvitationStage.Compose -> this.value = View.VISIBLE
                else -> this.value = View.GONE
            }
        }
    }
    val confirmVisibility = MediatorLiveData<Int>().apply {
        addSource(stage) {
            when (it) {
                InvitationStage.Confirm -> this.value = View.VISIBLE
                else -> this.value = View.GONE
            }
        }
    }

    val send: () -> Unit = { invitationEventsProcessor.onNext(InvitationEvent.Send) }

    @Inject
    lateinit var currentUser: User

    @Inject
    lateinit var invitationService: InvitationService

    @Inject
    lateinit var invitationEventsProcessor: PublishProcessor<InvitationEvent>

    @Inject
    lateinit var invitationEventsFlowable: Flowable<InvitationEvent>

    private var compositeDisposable = CompositeDisposable()

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart() {
        compositeDisposable.add(observeSendEvents())
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop() {
        compositeDisposable.clear()
        compositeDisposable = CompositeDisposable()
    }

    private fun observeSendEvents(): Disposable = invitationEventsFlowable
            .filter { it === InvitationEvent.Send }
            .doOnNext { validateInvitation() }
            .doOnNext { stage.postValue(InvitationStage.Loading) }
            .flatMap { sendInvitation() }
            .subscribe({
                if (it.status) {
                    stage.postValue(InvitationStage.Confirm)
                } else {
                    Toast.makeText(getApplication(), "Something went wrong...", Toast.LENGTH_SHORT).show()
                    stage.postValue(InvitationStage.Failure)
                }
            }, {
                Log.d(logTag, it.localizedMessage)
                stage.postValue(InvitationStage.Failure)
            })

    private fun validateInvitation() {
        if (inviteeId.value.isNullOrEmpty()) {
            Toast.makeText(getApplication(), "Something went wrong...", Toast.LENGTH_SHORT).show()
            Log.d(logTag, "send() to ID is null")
            throw Exception("inviteeId is null")
        }

        if (location.value == null) {
            Toast.makeText(getApplication(), "Something went wrong...", Toast.LENGTH_SHORT).show()
            Log.d(logTag, "send() to location is null")
            throw Exception("location is null")
        }
    }

    private fun sendInvitation(): Flowable<InvitationResponse> {
        val invitation = Invitation(
                fromId = currentUser.user_id,
                toId = inviteeId.value!!,
                fromName = currentUser.display_name,
                toName = currentUser.display_name,
                fromImage = currentUser.profile_image,
                toImage = inviteeImage.value ?: "",
                location =location.value!!,
                locationName = locationName.value ?: "???",
                placeName = placeName.value ?: "???",
                beverageType = beverageType.value ?: "???",
                messageBody = messageBody.value ?: "Hi, I would like to buy you a drink!"
        )

        Log.d(logTag, "sendInvitation() invitation: $invitation")

        return invitationService.sendInvitation(invitation)
    }
}
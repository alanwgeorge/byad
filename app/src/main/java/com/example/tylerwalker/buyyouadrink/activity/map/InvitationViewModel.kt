package com.example.tylerwalker.buyyouadrink.activity.map

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import android.view.View

class InvitationViewModel(app: Application): AndroidViewModel(app), LifecycleObserver {
    val placeName = MutableLiveData<String>()
    val beverageType = MutableLiveData<String>()
    val locationName = MutableLiveData<String>()
    val inviteeName = MutableLiveData<String>()
    val messageBody = MutableLiveData<String>()
    val isLoading = MutableLiveData<Boolean>()
    val loadingVisibility = MediatorLiveData<Int>().apply {
        addSource(isLoading) {
            when (it) {
                true -> this.value = View.VISIBLE
                else -> this.value = View.GONE
            }
        }
    }
    val invitationVisibility = MediatorLiveData<Int>().apply {
        addSource(isLoading) {
            when (it) {
                true -> this.value = View.GONE
                else -> this.value = View.VISIBLE
            }
        }
    }

    var send: () -> Unit = { }

    fun sendInvitation(view: View) { send() }
}
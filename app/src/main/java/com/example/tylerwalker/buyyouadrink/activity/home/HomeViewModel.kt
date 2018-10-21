package com.example.tylerwalker.buyyouadrink.activity.home

import android.app.Application
import android.arch.lifecycle.*
import android.view.View
import com.example.tylerwalker.buyyouadrink.model.ListItem
import com.example.tylerwalker.buyyouadrink.model.NavigationEvent
import com.example.tylerwalker.buyyouadrink.model.User
import io.reactivex.processors.PublishProcessor
import javax.inject.Inject

class HomeViewModel(app: Application): AndroidViewModel(app), LifecycleObserver {
    @Inject
    lateinit var navigationEventsProcessor: PublishProcessor<NavigationEvent>

    val users = MutableLiveData<List<User>>()
    val listItems = MutableLiveData<List<ListItem>>()

    fun publishNavigationEvent(event: NavigationEvent) = navigationEventsProcessor.onNext(event)
    fun transitionToMessages(view: View) = publishNavigationEvent(NavigationEvent.Messages)
    fun transitionToSettings(view: View) = publishNavigationEvent(NavigationEvent.Settings)
}
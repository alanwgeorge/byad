package io.tylerwalker.buyyouadrink.activity.home

import android.app.Application
import android.arch.lifecycle.*
import android.view.View
import io.reactivex.processors.PublishProcessor
import io.tylerwalker.buyyouadrink.model.ListItem
import io.tylerwalker.buyyouadrink.model.NavigationEvent
import io.tylerwalker.buyyouadrink.model.User
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
package com.example.tylerwalker.buyyouadrink.activity.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.databinding.DataBindingUtil
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.example.tylerwalker.buyyouadrink.R
import com.example.tylerwalker.buyyouadrink.R.string.conversations
import com.example.tylerwalker.buyyouadrink.activity.messages.MessagesActivity
import com.example.tylerwalker.buyyouadrink.activity.profile.ProfileActivity
import com.example.tylerwalker.buyyouadrink.activity.profile.SetupProfileActivity
import com.example.tylerwalker.buyyouadrink.databinding.ActivityHomeBinding
import com.example.tylerwalker.buyyouadrink.model.*
import com.example.tylerwalker.buyyouadrink.module.App
import com.example.tylerwalker.buyyouadrink.util.BERKELEY
import com.example.tylerwalker.buyyouadrink.util.distanceTo
import io.reactivex.Flowable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class HomeScreen : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: Adapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    lateinit var viewModel: HomeViewModel

    private var blackList: List<String> = listOf()

    @Inject
    lateinit var navigationEventsFlowable: Flowable<NavigationEvent>

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var currentUser: User

    var trash = CompositeDisposable()

    private val logTag = "HomeScreen"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        val component = App().getComponent(this)
        component.inject(this)

        val binding = DataBindingUtil.setContentView<ActivityHomeBinding>(this, R.layout.activity_home)
        binding.setLifecycleOwner(this)

        ViewModelProviders.of(this).get(HomeViewModel::class.java).apply {
            viewModel = this
            component.inject(this)
            lifecycle.addObserver(this)
            binding.viewModel = this

            listItems.observe(this@HomeScreen, Observer {
                viewAdapter.updateUsers(it)
            })
        }

        setupRecyclerView()

        trash.add(getBlackList())
        trash.add(getAllUsers())
        trash.add(observeNavigationEvents())
    }

    private fun getAllUsers(): Disposable = userRepository.getAllUsers()
            .doOnNext { Log.d(logTag, "Got users: ${it.users}") }
            .map { it.users?.filter { user -> true } }
            .map { sortUsersByProximity(it) }
            .map { filterBlacklistedUsers(it) }
            .subscribe({
                it?.let { users ->
                    updateViewModel(users)
                }

            }, {
                Log.e(logTag, "getAllUsers(): error: ${it.localizedMessage}")
            })

    private fun getBlackList(): Disposable = userRepository.getBlackList(currentUser.user_id)
            .map { it.users }
            .doOnNext {
                Log.d(logTag,"get black list: $it")
                it?.let {
                    blackList = it
                    viewModel.users.value?.let {
                        updateViewModel(filterBlacklistedUsers(it))
                    }
                }
            }
            .doOnError {
                Log.d(logTag, "get black list error: ${it.localizedMessage}")
            }
            .subscribe()


    private fun filterBlacklistedUsers(users: List<User>): List<User> {
        return users.filter {
            !blackList.contains(it.user_id)
        }
    }

    private fun updateViewModel(users: List<User>) {
        viewModel.users.value = users
        viewModel.listItems.value = groupUsersByPoximity(users)
    }

    private fun observeNavigationEvents(): Disposable = navigationEventsFlowable
            .doOnNext { Log.d(logTag, "Navigation Event: $it") }
            .doOnNext {
                when (it) {
                    is NavigationEvent.Settings -> transitionToSettings()
                    is NavigationEvent.Messages -> transitionToMessages()
                    is NavigationEvent.Profile -> transitionToProfile("0")
                }
            }
            .subscribe()

    private fun setupRecyclerView() {
        viewManager = LinearLayoutManager(this)
        viewAdapter = Adapter(this)

        recyclerView = findViewById<RecyclerView>(R.id.recycler).apply {
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    private fun sortUsersByProximity(users: List<User>): List<User> {
        currentUser.location.let {
            val safeCurrentLocation: Coordinates = if (it.latitude == 0F) {
                    BERKELEY
                } else {
                    Coordinates(it.latitude, it.longitude)
                }

            return users.sortedBy { user ->
                safeCurrentLocation.distanceTo(user.location)
            }
        }
    }

    private fun groupUsersByPoximity(users: List<User>): List<ListItem> {
        val augmentedUsersList = mutableListOf<ListItem>()

        val safeCurrentLocation: Coordinates = if (currentUser.location.latitude == 0F) {
            BERKELEY
        } else {
            Coordinates(currentUser.location.latitude, currentUser.location.longitude)
        }

        var currentLabelIndex = -1

        users.forEach {user ->
            val it = safeCurrentLocation.distanceTo(user.location)
            when {
                it < 5F -> {
                    if (currentLabelIndex == 0) {
                        augmentedUsersList.add(ListItem.UserListItem(user))
                    } else {
                        augmentedUsersList.add(ListItem.ListItemHeader("Within 5 miles"))
                        augmentedUsersList.add(ListItem.UserListItem(user))
                        currentLabelIndex = 0
                    }
                }
                it < 10F -> {
                    if (currentLabelIndex == 1) {
                        augmentedUsersList.add(ListItem.UserListItem(user))
                    } else {
                        augmentedUsersList.add(ListItem.ListItemHeader("Within 10 miles"))
                        augmentedUsersList.add(ListItem.UserListItem(user))
                        currentLabelIndex = 1
                    }
                }
                it < 25F -> {
                    if (currentLabelIndex == 2) {
                        augmentedUsersList.add(ListItem.UserListItem(user))
                    } else {
                        augmentedUsersList.add(ListItem.ListItemHeader("Within 25 miles"))
                        augmentedUsersList.add(ListItem.UserListItem(user))
                        currentLabelIndex = 2
                    }
                }
                else -> {
                    if (currentLabelIndex >= 3) {
                        augmentedUsersList.add(ListItem.UserListItem(user))
                    } else {
                        augmentedUsersList.add(ListItem.ListItemHeader("Really far away"))
                        augmentedUsersList.add(ListItem.UserListItem(user))
                        currentLabelIndex = 3
                    }
                }
            }
        }

        return augmentedUsersList
    }

    fun transitionToProfile(user_id: String) {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("user_id", user_id)
        startActivity(intent)
    }

    fun transitionToSettings() {
        val intent = Intent(this, SetupProfileActivity::class.java)
        startActivity(intent)
    }

    fun transitionToMessages() {
        val intent = Intent(this, MessagesActivity::class.java)
        startActivity(intent)
    }

}

package io.tylerwalker.buyyouadrink.activity.messages

import android.content.Intent
import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.tylerwalker.buyyouadrink.R
import io.tylerwalker.buyyouadrink.activity.home.HomeScreen
import io.tylerwalker.buyyouadrink.activity.profile.SetupProfileActivity
import io.tylerwalker.buyyouadrink.model.Conversation
import io.tylerwalker.buyyouadrink.model.User
import io.tylerwalker.buyyouadrink.model.UserRepository
import io.tylerwalker.buyyouadrink.module.App
import io.tylerwalker.buyyouadrink.service.ConversationService
import javax.inject.Inject

class MessagesActivity : AppCompatActivity() {
    @Inject
    lateinit var currentUser: User

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var conversationService: ConversationService

    private var blackList: List<String> = listOf()

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: Adapter
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var trash = CompositeDisposable()

    private val logTag = "MessagesActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val component = App().getComponent(this)
        component.inject(this)

        supportActionBar?.hide()

        setContentView(R.layout.activity_messages)

        setupRecyclerView()
        setupToolbar()
    }

    override fun onStart() {
        super.onStart()
        trash.add(getBlackList())
    }

    private fun setupRecyclerView() {
        viewManager = LinearLayoutManager(this)
        viewAdapter = Adapter(this)

        recyclerView = findViewById<RecyclerView>(R.id.recycler).apply {
            setHasFixedSize(false)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    private fun setupToolbar() {
        val settingsImage = findViewById<ImageView>(R.id.toolbar_image_left)
        val messagesImage = findViewById<ImageView>(R.id.toolbar_image_right)
        val title = findViewById<TextView>(R.id.toolbar_text)

        title.text = getString(R.string.conversations)

        val homeDrawable = BitmapFactory.decodeResource(resources, R.drawable.main_logo)
        messagesImage.setImageBitmap(homeDrawable)
        messagesImage.adjustViewBounds = true
        messagesImage.scaleType = ImageView.ScaleType.FIT_CENTER

        settingsImage.setOnClickListener {
            transitionToSettings(it)
        }

        messagesImage.setOnClickListener {
            transitionToHome(it)
        }
    }

    private fun getConversations(): Disposable = conversationService.getConversations(currentUser.user_id)
            .map { it.conversations ?: throw Exception("No conversations") }
            .doOnNext { Log.d(logTag, "got conversations: $it") }
            .map { filterRejectedConversations(it) }
            .map { filterBlacklistedConversations(it) }
            .subscribe({ viewAdapter.updateMessages(it) }, {
                Log.d(logTag, "getConversations() error ${it.localizedMessage}")
            })

    private fun getBlackList(): Disposable = userRepository.getBlackList(currentUser.user_id)
            .map { it.users }
            .doOnNext {
                Log.d(logTag,"get black list: $it")
                it?.let {
                    blackList = it
                }
            }
            .doOnComplete { trash.add(getConversations()) }
            .doOnError {
                Log.d(logTag, "get black list error: ${it.localizedMessage}")
                trash.add(getConversations())
            }
            .subscribe({}, {
                Log.d(logTag, "get black list error: ${it.localizedMessage}")
            })

    private fun filterBlacklistedConversations(conversations: List<Conversation>): List<Conversation> {
        return conversations.filter {
            !blackList.contains(it.withId)
        }
    }

    private fun filterRejectedConversations(conversations: List<Conversation>): List<Conversation> {
        Log.d(logTag, "conversations: $conversations")
        return conversations.filter {
            !it.isRejected
        }
    }

    private fun transitionToSettings(view: View) {
        val intent = Intent(this, SetupProfileActivity::class.java)
        startActivity(intent)
    }

    private fun transitionToHome(view: View) {
        val intent = Intent(this, HomeScreen::class.java)
        startActivity(intent)
    }

    fun transitionToConversation(conversation: Conversation) {
        val intent = Intent(this, ConversationActivity::class.java)
        intent.putExtra("conversation", conversation)
        startActivity(intent)
    }


    override fun onStop() {
        super.onStop()
        trash.clear()
        trash = CompositeDisposable()
    }
}

package io.tylerwalker.chat

import android.content.Context
import android.widget.EditText
import android.view.View.OnFocusChangeListener
import android.text.Editable
import android.text.TextWatcher
import android.text.TextUtils
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.text.InputType
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.support.v7.widget.CardView
import android.view.LayoutInflater
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.widget.ListView
import android.widget.RelativeLayout
import io.tylerwalker.chat.adapters.ChatViewListAdapter
import io.tylerwalker.chat.fab.FloatingActionsMenu
import io.tylerwalker.chat.models.ChatMessage
import io.tylerwalker.chat.models.ChatMessageType
import io.tylerwalker.chat.views.ViewBuilder
import io.tylerwalker.chat.views.ViewBuilderInterface


/**
 * Created by timi on 17/11/2015.
 */
open class ChatView @JvmOverloads constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0, private val viewBuilder: ViewBuilderInterface = ViewBuilder()) : RelativeLayout(context, attrs, defStyleAttr) {

    private var inputFrame: CardView? = null
    private var chatListView: ListView? = null
    var inputEditText: EditText? = null
        private set
    var actionsMenu: FloatingActionsMenu? = null
        private set
    private var previousFocusState = false
    private var useEditorAction: Boolean = false
    private var isTyping: Boolean = false
    private var typingListener: TypingListener? = null
    private val typingTimerRunnable = Runnable {
        if (isTyping) {
            isTyping = false
            if (typingListener != null) typingListener!!.userStoppedTyping()
        }
    }
    private var onSentMessageListener: ((ChatMessage) -> Boolean)? = null
    private var chatViewListAdapter: ChatViewListAdapter? = null

    private var inputHint: String? = null
    private var inputFrameBackgroundColor: Int = 0
    private var mBackgroundColor: Int = 0
    private var inputTextSize: Int = 0
    private var inputTextColor: Int = 0
    private var inputHintColor: Int = 0
    private var sendButtonBackgroundTint: Int = 0
    private var sendButtonIconTint: Int = 0

    private var bubbleElevation: Float = 0.toFloat()

    private var backgroundRcv: Int = 0
    private var backgroundSend: Int = 0
    private var bubbleBackgroundRcv: Int = 0
    private var bubbleBackgroundSend: Int = 0 // Drawables cause cardRadius issues. Better to use background color
    private var sendButtonIcon: Drawable? = null
    private var buttonDrawable: Drawable? = null
    private var attributes: TypedArray? = null
    private var textAppearanceAttributes: TypedArray? = null

    val typedMessage: String
        get() = inputEditText!!.text.toString()

    internal constructor(context: Context) : this(context, null) {}

    init {
        init(context, attrs, defStyleAttr)

    }

    private fun init(context: Context, attrs: AttributeSet?, defStyleAttr: Int) {
        LayoutInflater.from(getContext()).inflate(R.layout.chat_view, this, true)
        initializeViews()
        getXMLAttributes(attrs, defStyleAttr)
        setViewAttributes()
        setListAdapter()
        setButtonClickListeners()
        setUserTypingListener()
        setUserStoppedTypingListener()
    }

    private fun initializeViews() {
        chatListView = findViewById(R.id.chat_list) as ListView
        inputFrame = findViewById(R.id.input_frame) as CardView
        inputEditText = findViewById(R.id.input_edit_text) as EditText
        actionsMenu = findViewById(R.id.sendButton) as FloatingActionsMenu
    }

    private fun getXMLAttributes(attrs: AttributeSet?, defStyleAttr: Int) {
        attributes = context!!.obtainStyledAttributes(attrs, R.styleable.ChatView, defStyleAttr, R.style.ChatViewDefault)
        getChatViewBackgroundColor()
        getAttributesForChatMessageRow()
        getAttributesForBubbles()
        getAttributesForInputFrame()
        getAttributesForInputText()
        getAttributesForSendButton()
        getUseEditorAction()
        attributes!!.recycle()
    }

    private fun setListAdapter() {
        chatViewListAdapter = ChatViewListAdapter(context!!, ViewBuilder(), backgroundRcv, backgroundSend, bubbleBackgroundRcv, bubbleBackgroundSend, bubbleElevation)
        chatListView!!.adapter = chatViewListAdapter
    }

    private fun setViewAttributes() {
        setChatViewBackground()
        setInputFrameAttributes()
        setInputTextAttributes()
        setSendButtonAttributes()
        setUseEditorAction()
    }

    private fun getChatViewBackgroundColor() {
        mBackgroundColor = attributes!!.getColor(R.styleable.ChatView_backgroundColor, -1)
    }

    private fun getAttributesForChatMessageRow() {
        backgroundRcv = attributes!!.getColor(R.styleable.ChatView_backgroundRcv, ContextCompat.getColor(context!!, R.color.default_chat_message_background_color_rcv))
        backgroundSend = attributes!!.getColor(R.styleable.ChatView_backgroundSend, ContextCompat.getColor(context!!, R.color.default_chat_message_background_color_send))
    }

    private fun getAttributesForBubbles() {
        val dip4 = context!!.getResources().getDisplayMetrics().density * 4.0f
        val elevation = attributes!!.getInt(R.styleable.ChatView_bubbleElevation, ELEVATED)
        bubbleElevation = if (elevation == ELEVATED) dip4 else 0F

        bubbleBackgroundRcv = attributes!!.getColor(R.styleable.ChatView_bubbleBackgroundRcv, ContextCompat.getColor(context!!, R.color.default_bubble_color_rcv))
        bubbleBackgroundSend = attributes!!.getColor(R.styleable.ChatView_bubbleBackgroundSend, ContextCompat.getColor(context!!, R.color.default_bubble_color_send))
    }


    private fun getAttributesForInputFrame() {
        inputFrameBackgroundColor = attributes!!.getColor(R.styleable.ChatView_inputBackgroundColor, -1)
    }

    private fun setInputFrameAttributes() {
        inputFrame!!.setCardBackgroundColor(inputFrameBackgroundColor)
    }

    private fun setChatViewBackground() {
        this.setBackgroundColor(mBackgroundColor)
    }

    private fun getAttributesForInputText() {
        setInputTextDefaults()
        if (hasStyleResourceSet()) {
            setTextAppearanceAttributes()
            setInputTextSize()
            setInputTextColor()
            setInputHintColor()
            textAppearanceAttributes!!.recycle()
        }
        overrideTextStylesIfSetIndividually()
    }


    private fun setTextAppearanceAttributes() {
        val textAppearanceId = attributes!!.getResourceId(R.styleable.ChatView_inputTextAppearance, 0)
        textAppearanceAttributes = context.obtainStyledAttributes(textAppearanceId, R.styleable.ChatViewInputTextAppearance)
    }

    private fun setInputTextAttributes() {
        if (inputHint != null) {
            inputEditText!!.hint = inputHint
        }
        inputEditText!!.setTextColor(inputTextColor)
        inputEditText!!.setHintTextColor(inputHintColor)
        inputEditText!!.setTextSize(TypedValue.COMPLEX_UNIT_PX, inputTextSize.toFloat())
    }

    private fun getAttributesForSendButton() {
        sendButtonBackgroundTint = attributes!!.getColor(R.styleable.ChatView_sendBtnBackgroundTint, -1)
        sendButtonIconTint = attributes!!.getColor(R.styleable.ChatView_sendBtnIconTint, Color.WHITE)
        sendButtonIcon = attributes!!.getDrawable(R.styleable.ChatView_sendBtnIcon)
    }

    private fun setSendButtonAttributes() {
        actionsMenu!!.sendButton!!.colorNormal = sendButtonBackgroundTint
        actionsMenu!!.iconDrawable = sendButtonIcon

        buttonDrawable = actionsMenu!!.iconDrawable
        actionsMenu!!.setButtonIconTint(sendButtonIconTint)
    }

    private fun getUseEditorAction() {
        useEditorAction = attributes!!.getBoolean(R.styleable.ChatView_inputUseEditorAction, false)
    }

    private fun setUseEditorAction() {
        if (useEditorAction) {
            setupEditorAction()
        } else {
            inputEditText!!.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_AUTO_CORRECT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        }
    }

    private fun hasStyleResourceSet(): Boolean {
        return attributes!!.hasValue(R.styleable.ChatView_inputTextAppearance)
    }

    private fun setInputTextDefaults() {
        inputTextSize = context!!.getResources().getDimensionPixelSize(R.dimen.default_input_text_size)
        inputTextColor = ContextCompat.getColor(context!!, R.color.black)
        inputHintColor = ContextCompat.getColor(context!!, R.color.main_color_gray)
        setInputHint()
    }

    private fun setInputTextSize() {
        if (textAppearanceAttributes!!.hasValue(R.styleable.ChatView_inputTextSize)) {
            inputTextSize = attributes!!.getDimensionPixelSize(R.styleable.ChatView_inputTextSize, inputTextSize)
        }
    }

    private fun setInputTextColor() {
        if (textAppearanceAttributes!!.hasValue(R.styleable.ChatView_inputTextColor)) {
            inputTextColor = attributes!!.getColor(R.styleable.ChatView_inputTextColor, inputTextColor)
        }
    }

    fun setInputHint() {
        if (attributes!!.hasValue(R.styleable.ChatView_inputHint)) {
            inputHint = attributes!!.getString(R.styleable.ChatView_inputHint)
        }
    }

    private fun setInputHintColor() {
        if (textAppearanceAttributes!!.hasValue(R.styleable.ChatView_inputHintColor)) {
            inputHintColor = attributes!!.getColor(R.styleable.ChatView_inputHintColor, inputHintColor)
        }
    }

    private fun overrideTextStylesIfSetIndividually() {
        inputTextSize = attributes!!.getDimension(R.styleable.ChatView_inputTextSize, inputTextSize.toFloat()).toInt()
        inputTextColor = attributes!!.getColor(R.styleable.ChatView_inputTextColor, inputTextColor)
        inputHintColor = attributes!!.getColor(R.styleable.ChatView_inputHintColor, inputHintColor)
    }

    private fun setupEditorAction() {
        inputEditText!!.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_AUTO_CORRECT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        inputEditText!!.imeOptions = EditorInfo.IME_ACTION_SEND
        inputEditText!!.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    val stamp = System.currentTimeMillis()
                    val message = inputEditText!!.text.toString()

                    if (!TextUtils.isEmpty(message)) {
                        sendMessage(message, stamp)
                    }
                    return true
                }
                return false
            }
        })
    }

    fun setLocationListener(action: () -> Unit) {

    }

    private fun setButtonClickListeners() {

        actionsMenu!!.sendButton!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {

                if (actionsMenu!!.isExpanded) {
                    actionsMenu!!.collapse()
                    return
                }

                val stamp = System.currentTimeMillis()
                val message = inputEditText!!.text.toString()
                if (!TextUtils.isEmpty(message)) {
                    sendMessage(message, stamp)
                }

            }
        })

        actionsMenu!!.sendButton!!.setOnLongClickListener(object : OnLongClickListener {

            override fun onLongClick(v: View): Boolean {
                actionsMenu!!.expand()
                return true
            }
        })
    }

    private fun setUserTypingListener() {
        inputEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length > 0) {

                    if (!isTyping) {
                        isTyping = true
                        if (typingListener != null) typingListener!!.userStartedTyping()
                    }

                    removeCallbacks(typingTimerRunnable)
                    postDelayed(typingTimerRunnable, 1500)
                }
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    private fun setUserStoppedTypingListener() {
        inputEditText!!.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (previousFocusState && !hasFocus && typingListener != null) {
                typingListener!!.userStoppedTyping()
            }
            previousFocusState = hasFocus
        }
    }

    fun setTypingListener(typingListener: TypingListener) {
        this.typingListener = typingListener
    }

    fun setOnSentMessageListener(onSentMessageListener: (ChatMessage) -> Boolean) {
        this.onSentMessageListener = onSentMessageListener
    }

    private fun sendMessage(message: String, stamp: Long) {

        val chatMessage = ChatMessage(message, stamp, ChatMessageType.Sent)
        if (onSentMessageListener != null && onSentMessageListener!!(chatMessage)) {
            chatViewListAdapter!!.addMessage(chatMessage)
            inputEditText!!.setText("")
        }
    }

    fun addMessage(chatMessage: ChatMessage) {
        chatViewListAdapter!!.addMessage(chatMessage)
    }

    fun addMessages(messages: ArrayList<ChatMessage>) {
        chatViewListAdapter!!.addMessages(messages)
    }

    fun removeMessage(position: Int) {
        chatViewListAdapter!!.removeMessage(position)
    }

    fun clearMessages() {
        chatViewListAdapter!!.clearMessages()
    }


    interface TypingListener {

        fun userStartedTyping()

        fun userStoppedTyping()

    }

    companion object {

        private const val FLAT = 0
        private const val ELEVATED = 1
    }

}
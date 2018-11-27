package io.tylerwalker.chat.fab

import android.os.Parcel
import android.os.Parcelable
import android.graphics.PorterDuff
import android.widget.TextView
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.ViewGroup
import android.animation.ObjectAnimator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.view.TouchDelegate
import android.support.annotation.ColorRes
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.support.v4.content.ContextCompat
import android.animation.AnimatorSet
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.support.annotation.NonNull
import android.support.v7.view.ContextThemeWrapper
import android.util.AttributeSet
import android.view.View
import io.tylerwalker.chat.R


class FloatingActionsMenu : ViewGroup {

    private var mAddButtonPlusColor: Int = 0
    private var mAddButtonColorNormal: Int = 0
    private var mAddButtonColorPressed: Int = 0
    private var mAddButtonSize: Int = 0
    private var mAddButtonStrokeVisible: Boolean = false
    private var mExpandDirection: Int = 0

    private var mButtonSpacing: Int = 0
    private var mLabelsMargin: Int = 0
    private var mLabelsVerticalOffset: Int = 0

    var isExpanded: Boolean = false
        private set

    private val mExpandAnimation = AnimatorSet().setDuration(ANIMATION_DURATION.toLong())
    private val mCollapseAnimation = AnimatorSet().setDuration(ANIMATION_DURATION.toLong())
    var sendButton: SendFloatingActionButton? = null
        private set
    private val mRotatingDrawable: RotatingDrawable? = null
    private var mMaxButtonWidth: Int = 0
    private var mMaxButtonHeight: Int = 0
    private var mLabelsStyle: Int = 0
    private var mLabelsPosition: Int = 0
    private var mButtonsCount: Int = 0

    var closeDrawable: Drawable? = null
        private set
    var iconDrawable: Drawable? = null
        set(iconDrawable) {
            field = iconDrawable
            sendButton?.setImageDrawable(iconDrawable)
        }

    private var mTouchDelegateGroup: TouchDelegateGroup? = null

    private var mListener: OnFloatingActionsMenuUpdateListener? = null
    private var mIconColorTint: Int = 0

    fun setButtonIconTint(colorTint: Int) {
        mIconColorTint = colorTint
        sendButton?.drawable?.setColorFilter(colorTint, PorterDuff.Mode.SRC_IN)
    }

    interface OnFloatingActionsMenuUpdateListener {
        fun onMenuExpanded()
        fun onMenuCollapsed()
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context, attrs)
    }

    private fun init(context: Context, attributeSet: AttributeSet?) {
        mButtonSpacing = (resources.getDimension(R.dimen.fab_actions_spacing) - resources.getDimension(R.dimen.fab_shadow_radius) - resources.getDimension(R.dimen.fab_shadow_offset)).toInt()
        mLabelsMargin = resources.getDimensionPixelSize(R.dimen.fab_labels_margin)
        mLabelsVerticalOffset = resources.getDimensionPixelSize(R.dimen.fab_shadow_offset)

        mTouchDelegateGroup = TouchDelegateGroup(this)
        touchDelegate = mTouchDelegateGroup

        val attr = context.obtainStyledAttributes(attributeSet, R.styleable.FloatingActionsMenu, 0, 0)
        mAddButtonPlusColor = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonPlusIconColor, getColor(android.R.color.white))
        mAddButtonColorNormal = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorNormal, getColor(android.R.color.holo_blue_dark))
        mAddButtonColorPressed = attr.getColor(R.styleable.FloatingActionsMenu_fab_addButtonColorPressed, getColor(android.R.color.holo_blue_light))
        mAddButtonSize = FloatingActionButton.FabSize.Normal.ordinal
        mAddButtonStrokeVisible = attr.getBoolean(R.styleable.FloatingActionsMenu_fab_addButtonStrokeVisible, true)
        mExpandDirection = attr.getInt(R.styleable.FloatingActionsMenu_fab_expandDirection, EXPAND_UP)
        mLabelsStyle = attr.getResourceId(R.styleable.FloatingActionsMenu_fab_labelStyle, 0)
        mLabelsPosition = attr.getInt(R.styleable.FloatingActionsMenu_fab_labelsPosition, LABELS_ON_LEFT_SIDE)
        attr.recycle()

        if (mLabelsStyle != 0 && expandsHorizontally()) {
            throw IllegalStateException("Action labels in horizontal expand orientation is not supported.")
        }

        closeDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_close_drawable)
        createSendButton(context)
    }

    fun setOnFloatingActionsMenuUpdateListener(listener: OnFloatingActionsMenuUpdateListener) {
        mListener = listener
    }

    private fun expandsHorizontally(): Boolean {
        return mExpandDirection == EXPAND_LEFT || mExpandDirection == EXPAND_RIGHT
    }

    private class RotatingDrawable(drawable: Drawable) : LayerDrawable(arrayOf(drawable)) {

        var rotation: Float = 0.toFloat()
            set(rotation) {
                field = rotation
                invalidateSelf()
            }

        override fun draw(canvas: Canvas) {
            canvas.save()
            canvas.rotate(rotation, bounds.centerX().toFloat(), bounds.centerY().toFloat())
            super.draw(canvas)
            canvas.restore()
        }
    }

    private fun createSendButton(context: Context) {
        sendButton = object : SendFloatingActionButton(context) {
            override fun updateBackground() {
                mPlusColor = mAddButtonPlusColor
                mColorNormal = darkenColor(1.0, mColorNormal)
                mColorPressed = darkenColor(0.8, mColorNormal)
                mStrokeVisible = mAddButtonStrokeVisible
                super.updateBackground()
            }
        }

        sendButton!!.id = R.id.fab_expand_menu_button
        sendButton!!.size = FloatingActionButton.FabSize.getSizeForOrdinal(mAddButtonSize)
        addView(sendButton, super.generateDefaultLayoutParams())
        mButtonsCount++
    }

    fun addButton(button: FloatingActionButton) {
        addView(button, mButtonsCount - 1)
        mButtonsCount++

        if (mLabelsStyle != 0) {
            createLabels()
        }
    }

    fun removeButton(button: FloatingActionButton) {
        removeView(button.labelView)
        removeView(button)
        button.setTag(R.id.fab_label, null)
        mButtonsCount--
    }

    private fun getColor(@ColorRes id: Int): Int {
        return resources.getColor(id)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        measureChildren(widthMeasureSpec, heightMeasureSpec)

        var width = 0
        var height = 0

        mMaxButtonWidth = 0
        mMaxButtonHeight = 0
        var maxLabelWidth = 0

        for (i in 0 until mButtonsCount) {
            val child = getChildAt(i)

            if (child.visibility === View.GONE) {
                continue
            }

            when (mExpandDirection) {
                EXPAND_UP, EXPAND_DOWN -> {
                    mMaxButtonWidth = Math.max(mMaxButtonWidth, child.measuredWidth)
                    height += child.measuredHeight
                }
                EXPAND_LEFT, EXPAND_RIGHT -> {
                    width += child.measuredWidth
                    mMaxButtonHeight = Math.max(mMaxButtonHeight, child.measuredHeight)
                }
            }

            if (!expandsHorizontally()) {
                val label = child.getTag(R.id.fab_label) as TextView?
                if (label != null) {
                    maxLabelWidth = Math.max(maxLabelWidth, label.measuredWidth)
                }
            }
        }

        if (!expandsHorizontally()) {
            width = mMaxButtonWidth + if (maxLabelWidth > 0) maxLabelWidth + mLabelsMargin else 0
        } else {
            height = mMaxButtonHeight
        }

        when (mExpandDirection) {
            EXPAND_UP, EXPAND_DOWN -> {
                height += mButtonSpacing * (mButtonsCount - 1)
                height = adjustForOvershoot(height)
            }
            EXPAND_LEFT, EXPAND_RIGHT -> {
                width += mButtonSpacing * (mButtonsCount - 1)
                width = adjustForOvershoot(width)
            }
        }

        setMeasuredDimension(width, height)
    }

    private fun adjustForOvershoot(dimension: Int): Int {
        return dimension * 12 / 10
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        when (mExpandDirection) {
            EXPAND_UP, EXPAND_DOWN -> {
                val expandUp = mExpandDirection == EXPAND_UP

                if (changed) {
                    mTouchDelegateGroup!!.clearTouchDelegates()
                }

                val addButtonY = if (expandUp) b - t - sendButton!!.getMeasuredHeight() else 0
                // Ensure mSendButton is centered on the line where the buttons should be
                val buttonsHorizontalCenter = if (mLabelsPosition == LABELS_ON_LEFT_SIDE)
                    r - l - mMaxButtonWidth / 2
                else
                    mMaxButtonWidth / 2
                val addButtonLeft = buttonsHorizontalCenter - sendButton!!.getMeasuredWidth() / 2
                sendButton!!.layout(addButtonLeft, addButtonY, addButtonLeft + sendButton!!.getMeasuredWidth(), addButtonY + sendButton!!.getMeasuredHeight())

                val labelsOffset = mMaxButtonWidth / 2 + mLabelsMargin
                val labelsXNearButton = if (mLabelsPosition == LABELS_ON_LEFT_SIDE)
                    buttonsHorizontalCenter - labelsOffset
                else
                    buttonsHorizontalCenter + labelsOffset

                var nextY = if (expandUp)
                    addButtonY - mButtonSpacing
                else
                    addButtonY + sendButton!!.getMeasuredHeight() + mButtonSpacing

                for (i in mButtonsCount - 1 downTo 0) {
                    val child = getChildAt(i)

                    if (child === sendButton || child.visibility === View.GONE) continue

                    val childX = buttonsHorizontalCenter - child.measuredWidth / 2
                    val childY = if (expandUp) nextY - child.measuredHeight else nextY
                    child.layout(childX, childY, childX + child.measuredWidth, childY + child.measuredHeight)

                    val collapsedTranslation = (addButtonY - childY).toFloat()
                    val expandedTranslation = 0f

                    child.translationY = if (isExpanded) expandedTranslation else collapsedTranslation
                    child.alpha = if (isExpanded) 1f else 0f

                    val params = child.layoutParams as LayoutParams
                    params.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation)
                    params.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation)
                    params.setAnimationsTarget(child)

                    val label = child.getTag(R.id.fab_label) as View
                    if (label != null) {
                        val labelXAwayFromButton = if (mLabelsPosition == LABELS_ON_LEFT_SIDE)
                            labelsXNearButton - label!!.getMeasuredWidth()
                        else
                            labelsXNearButton + label!!.getMeasuredWidth()

                        val labelLeft = if (mLabelsPosition == LABELS_ON_LEFT_SIDE)
                            labelXAwayFromButton
                        else
                            labelsXNearButton

                        val labelRight = if (mLabelsPosition == LABELS_ON_LEFT_SIDE)
                            labelsXNearButton
                        else
                            labelXAwayFromButton

                        val labelTop = childY - mLabelsVerticalOffset + (child.measuredHeight - label!!.getMeasuredHeight()) / 2

                        label!!.layout(labelLeft, labelTop, labelRight, labelTop + label!!.getMeasuredHeight())

                        val touchArea = Rect(
                                Math.min(childX, labelLeft),
                                childY - mButtonSpacing / 2,
                                Math.max(childX + child.measuredWidth, labelRight),
                                childY + child.measuredHeight + mButtonSpacing / 2)
                        mTouchDelegateGroup!!.addTouchDelegate(TouchDelegate(touchArea, child))

                        label!!.setTranslationY(if (isExpanded) expandedTranslation else collapsedTranslation)
                        label!!.setAlpha(if (isExpanded) 1f else 0f)

                        val labelParams = label!!.getLayoutParams() as LayoutParams
                        labelParams.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation)
                        labelParams.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation)
                        labelParams.setAnimationsTarget(label)
                    }

                    nextY = if (expandUp)
                        childY - mButtonSpacing
                    else
                        childY + child.measuredHeight + mButtonSpacing
                }
            }

            EXPAND_LEFT, EXPAND_RIGHT -> {
                val expandLeft = mExpandDirection == EXPAND_LEFT

                val addButtonX = if (expandLeft) r - l - sendButton!!.getMeasuredWidth() else 0
                // Ensure mSendButton is centered on the line where the buttons should be
                val addButtonTop = b - t - mMaxButtonHeight + (mMaxButtonHeight - sendButton!!.getMeasuredHeight()) / 2
                sendButton!!.layout(addButtonX, addButtonTop, addButtonX + sendButton!!.getMeasuredWidth(), addButtonTop + sendButton!!.getMeasuredHeight())

                var nextX = if (expandLeft)
                    addButtonX - mButtonSpacing
                else
                    addButtonX + sendButton!!.getMeasuredWidth() + mButtonSpacing

                for (i in mButtonsCount - 1 downTo 0) {
                    val child = getChildAt(i)

                    if (child === sendButton || child.visibility === View.GONE) continue

                    val childX = if (expandLeft) nextX - child.measuredWidth else nextX
                    val childY = addButtonTop + (sendButton!!.getMeasuredHeight() - child.measuredHeight) / 2
                    child.layout(childX, childY, childX + child.measuredWidth, childY + child.measuredHeight)

                    val collapsedTranslation = (addButtonX - childX).toFloat()
                    val expandedTranslation = 0f

                    child.translationX = if (isExpanded) expandedTranslation else collapsedTranslation
                    child.alpha = if (isExpanded) 1f else 0f

                    val params = child.layoutParams as LayoutParams
                    params.mCollapseDir.setFloatValues(expandedTranslation, collapsedTranslation)
                    params.mExpandDir.setFloatValues(collapsedTranslation, expandedTranslation)
                    params.setAnimationsTarget(child)

                    nextX = if (expandLeft)
                        childX - mButtonSpacing
                    else
                        childX + child.measuredWidth + mButtonSpacing
                }
            }
        }
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams(super.generateDefaultLayoutParams())
    }

    override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams {
        return LayoutParams(super.generateLayoutParams(attrs))
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return LayoutParams(super.generateLayoutParams(p))
    }

    private inner class LayoutParams(source: ViewGroup.LayoutParams) : ViewGroup.LayoutParams(source) {

        internal val mExpandDir = ObjectAnimator()
        internal val mExpandAlpha = ObjectAnimator()
        internal val mCollapseDir = ObjectAnimator()
        internal val mCollapseAlpha = ObjectAnimator()
        internal var animationsSetToPlay: Boolean = false

        init {

            mExpandDir.interpolator = sExpandInterpolator
            mExpandAlpha.interpolator = sAlphaExpandInterpolator
            mCollapseDir.interpolator = sCollapseInterpolator
            mCollapseAlpha.interpolator = sCollapseInterpolator

            mCollapseAlpha.setProperty(View.ALPHA)
            mCollapseAlpha.setFloatValues(1f, 0f)

            mExpandAlpha.setProperty(View.ALPHA)
            mExpandAlpha.setFloatValues(0f, 1f)

            when (mExpandDirection) {
                EXPAND_UP, EXPAND_DOWN -> {
                    mCollapseDir.setProperty(View.TRANSLATION_Y)
                    mExpandDir.setProperty(View.TRANSLATION_Y)
                }
                EXPAND_LEFT, EXPAND_RIGHT -> {
                    mCollapseDir.setProperty(View.TRANSLATION_X)
                    mExpandDir.setProperty(View.TRANSLATION_X)
                }
            }
        }

        fun setAnimationsTarget(view: View?) {
            mCollapseAlpha.target = view
            mCollapseDir.target = view
            mExpandAlpha.target = view
            mExpandDir.target = view

            // Now that the animations have targets, set them to be played
            if (!animationsSetToPlay) {
                addLayerTypeListener(mExpandDir, view)
                addLayerTypeListener(mCollapseDir, view)

                mCollapseAnimation.play(mCollapseAlpha)
                mCollapseAnimation.play(mCollapseDir)
                mExpandAnimation.play(mExpandAlpha)
                mExpandAnimation.play(mExpandDir)
                animationsSetToPlay = true
            }
        }

        private fun addLayerTypeListener(animator: Animator, view: View?) {
            animator.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view!!.setLayerType(View.LAYER_TYPE_NONE, null)
                }

                override fun onAnimationStart(animation: Animator) {
                    view!!.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                }
            })
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        bringChildToFront(sendButton)
        mButtonsCount = childCount

        if (mLabelsStyle != 0) {
            createLabels()
        }
    }

    private fun createLabels() {
        val context = ContextThemeWrapper(context, mLabelsStyle)

        for (i in 0 until mButtonsCount) {
            val button = getChildAt(i) as FloatingActionButton
            val title = button.title

            if (button == sendButton || title == null ||
                    button.getTag(R.id.fab_label) != null)
                continue

            val label = TextView(context)
            label.setTextAppearance(getContext(), mLabelsStyle)
            label.text = button.title
            addView(label)

            button.setTag(R.id.fab_label, label)
        }
    }

    fun collapse() {
        collapse(false)
    }

    fun collapseImmediately() {
        collapse(true)
    }

    private fun collapse(immediately: Boolean) {
        if (isExpanded) {
            isExpanded = false
            mTouchDelegateGroup!!.setEnabled(false)
            mCollapseAnimation.duration = (if (immediately) 0 else ANIMATION_DURATION).toLong()
            mCollapseAnimation.start()
            mExpandAnimation.cancel()

            if (iconDrawable != null) {
                iconDrawable!!.setColorFilter(mIconColorTint, PorterDuff.Mode.SRC_IN)
                sendButton!!.setImageDrawable(iconDrawable)
            }

            if (mListener != null) {
                mListener!!.onMenuCollapsed()
            }
        }
    }

    fun toggle() {
        if (isExpanded) {
            collapse()
        } else {
            expand()
        }
    }

    fun expand() {
        if (!isExpanded) {
            isExpanded = true
            mTouchDelegateGroup!!.setEnabled(true)
            mCollapseAnimation.cancel()
            mExpandAnimation.start()

            if (closeDrawable != null) {
                closeDrawable!!.setColorFilter(mIconColorTint, PorterDuff.Mode.SRC_IN)
                sendButton!!.setImageDrawable(closeDrawable)
            }

            if (mListener != null) {
                mListener!!.onMenuExpanded()
            }
        }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)

        sendButton!!.setEnabled(enabled)
    }

    public override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        val savedState = SavedState(superState)
        savedState.mExpanded = isExpanded

        return savedState
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            val savedState = state as SavedState
            isExpanded = savedState.mExpanded
            mTouchDelegateGroup!!.setEnabled(isExpanded)

            if (mRotatingDrawable != null) {
                mRotatingDrawable.rotation = if (isExpanded) EXPANDED_PLUS_ROTATION else COLLAPSED_PLUS_ROTATION
            }

            super.onRestoreInstanceState(savedState.getSuperState())
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    class SavedState : View.BaseSavedState {
        var mExpanded: Boolean = false

        constructor(parcel: Parcelable) : super(parcel) {}

        private constructor(`in`: Parcel) : super(`in`) {
            mExpanded = `in`.readInt() == 1
        }

        override fun writeToParcel(@NonNull out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(if (mExpanded) 1 else 0)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState> {

                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState> {
                    return arrayOf()
                }
            }
        }
    }

    companion object {
        val EXPAND_UP = 0
        val EXPAND_DOWN = 1
        val EXPAND_LEFT = 2
        val EXPAND_RIGHT = 3

        val LABELS_ON_LEFT_SIDE = 0
        val LABELS_ON_RIGHT_SIDE = 1

        private val ANIMATION_DURATION = 300
        private val COLLAPSED_PLUS_ROTATION = 0f
        private val EXPANDED_PLUS_ROTATION = 90f + 45f

        private val sExpandInterpolator = OvershootInterpolator()
        private val sCollapseInterpolator = DecelerateInterpolator(3f)
        private val sAlphaExpandInterpolator = DecelerateInterpolator()

        internal fun darkenColor(factor: Double, color: Int): Int {
            val a = Color.alpha(color)
            val r = Color.red(color)
            val g = Color.green(color)
            val b = Color.blue(color)

            return Color.argb(a,
                    Math.max((r * factor).toInt(), 0),
                    Math.max((g * factor).toInt(), 0),
                    Math.max((b * factor).toInt(), 0))
        }
    }
}
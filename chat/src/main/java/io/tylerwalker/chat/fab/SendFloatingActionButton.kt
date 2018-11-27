package io.tylerwalker.chat.fab

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.annotation.ColorRes
import android.util.AttributeSet
import io.tylerwalker.chat.R


open class SendFloatingActionButton : FloatingActionButton {
    internal var mPlusColor: Int = 0

    /**
     * @return the current Color of plus icon.
     */
    var plusColor: Int
        get() = mPlusColor
        set(color) {
            if (mPlusColor != color) {
                mPlusColor = color
                updateBackground()
            }
        }

    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun init(context: Context, attributeSet: AttributeSet?) {
        val attr = context.obtainStyledAttributes(attributeSet, R.styleable.SendFloatingActionButton, 0, 0)
        mPlusColor = attr.getColor(R.styleable.SendFloatingActionButton_fab_plusIconColor, getColor(android.R.color.white))
        attr.recycle()

        super.init(context, attributeSet)
    }

    fun setPlusColorResId(@ColorRes plusColor: Int) {
        this@SendFloatingActionButton.plusColor = getColor(plusColor)
    }

    override fun setIcon(@DrawableRes icon: Int) {
        throw UnsupportedOperationException("Use FloatingActionButton if you want to use custom icon")
    }
}
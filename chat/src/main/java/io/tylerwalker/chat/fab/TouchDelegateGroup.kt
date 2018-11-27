package io.tylerwalker.chat.fab

import android.graphics.Rect
import android.support.annotation.NonNull
import android.view.MotionEvent
import android.view.TouchDelegate
import android.view.View


class TouchDelegateGroup(uselessHackyView: View) : TouchDelegate(USELESS_HACKY_RECT, uselessHackyView) {
    private val mTouchDelegates = ArrayList<TouchDelegate>()
    private var mCurrentTouchDelegate: TouchDelegate? = null
    private var mEnabled: Boolean = false

    fun addTouchDelegate(@NonNull touchDelegate: TouchDelegate) {
        mTouchDelegates.add(touchDelegate)
    }

    fun removeTouchDelegate(touchDelegate: TouchDelegate) {
        mTouchDelegates.remove(touchDelegate)
        if (mCurrentTouchDelegate === touchDelegate) {
            mCurrentTouchDelegate = null
        }
    }

    fun clearTouchDelegates() {
        mTouchDelegates.clear()
        mCurrentTouchDelegate = null
    }

    override fun onTouchEvent(@NonNull event: MotionEvent): Boolean {
        if (!mEnabled) return false

        var delegate: TouchDelegate? = null

        when (event.action) {
            MotionEvent.ACTION_DOWN -> for (i in 0 until mTouchDelegates.size) {
                val touchDelegate = mTouchDelegates[i]
                if (touchDelegate.onTouchEvent(event)) {
                    mCurrentTouchDelegate = touchDelegate
                    return true
                }
            }

            MotionEvent.ACTION_MOVE -> delegate = mCurrentTouchDelegate

            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                delegate = mCurrentTouchDelegate
                mCurrentTouchDelegate = null
            }
        }

        return delegate != null && delegate.onTouchEvent(event)
    }

    fun setEnabled(enabled: Boolean) {
        mEnabled = enabled
    }

    companion object {
        private val USELESS_HACKY_RECT = Rect()
    }
}
package com.example.tylerwalker.buyyouadrink.view

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import jp.wasabeef.blurry.Blurry

class BlurryView(context: Context, attrs: AttributeSet): ViewGroup(context, attrs) {
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val child = getChildAt(0) as ImageView

        if (child.visibility == View.GONE) return

        val childWidth = this.measuredWidth
        val childHeight = this.measuredHeight

        child.layout(0, 0, childWidth, childHeight)

        // Blurry
        Blurry.with(context)
                .sampling(1)
                .radius(25)
                .capture(this)
                .into(child)
    }

}
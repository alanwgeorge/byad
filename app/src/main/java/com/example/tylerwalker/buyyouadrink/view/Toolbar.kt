package com.example.tylerwalker.buyyouadrink.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.constraint.ConstraintSet
import android.util.AttributeSet
import android.view.View
import com.example.tylerwalker.buyyouadrink.R

class Toolbar: ConstraintLayout {
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        val view = inflate(context, R.layout.toolbar, null)
        addView(view, 0)
    }
}
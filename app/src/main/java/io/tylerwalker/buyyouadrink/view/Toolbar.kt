package io.tylerwalker.buyyouadrink.view

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import io.tylerwalker.buyyouadrink.R

class Toolbar: ConstraintLayout {
    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        val view = inflate(context, R.layout.toolbar, null)
        addView(view, 0)
    }
}
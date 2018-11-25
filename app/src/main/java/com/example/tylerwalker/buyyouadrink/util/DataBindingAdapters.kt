package com.example.tylerwalker.buyyouadrink.util

import android.databinding.BindingAdapter
import android.graphics.drawable.Drawable
import android.widget.ImageView

@BindingAdapter("android:drawable")
fun ImageView.bindImageDrawable(drawable: Drawable) {
    this.setImageDrawable(drawable)
}

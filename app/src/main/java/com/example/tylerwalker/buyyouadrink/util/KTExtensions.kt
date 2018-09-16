package com.example.tylerwalker.buyyouadrink.util

import android.content.res.Resources
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.util.Base64
import android.util.Log


fun Bitmap.toEncodedString(): String? = try {
    val baos = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.WEBP, 1, baos)
    val b = baos.toByteArray()
    Base64.encodeToString(b, Base64.DEFAULT)
} catch (e: Exception) {
    Log.d("KTExtensions", "$e")
    null
}

fun Bitmap.toRoundedDrawable(resources: Resources): Drawable {
    val roundDrawable = RoundedBitmapDrawableFactory.create(resources, this)
    roundDrawable.isCircular = true
    return roundDrawable
}


fun String.toBitmap(): Bitmap? = try {
        val encoded = Base64.decode(this, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(encoded, 0, encoded.size)
    } catch (e: Exception) {
        Log.d("KTExtensions", "$e")
        null
    }

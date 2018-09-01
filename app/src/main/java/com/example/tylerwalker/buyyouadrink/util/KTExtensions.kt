package com.example.tylerwalker.buyyouadrink.util

import android.content.res.Resources
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import java.util.*
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.widget.ImageView


fun Bitmap.toEncodedString(): String? {
     return try {
         val baos = ByteArrayOutputStream()
         this.compress(Bitmap.CompressFormat.WEBP, 1, baos)
         val b = baos.toByteArray()
         Base64.getEncoder().encodeToString(b)
     } catch (e: NullPointerException) {
         null
     } catch (e: OutOfMemoryError) {
         null
     }
}

fun Bitmap.toRoundedDrawable(resources: Resources): Drawable {
    val roundDrawable = RoundedBitmapDrawableFactory.create(resources, this)
    roundDrawable.isCircular = true
    return roundDrawable
}

fun String.toBitmap(): Bitmap? {
    return try {
        val encodeByte = Base64.getDecoder().decode(this)
        return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
    } catch (e: NullPointerException) {
        e.message
        return null
    } catch (e: OutOfMemoryError) {
        return null
    }
}

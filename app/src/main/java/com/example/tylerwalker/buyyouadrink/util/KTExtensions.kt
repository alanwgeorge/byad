package com.example.tylerwalker.buyyouadrink.util

import android.content.res.Resources
import android.graphics.*
import java.io.ByteArrayOutputStream
import android.graphics.drawable.Drawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.util.Base64
import android.util.Log
import com.example.tylerwalker.buyyouadrink.model.Coordinates
import java.lang.Math.*
import android.opengl.ETC1.getHeight
import android.opengl.ETC1.getWidth
import android.graphics.Bitmap




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


fun Bitmap.toDrawable(resources: Resources): Drawable {
    return RoundedBitmapDrawableFactory.create(resources, this)
}


fun String.toBitmap(): Bitmap? = try {
        val encoded = Base64.decode(this, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(encoded, 0, encoded.size)
    } catch (e: Exception) {
        Log.d("KTExtensions", "$e")
        null
    }


const val R = 6372.8 // in kilometers

fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val λ1 = toRadians(lat1)
    val λ2 = toRadians(lat2)
    val Δλ = toRadians(lat2 - lat1)
    val Δφ = toRadians(lon2 - lon1)
    return 2 * R * asin(sqrt(pow(sin(Δλ / 2), 2.0) + pow(sin(Δφ / 2), 2.0) * cos(λ1) * cos(λ2)))
}

fun Coordinates.distanceTo(other: Coordinates): Float {
    val (lat1, lon1) = this
    val (lat2, lon2) = other
    return haversine(lat1.toDouble(), lon1.toDouble(), lat2.toDouble(), lon2.toDouble()).toFloat()
}

fun Bitmap.toRounded(): Bitmap {
    val output: Bitmap = if (this.width > this.height) {
        Bitmap.createBitmap(this.height, this.height, Bitmap.Config.ARGB_8888)
    } else {
        Bitmap.createBitmap(this.width, this.width, Bitmap.Config.ARGB_8888)
    }

    val canvas = Canvas(output)

    val color = -0xbdbdbe
    val paint = Paint()
    val rect = Rect(0, 0, this.width, this.height)

    val r: Float = if (this.width > this.height) {
        (this.height / 2).toFloat()
    } else {
        (this.width / 2).toFloat()
    }

    paint.isAntiAlias = true
    canvas.drawARGB(0, 0, 0, 0)
    paint.color = color
    canvas.drawCircle(r, r, r, paint)
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, rect, rect, paint)
    return output
}

fun Bitmap.rotate(): Bitmap {
    val matrix = Matrix()

    matrix.postRotate(90F)

    val scaledBitmap = Bitmap.createScaledBitmap(this, width, height, true)

    return Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.width, scaledBitmap.height, matrix, true)
}
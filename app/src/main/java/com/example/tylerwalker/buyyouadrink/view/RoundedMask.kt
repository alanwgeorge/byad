package com.example.tylerwalker.buyyouadrink.view

import android.content.Context
import android.graphics.*
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.example.tylerwalker.buyyouadrink.R

class RoundedMask(context: Context, attrs: AttributeSet): View(context, attrs) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val white = ResourcesCompat.getColor(resources, android.R.color.white, null)
    private val black = ResourcesCompat.getColor(resources, android.R.color.black, null)
    private val gray = ResourcesCompat.getColor(resources, R.color.lightgray, null)
    var bitmap: Bitmap? = null
    private val rect = Rect()
    private val bitmapRect = Rect()
    private val center = PaintCoordinates((measuredWidth/2).toFloat(), (measuredHeight/2).toFloat())
    private val bounds = PaintBounds(measuredWidth.toFloat(), measuredHeight.toFloat())

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        center.left = (measuredWidth/2).toFloat()
        center.top = (measuredHeight/2).toFloat()

        bounds.width = measuredWidth.toFloat()
        bounds.height = measuredHeight.toFloat()

        val arcTop: Float = center.top + center.top / 4

        Log.d("onDraw() center", "${center.left}, ${center.top}")
        Log.d("onDraw() bounds", "${bounds.width}, ${bounds.height}")
        Log.d("onDraw() arcTop", "${arcTop}")

//        canvas?.drawColor(white)

        paint.color = black

//        canvas?.drawArc(0F, arcTop, bounds.width, bounds.height, 0F, 180F, true, paint)
//        canvas?.drawRect(0F, 0F, bounds.width, (bounds.height - (bounds.height - arcTop)/ 2) + .5F, paint)
//
//        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)

        rect.set(0, 0, bounds.width.toInt(), bounds.height.toInt())
        bitmapRect.set(0, 0, bitmap?.width ?: 0, bitmap?.height ?: 0)

        if (bitmap == null) {
            paint.color = gray

            canvas?.drawArc(0F, arcTop, bounds.width, bounds.height, 0F, 180F, true, paint)
            canvas?.drawRect(0F, 0F, bounds.width, (bounds.height - (bounds.height - arcTop)/ 2) + .5F, paint)

        }

        bitmap?.let {
            Log.d("RoundedMask", "drawing bitmap: $bitmap")
            canvas?.drawBitmap(bitmap, rect, rect, paint)
            return
        }
    }

    fun updateBitmap(_bitmap: Bitmap?) {
        this.bitmap = _bitmap
        invalidate()
    }
}

data class PaintCoordinates(var left: Float, var top: Float)
data class PaintBounds(var width: Float, var height: Float)
package com.example.tylerwalker.buyyouadrink.view

import android.content.Context
import android.graphics.*
import android.support.v4.content.res.ResourcesCompat
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.example.tylerwalker.buyyouadrink.R
import com.example.tylerwalker.buyyouadrink.R.drawable.gray

class RoundedMask(context: Context, attrs: AttributeSet): View(context, attrs) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    var bitmap: Bitmap? = null

    private val rect = Rect()
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

        rect.set(0, 0, bounds.width.toInt(), bounds.height.toInt())

        if (bitmap == null) {
            paint.color = primary

            canvas?.drawArc(0F, arcTop, bounds.width, bounds.height, 0F, 180F, true, paint)
            canvas?.drawRect(0F, 0F, bounds.width, (bounds.height - (bounds.height - arcTop)/ 2) + .5F, paint)

        } else {
            val tempBitmap = Bitmap.createBitmap(bounds.width.toInt(), bounds.height.toInt(), Bitmap.Config.ARGB_8888)
            val tempCanvas = Canvas(tempBitmap)

            paint.color = black
            tempCanvas.drawRect(0F, 0F, bounds.width, (bounds.height - (bounds.height - arcTop)/ 2) + .5F, paint)
            tempCanvas.drawArc(0F, arcTop, bounds.width, bounds.height, 0F, 180F, true, paint)

            paint.xfermode = srcIn
            tempCanvas.drawBitmap(bitmap!!, 0F, 0F, paint)

            paint.xfermode = null

            canvas?.drawBitmap(tempBitmap, 0F, 0F, paint)
        }
    }

    fun updateBitmap(_bitmap: Bitmap?) {
        this.bitmap = _bitmap
        invalidate()
    }

    private val black = ResourcesCompat.getColor(resources, android.R.color.black, null)
    private val primary = ResourcesCompat.getColor(resources, R.color.colorPrimary, null)
    private val srcIn = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
}

data class PaintCoordinates(var left: Float, var top: Float)
data class PaintBounds(var width: Float, var height: Float)
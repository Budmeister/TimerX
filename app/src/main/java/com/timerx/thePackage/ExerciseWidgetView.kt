package com.timerx.thePackage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import kotlin.math.min

class ExerciseWidgetView(
    context: Context,
    attrs: AttributeSet?,
): View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 55.0f
        typeface = Typeface.create("Times New Roman",  Typeface.BOLD)
    }

    init{
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.ExerciseWidgetView){
            text = getString(R.styleable.ExerciseWidgetView_text)?: ""
            color = getColor(R.styleable.ExerciseWidgetView_color, Color.RED)
            textColor = getColor(R.styleable.ExerciseWidgetView_textColor,
                    if(Color.red(color) + Color.green(color) + Color.blue(color) > 0xff / 4 * 3)
                        Color.BLACK
                    else
                        Color.WHITE
                )
        }
    }

    private var text = ""
    private var textY = 0f
    private var icon: Drawable? = null
    private var iconLeft = 0
    private var iconRight = 0
    private var iconTop = 0
    private var iconBottom = 0
    private var color = Color.RED
    private var textColor = Color.WHITE
    private var radius = 50f
    private var isRunning = false
    private var runningBorder = 20f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        updateIconSize()
        textY = h * 3 / 4 + paint.textSize / 2
    }

    private fun updateIconSize(){
        var iconSize = min(width, height) / 2
        iconLeft = (width - iconSize) / 2
        iconTop = (height - iconSize) / 2
        iconRight = iconLeft + iconSize
        iconBottom = iconTop + iconSize
        icon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)

    }

    fun setText(text: String) {
        this.text = text
    }

    fun setColor(color: Int){
        this.color = color
    }

    fun setIsRunning(running: Boolean){
        isRunning = running
        invalidate()
    }

    fun isRunning(): Boolean {
        return isRunning
    }

    fun setIcon(icon: Drawable?){
        this.icon = icon
        updateIconSize()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (canvas == null)
            return
        if(isRunning){
            paint.color = Color.BLACK
            canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radius, radius, paint)
            paint.color = color
            canvas.drawRoundRect(runningBorder, runningBorder, width.toFloat() - runningBorder, height.toFloat() - runningBorder,
                radius - runningBorder, radius - runningBorder, paint)
        }else{
            paint.color = color
            canvas.drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), radius, radius, paint)
        }
        icon?.draw(canvas)
        paint.color = textColor
        canvas.drawText(text, (width / 2).toFloat(), textY.toFloat(), paint)
    }

}
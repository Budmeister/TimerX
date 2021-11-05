package com.timerx.thePackage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes

class CalendarView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object{
        const val MILLISECONDS_PER_DAY = 1000L * 60 * 60 * 24

        val dayOfWeekInitials = arrayOf(
            "S",
            "M",
            "T",
            "W",
            "R",
            "F",
            "S"
        )

        const val daysPerWeek = 7
    }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.LEFT
        textSize = 55.0f
        typeface = Typeface.create("Times New Roman",  Typeface.BOLD)
    }

    init{
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.CalendarView){
            // put attributes here
//            backColor = getColor(R.styleable.CalendarView_backColor, backColor)
//            accentColor = getColor(R.styleable.CalendarView_accentColor, accentColor)
//            margin = getFloat(R.styleable.CalendarView_margin, margin)
//            borderColor = getColor(R.styleable.CalendarView_borderColor, borderColor)
            dayInitialsFontSize = getFloat(R.styleable.CalendarView_dayInitialsFontSize, dayInitialsFontSize)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updatePlotWidth()
        updatePlotHeight()
    }


    override fun performClick(): Boolean {
        if(super.performClick())
            return true
        return true
    }

    var accentColor = Color.LTGRAY        // attr
    var backColor = Color.WHITE       // attr
    var borderColor = Color.LTGRAY      // attr
    var margin = 20f                    // attr

    private var plotWidth = 0f
    private var plotHeight = 0f

    private var data: MutableList<TimeElement> = mutableListOf()
    var colors: HashMap<String, Int> = HashMap()
    var startTime = 0L
    var endTime = MILLISECONDS_PER_DAY
    var weekStart = -1L         // -1 = plot all elements mod (MILLISECONDS_PER_DAY * 7)
    var showDayInitials = true

    var dayInitialsFontSize = 55.0f

    fun setData(data: MutableList<TimeElement>){
        this.data = data
        invalidate()
    }

    fun addElement(element: TimeElement){
        data.add(element)
    }

    private fun updatePlotWidth() {
        plotWidth = width - margin * 2
    }

    private fun updatePlotHeight(){
        plotHeight = height - margin * 2
        if(showDayInitials)
            plotHeight-=dayInitialsFontSize
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = borderColor
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.color = backColor
        canvas.drawRect(margin, margin,
            width - margin, height - margin, paint)

        // grid and day initials
        paint.color = accentColor
        for(i in 0 until daysPerWeek + 1) {
            canvas.drawLine(
                margin + i * plotWidth / daysPerWeek, margin,
                margin + i * plotWidth / daysPerWeek, margin + plotHeight, paint
            )
            paint.textSize = dayInitialsFontSize
            if(i < daysPerWeek && showDayInitials)
                canvas.drawText(dayOfWeekInitials[i], margin + (i + 0.5f) * plotWidth / daysPerWeek,
                    plotHeight + dayInitialsFontSize, paint)
        }
        canvas.drawLine(margin, margin, plotWidth - margin, margin, paint)
        canvas.drawLine(margin, margin + plotHeight, plotWidth - margin,
            margin + plotHeight, paint)

        // exercises
        for(element in data){
            val day =
                if(weekStart == -1L)
                    ((element.startTime() % (MILLISECONDS_PER_DAY * daysPerWeek)) / MILLISECONDS_PER_DAY).toInt()
                else
                    ((element.startTime() - weekStart) / MILLISECONDS_PER_DAY).toInt()
            if(day < 0 || day >= daysPerWeek)
                continue
            val x = (day * plotWidth / daysPerWeek).toInt()// TODO finish rendering CalendarView
        }

    }

}
package custom_views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.timerx.thePackage.R
import com.timerx.thePackage.TimeElement
import java.util.*

class CalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object{
        const val MILLISECONDS_PER_SECOND = 1000L
        const val MILLISECONDS_PER_MINUTE = MILLISECONDS_PER_SECOND * 60
        const val MILLISECONDS_PER_HOUR = MILLISECONDS_PER_MINUTE * 60
        const val MILLISECONDS_PER_DAY = MILLISECONDS_PER_HOUR * 24

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
        textAlign = Paint.Align.CENTER
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
    var colorProvider: (TimeElement) -> Int = { te: TimeElement -> Color.BLACK }
//    var startTime = 0L
//    var endTime = MILLISECONDS_PER_DAY
//    var weekStart = -1L         // -1 = plot all elements mod (MILLISECONDS_PER_DAY * 7)
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
            plotHeight-=dayInitialsFontSize * 1.5f
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
                canvas.drawText(
                    dayOfWeekInitials[i], margin + (i + 0.5f) * plotWidth / daysPerWeek,
                    plotHeight + dayInitialsFontSize * 1.5f, paint)
        }
        val numHoriz = 4
        for(i in 0 until numHoriz + 1)
            canvas.drawLine(
                margin, margin + plotHeight * i / numHoriz,
                width - margin, margin + plotHeight * i / numHoriz, paint
            )

        // exercises
        var overflow = data
        do{
            overflow = renderData(overflow, canvas, paint)
        } while(overflow.isNotEmpty())

    }

    private fun renderData(
        records: MutableList<TimeElement>,
        canvas: Canvas,
        paint: Paint
    ) : MutableList<TimeElement>{
        val overflow = mutableListOf<TimeElement>()

        val w = plotWidth / daysPerWeek
        for(element in records){
            val coords = getCoords(element)
            val x = coords[0]
            val y = coords[1]
            //  w calculated above
            var h = coords[3]

            val cal = Calendar.getInstance()
            cal.timeInMillis = element.startTime()
            if(y + h >= margin + plotHeight) {
                h = plotHeight - y
                cal.timeInMillis+= MILLISECONDS_PER_DAY
                for(field in arrayOf(
                    Calendar.HOUR_OF_DAY,
                    Calendar.MINUTE,
                    Calendar.SECOND,
                    Calendar.MILLISECOND
                ))
                    cal[field] = 0
                val nextDay = cal.timeInMillis
                val new = element.copy()
                new.startTime(nextDay)
                overflow.add(new)
            }
            paint.color = colorProvider.invoke(element)
            canvas.drawRect(x, y, x + w, y + h, paint)
        }
        return overflow
    }

    fun getElementAt(touchX: Float, touchY: Float) : TimeElement?{
        for(element in data) {
            val coords = getCoords(element)
            val x = coords[0]
            val y = coords[1]
            val w = coords[2]
            val h = coords[3]
            if(isIn(touchX, touchY, x, y, w, h))
                return element
        }
        return null
    }

    private fun isIn(qx: Float, qy: Float, x: Float, y: Float, w: Float, h: Float) =
        qx >= x && qx < x + w && qy >= y && qy < y + h

    private fun getCoords(element: TimeElement) : Array<Float>{
        val w = plotWidth / daysPerWeek
        val cal = Calendar.getInstance()
        cal.timeInMillis = element.startTime()
        val day = cal[Calendar.DAY_OF_WEEK] - 1
        val millisecondsThisDay =
            cal[Calendar.MILLISECOND] +
                    cal[Calendar.SECOND] * MILLISECONDS_PER_SECOND +
                    cal[Calendar.MINUTE] * MILLISECONDS_PER_MINUTE +
                    cal[Calendar.HOUR_OF_DAY] * MILLISECONDS_PER_HOUR


        val x = margin + day * plotWidth / daysPerWeek
        val y = margin + millisecondsThisDay * plotHeight / MILLISECONDS_PER_DAY
        var h = element.length() * plotHeight / MILLISECONDS_PER_DAY
        return arrayOf(x, y, w, h)
    }

}
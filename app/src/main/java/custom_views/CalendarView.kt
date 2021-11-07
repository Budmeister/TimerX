package custom_views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.withStyledAttributes
import com.timerx.thePackage.R
import com.timerx.thePackage.TimeElement
import java.lang.Long.max
import java.lang.Long.min
import java.util.*

/**
 * Allows [TimeElement]s to be displayed in a calendar view.
 * Includes customizable start and end times and a now bar.
 * @author Brian Smith
 */
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
        typeface = font
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

    val font = Typeface.create("Times New Roman", Typeface.BOLD)

    private var plotWidth = 0f
    private var plotHeight = 0f

    private var data: MutableList<TimeElement> = mutableListOf()
    var colorProvider: (TimeElement) -> Int = { te: TimeElement -> Color.LTGRAY }
    var recordNameDarkColor = Color.BLACK
    var recordNameLightColor = Color.WHITE
    var recordNameFontSize = 15.0f

    var timeLabelFontSize = 15.0f
    var timeLabelsWidth = 50f
    var startTime = 0L
    var endTime = MILLISECONDS_PER_DAY
    var realStartTime = startTime
    var realEndTime = endTime
    var showDayInitials = true

    var showNowBar = false
    var nowBarColor = Color.BLACK

    var dayInitialsFontSize = 55.0f

    fun setData(data: MutableList<TimeElement>){
        this.data = data
        updateRealStartAndEndTime()
        invalidate()
    }

    fun addElement(element: TimeElement){
        data.add(element)
    }

    private fun updatePlotWidth() {
        plotWidth = width - margin * 2
        plotWidth-=timeLabelsWidth
    }

    private fun updatePlotHeight(){
        plotHeight = height - margin * 2
        if(showDayInitials)
            plotHeight-=dayInitialsFontSize * 1.5f
    }

    private fun updateRealStartAndEndTime() {
        startTime = min(startTime, MILLISECONDS_PER_DAY - MILLISECONDS_PER_HOUR)
        endTime = max(endTime, startTime + MILLISECONDS_PER_HOUR)
        realStartTime = startTime
        realEndTime = endTime
        var i = 0
        while(i < data.size){
            val element = data[i]
            val startTimeThisDay = getMillisecondsThisDay(getCalendar(element.startTime()))
            val endTimeThisDay = startTimeThisDay + element.length()
            Log.d("CalendarView", "startTimeThisDay: $startTimeThisDay, endTimeThisDay: $endTimeThisDay")
            if(startTimeThisDay < realStartTime){
                realStartTime = startTimeThisDay
                if(realStartTime < 0)
                    realStartTime = 0
            }
            if(endTimeThisDay > realEndTime){
                realEndTime = endTimeThisDay
                if(realEndTime > MILLISECONDS_PER_DAY)
                    realEndTime = MILLISECONDS_PER_DAY
            }
            if(endTimeThisDay > MILLISECONDS_PER_DAY){ // overflow
                val first = element.copy()
                val second = element.copy()
                first.length(MILLISECONDS_PER_DAY - startTimeThisDay)
                second.startTime(second.startTime() + first.length())
                data[i] = first
                data.add(second)
            }
            i++
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = borderColor
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.color = backColor
        canvas.drawRect(margin, margin,
            width - margin, height - margin, paint)

        paint.typeface = font

        // day initials
        paint.color = accentColor
        paint.textSize = dayInitialsFontSize
        paint.textAlign = Paint.Align.CENTER
        for(i in 0 until daysPerWeek + 1) {
            val x = getElementX(i)
            canvas.drawLine(
                x, margin,
                x, margin + plotHeight, paint
            )
            if(i < daysPerWeek && showDayInitials)
                canvas.drawText(
                    dayOfWeekInitials[i], x + 0.5f * plotWidth / daysPerWeek,
                    plotHeight + dayInitialsFontSize * 1.5f, paint)
        }

        // horizontal lines
        val cal = Calendar.getInstance()
        cal.timeInMillis = cal.timeInMillis - getMillisecondsThisDay(cal) + realStartTime
        for(field in arrayOf(
            Calendar.MINUTE,
            Calendar.SECOND,
            Calendar.MILLISECOND
        ))
            cal[field] = 0
        cal[Calendar.HOUR_OF_DAY]++
        paint.textSize = timeLabelFontSize
        paint.textAlign = Paint.Align.LEFT
        paint.color = accentColor
        var hour = cal[Calendar.HOUR_OF_DAY]
        var pm: String
        while(hour * MILLISECONDS_PER_HOUR <= realEndTime){
            pm =
                if(hour >= 12)
                    "PM"
                else
                    "AM"
            val y = getElementY(hour * MILLISECONDS_PER_HOUR)
            if(hour == 12)
                paint.color = Color.BLACK
            canvas.drawLine(
                margin, y,
                width - margin, y, paint
            )
            paint.color = accentColor
            canvas.drawText("${(hour + 11) % 12 + 1} $pm", margin + 3, y - 3, paint)
            hour++
        }
        // there should always be a horizontal line at the start time and at the
        // end time
        canvas.drawLine(margin, margin, width - margin, margin, paint)
        canvas.drawLine(
            margin, margin + plotHeight,
            width - margin, margin + plotHeight,
            paint
        )

        // exercises
        renderData(data, canvas, paint)

        // now bar
        if(showNowBar) {
            val nowCal = Calendar.getInstance()
            val nowDay = nowCal[Calendar.DAY_OF_WEEK] - 1
            val nowY = getElementY(getMillisecondsThisDay(nowCal))
            paint.color = nowBarColor
            canvas.drawLine(
                getElementX(nowDay),
                nowY,
                getElementX(nowDay + 1),
                nowY,
                paint
            )
        }

    }

    private fun renderData(
        records: MutableList<TimeElement>,
        canvas: Canvas,
        paint: Paint
    ) {
        paint.textAlign = Paint.Align.LEFT
        paint.textSize = recordNameFontSize
        val w = plotWidth / daysPerWeek
        for(element in records){
            val coords = getCoords(element)
            val x = coords[0]
            val y = coords[1]
            //  w calculated above
            var h = coords[3]

            val cal = Calendar.getInstance()
            cal.timeInMillis = element.startTime()
            val color = colorProvider.invoke(element)
            paint.color = color
            canvas.drawRect(x, y, x + w, y + h, paint)
            if(h >= recordNameFontSize) {
                paint.color =
                    if(isLight(color))
                        recordNameDarkColor
                    else
                        recordNameLightColor
                canvas.drawText(element.name(), x + 3, y + recordNameFontSize, paint)
            }
        }
    }

    // using HSP; see http://alienryderflex.com/hsp.html
    private fun isLight(color: Int): Boolean{
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        val hsp = kotlin.math.sqrt(0.299 * (r * r) + 0.587 * (g * g) + 0.114 * (b * b))
        return hsp > 127.5
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

    private fun getCalendar(time: Long): Calendar{
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        return cal
    }

    private fun getMillisecondsThisDay(cal: Calendar): Long{
        val millisecondsThisDay =
            cal[Calendar.MILLISECOND] +
                    cal[Calendar.SECOND] * MILLISECONDS_PER_SECOND +
                    cal[Calendar.MINUTE] * MILLISECONDS_PER_MINUTE +
                    cal[Calendar.HOUR_OF_DAY] * MILLISECONDS_PER_HOUR
        return millisecondsThisDay
    }

    private fun getElementX(day: Int) =
        margin + timeLabelsWidth + day * plotWidth / daysPerWeek

    private fun getElementY(millisecondsThisDay: Long) =
        margin +
                plotHeight * (millisecondsThisDay - realStartTime) / (realEndTime - realStartTime)

    private fun getCoords(element: TimeElement) : Array<Float>{
        val w = plotWidth / daysPerWeek
        val cal = getCalendar(element.startTime())
        val day = cal[Calendar.DAY_OF_WEEK] - 1
        val millisecondsThisDay = getMillisecondsThisDay(cal)


        val x = getElementX(day)
        val y = getElementY(millisecondsThisDay)
        val h = element.length() * plotHeight / (realEndTime - realStartTime)
        return arrayOf(x, y, w, h)
    }

}
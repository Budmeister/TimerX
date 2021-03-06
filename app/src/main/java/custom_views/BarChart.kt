package custom_views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes
import com.timerx.thePackage.R

/**
 * Custom view for displaying bar charts. Features:
 * * xLabels,
 * * yLabels,
 * * key,
 * * stacked values
 * Used in: `TimerX`.
 * @author Brian Smith
 */
class BarChart @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.LEFT
        textSize = 55.0f
        typeface = Typeface.create("Times New Roman",  Typeface.BOLD)
    }

    init{
        isClickable = true
        context.withStyledAttributes(attrs, R.styleable.BarChart){
            backColor = getColor(R.styleable.BarChart_backColor, backColor)
            accentColor = getColor(R.styleable.BarChart_accentColor, accentColor)
            margin = getFloat(R.styleable.BarChart_margin, margin)
            borderColor = getColor(R.styleable.BarChart_borderColor, borderColor)
            xGap = getFloat(R.styleable.BarChart_xGap, xGap)
            xLabelsPad = getFloat(R.styleable.BarChart_xLabelsPad, xLabelsPad)
            xLabelsFontSize = getFloat(R.styleable.BarChart_xLabelsFontSize, xLabelsFontSize)
            yLabelsTextWidth = getFloat(R.styleable.BarChart_yLabelsTextWidth, yLabelsTextWidth)
            yLabelsPad = getFloat(R.styleable.BarChart_yLabelsPad, yLabelsPad)
            yLabelsFontSize = getFloat(R.styleable.BarChart_yLabelsFontSize, yLabelsFontSize)
            legendPad = getFloat(R.styleable.BarChart_legendPad, legendPad)
            legendFontSize = getFloat(R.styleable.BarChart_legendFontSize, legendFontSize)
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updatePlotWidth()
        updatePlotHeight()
        barWidth = getBarX(1) - getBarX(0) - xGap
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

    var barRef = 1L
    private var data: Array<LongArray>? = null
    private var colors: IntArray? = null
    private var key: Array<String>? = null
    var xGap = 30f                       // attr
    private var barWidth = 0f

    private var plotWidth = 0f
    private var plotHeight = 0f

    private var xLabels : Array<String>? = null
    var xLabelsPad = 3f                 // attr
    private var xLabelsHeight = 0f
    var xLabelsFontSize = 55.0f         // attr
    var xLabelsFont = Typeface.create("Times New Roman",  Typeface.BOLD)
    var avoidXLabelCollisions = true
    var avoidXLabelCollisionsDecrement = 3f
    var xLabelsFontSizesAfterCollisionCheck = arrayOf<Float>()

    private var yLabels : Array<String>? = null
    private var yLabelsHeights : LongArray? = null
    private var yLabelsWidth = 0f
    var yLabelsTextWidth = 50f          // attr
    var yLabelsPad = 6f                 // attr
    var yLabelsFont = Typeface.create("Times New Roman",  Typeface.BOLD)
    var yLabelsFontSize = 55.0f         // attr

    private var legendHeight = 0f
    var legendPad = 12f                 // attr
    var legendFont = Typeface.create("Times New Roman",  Typeface.BOLD)
    var legendFontSize = 40.0f          // attr
    private var legendBounds: MutableList<Rect> = mutableListOf()

    var emphY : Long? = null
    var emphMessage : String? = null
    var emphFont = Typeface.create("Times New Roman",  Typeface.BOLD)
    var emphFontSize = 25.0f
    var elColor = Color.BLACK

    fun setData(data : Array<LongArray>){
        this.data = data
        barHeights =
            if(data.isNotEmpty())
                FloatArray(data[0].size)
            else
                FloatArray(1)
        invalidate()
    }

    fun setColors(colors : IntArray){
        this.colors = colors
    }

    fun setKey(key : Array<String>?){
        this.key = key
        updatePlotHeight()
    }

    fun setXLabels(xLabels : Array<String>?){
        this.xLabels = xLabels
        updatePlotHeight()
    }

    fun setYLabels(yLabels : Array<String>?, yLabelsHeights : LongArray?){
        this.yLabels = yLabels
        this.yLabelsHeights = yLabelsHeights
        updatePlotWidth()
    }

    fun setEmphLine(emphasisY : Long?, emphMessage : String?){
        this.emphY = emphasisY
        this.emphMessage = emphMessage
    }

    private fun updatePlotWidth() {
        plotWidth = width - margin * 2
        if(yLabels != null && yLabelsHeights != null){
            yLabelsWidth = yLabelsTextWidth + yLabelsPad * 2
            plotWidth-=yLabelsWidth
        }else
            yLabelsWidth = 0f
    }

    private fun updatePlotHeight(){
        plotHeight = height - margin * 2
        updateLegendBounds()
        plotHeight-=legendHeight
        checkXLabelsCollisions()
        plotHeight-=xLabelsHeight
    }

    /**
     * This function should not be called directly. Instead, `updatePlotHeight` should be
     * called, which calls this function and updates all dependant y variables.
     */
    private fun updateLegendBounds() {
        if(key == null) {
            legendHeight = 0f
            legendBounds = mutableListOf()
            return
        }
        legendBounds = mutableListOf()
        paint.textSize = legendFontSize
        var curY = 0f
        var curX = 0f
        for(k in key!!){
            val b = Rect()
            paint.getTextBounds(k, 0, k.length, b)
            val w = b.width()
            if(margin + legendPad * 2 + curX + w >= width - margin - legendPad * 2){
                curX = 0f
                curY+=legendFontSize + legendPad
            }
            b.set(
                curX.toInt(),
                curY.toInt(),
                (curX + w).toInt(),
                (curY + legendFontSize).toInt()
            )
            legendBounds.add(b)
            curX+=w + legendPad * 4
        }

        legendHeight = legendPad +
            if(curX == 0f)
                curY
            else
                curY + legendFontSize + legendPad
    }

    private fun findWidestRect(rects: Array<Rect>) : Int{
        if(rects.isEmpty())
            return -1
        var widestRect = rects[0]
        var widestIndex = 0
        for(i in 1 until rects.size){
            if(rects[i].width() > widestRect.width()){
                widestRect = rects[i]
                widestIndex = i
            }
        }
        return widestIndex
    }

    private fun findTallestRect(rects: Array<Rect>) : Int{
        if(rects.isEmpty())
            return -1
        var tallestRect = rects[0]
        var tallestIndex = 0
        for(i in 1 until rects.size){
            if(rects[i].height() > tallestRect.height()){
                tallestRect = rects[i]
                tallestIndex = i
            }
        }
        return tallestIndex
    }

    /**
     * This function should not be called directly instead, `updatePlotHeight` should
     * be called which calls this one.
     */
    private fun checkXLabelsCollisions(){
        if(xLabels == null) {
            xLabelsHeight = 0f
            return
        }
        if(xLabels!!.size <= 1 || !avoidXLabelCollisions) {
            xLabelsFontSizesAfterCollisionCheck = Array(xLabels!!.size) { xLabelsFontSize }
            return
        }
        fun getXLabelBounds(index: Int, fontSize: Float): Rect{
            val b = Rect()
            paint.textSize = fontSize
            paint.getTextBounds(xLabels!![index], 0, xLabels!![index].length, b)
            val w = b.width()
            val x = getBarX(index) + barWidth / 2 - w / 2
            val y = margin + plotHeight - xLabelsPad    // top
            b.set(
                x.toInt(),
                y.toInt(),
                (x + w).toInt(),
                (y + fontSize).toInt()
            )
            return b
        }
        xLabelsFontSizesAfterCollisionCheck = Array(xLabels!!.size) { xLabelsFontSize }
        val labelBounds = Array(xLabels!!.size) {
            getXLabelBounds(it, xLabelsFontSize)
        }
        var collision = true
        while(collision){
            collision = false
            val widest = findWidestRect(labelBounds)
            val toCheck = mutableListOf<Int>()
            if(widest != 0)
                toCheck.add(widest - 1)
            if(widest != labelBounds.size - 1)
                toCheck.add(widest + 1)
            for(i in toCheck){
                if(Rect.intersects(labelBounds[i], labelBounds[widest])) {
                    xLabelsFontSizesAfterCollisionCheck[i]-=avoidXLabelCollisionsDecrement
                    xLabelsFontSizesAfterCollisionCheck[widest]-=avoidXLabelCollisionsDecrement
                    labelBounds[i] = getXLabelBounds(i, xLabelsFontSizesAfterCollisionCheck[i])
                    labelBounds[widest] = getXLabelBounds(widest, xLabelsFontSizesAfterCollisionCheck[widest])
                    collision = true
                }
            }
        }
        val tallest = findTallestRect(labelBounds)
        xLabelsHeight = labelBounds[tallest].height().toFloat() + xLabelsPad * 2
    }

    private fun getBarX(barNum: Int): Float {
        if(data == null || data!!.isEmpty())
            return 0f
        return margin + yLabelsWidth + xGap + barNum * (plotWidth - xGap) / data!![0].size.toFloat()
    }

    private fun getY(v : Long): Float {
        return margin + plotHeight - v * plotHeight / barRef
    }

    private var barHeights = FloatArray(0)
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.color = borderColor
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.color = backColor
        canvas.drawRect(margin, margin,
            width - margin, height - margin, paint)

        // yLabels
        if(yLabels != null && yLabelsHeights != null) {
            paint.color = accentColor
            paint.textAlign = Paint.Align.CENTER
            paint.typeface = yLabelsFont
            paint.textSize = yLabelsFontSize
            val x = margin + yLabelsWidth / 2
            for(i in yLabels!!.indices){
                val Y = getY(yLabelsHeights!![i])   // Represents where data of this value would go
                val y = Y + yLabelsFontSize / 2     // offset so text is centered
                canvas.drawText(yLabels!![i], x, y, paint)
                canvas.drawLine(margin + yLabelsWidth, Y, width - margin, Y, paint)
            }
        }

        if(data != null && data!!.isNotEmpty()) {
            val sdata= data as Array<LongArray>
            val numBars = sdata[0].size
            barHeights.fill(0f)
            for(e in sdata.indices){
                val exData = sdata[e]
                if(colors != null)
                    paint.color = colors!![e]
                else
                    paint.color = Color.BLACK

                // Bars
                for(i in exData.indices){
                    val height = exData[i]
                    val h = plotHeight * height / barRef.toFloat()
                    val x = getBarX(i)
                    barHeights[i]+=h
                    val y = margin + plotHeight - barHeights[i]
                    canvas.drawRect(x, y,x + barWidth,y + h, paint)
                }

                // Legend
                if(key != null) {
                    val x : Float = margin + legendPad * 2 + legendBounds[e].left
                    val y : Float = margin + plotHeight + xLabelsHeight + legendPad + legendBounds[e].bottom
                    paint.textAlign = Paint.Align.LEFT
                    paint.typeface = legendFont
                    paint.textSize = legendFontSize
                    canvas.drawText(key!![e], x, y, paint)
                }
            }

            // Emphasis y
            if(emphY != null){
                val y = getY(emphY!!)
                paint.color = elColor
                canvas.drawLine(margin + yLabelsWidth, y, width - margin, y, paint)
                if(emphMessage != null){
                    val x = margin + yLabelsWidth
                    paint.typeface = emphFont
                    paint.textSize = emphFontSize
                    paint.textAlign = Paint.Align.LEFT
                    canvas.drawText(emphMessage!!, x, y - 3f, paint)
                }
            }

            // xLabels
            if(xLabels != null) {
                paint.color = accentColor
                paint.textAlign = Paint.Align.CENTER
                paint.typeface = xLabelsFont
                val y = margin + plotHeight + xLabelsHeight - xLabelsPad / 2
                for (i in 0 until numBars) {
                    paint.textSize = xLabelsFontSizesAfterCollisionCheck[i]
                    val x = getBarX(i) + barWidth / 2
                    canvas.drawText(xLabels!![i], x, y, paint)
                }
            }

            // Plot baseline
            paint.color = Color.BLACK
            canvas.drawLine(margin + yLabelsWidth, margin + plotHeight, width - margin, margin + plotHeight, paint)
        }
    }

}
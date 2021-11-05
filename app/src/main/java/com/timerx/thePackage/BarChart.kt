package com.timerx.thePackage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import androidx.core.content.withStyledAttributes

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
    var xLabelsPad = 6f                 // attr
    private var xLabelsHeight = 0f
    var xLabelsFontSize = 55.0f         // attr
    var xLabelsFont = Typeface.create("Times New Roman",  Typeface.BOLD)

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
    var legendFontSize = 55.0f          // attr

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
        if(key != null){
            legendHeight = legendFontSize * ((key!!.size + 1) / 2) + legendPad * 2
            plotHeight-=legendHeight
        }else
            legendHeight = 0f
        if(xLabels != null){
            xLabelsHeight = xLabelsFontSize + xLabelsPad
            plotHeight-=xLabelsHeight
        }else
            xLabelsHeight = 0f
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
                    var y : Float
                    var x : Float
                    if(key!!.size > 2) {
                        y = margin + plotHeight + xLabelsHeight + legendPad + legendFontSize * (e / 3 + 1)
                        x = when (e % 3) {
                                0 -> margin + yLabelsWidth + xGap
                                1 -> width / 3f
                                else -> 2 * width / 3f
                            }
                        x = margin + yLabelsWidth + xGap + plotWidth * e / 3
                        paint.textSize = legendFontSize * 2 / 3
                    }else{
                        y = margin + plotHeight + xLabelsHeight + legendPad + legendFontSize * (e / 2 + 1)
                        x = when(e % 2) {
                            0 -> margin + yLabelsWidth + xGap
                            else -> width / 2f
                        }
                        paint.textSize = legendFontSize
                    }
                    paint.textAlign = Paint.Align.LEFT
                    paint.typeface = legendFont
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
                paint.textSize = xLabelsFontSize
                val y = margin + plotHeight + xLabelsHeight - xLabelsPad / 2
                for (i in 0 until numBars) {
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
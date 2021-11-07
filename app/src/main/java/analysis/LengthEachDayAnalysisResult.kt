package analysis

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.timerx.thePackage.DataProcessor
import com.timerx.thePackage.MainActivity
import com.timerx.thePackage.R
import com.timerx.thePackage.databinding.WidgetAnalysisChartBinding
import kotlinx.serialization.Serializable

@Serializable
class LengthEachDayAnalysisResult(
    override var title: String?,
    override var relevance: Int,
    override var weekNumber: Int,
    @JvmField var lengthEachDay: LongArray,
    @JvmField var longestDay: Int,
    @JvmField var avgLength: Long   // for emphasis line on graph
) : AnalysisResult() {
    override var description: String? = null

    init {
        description = ("Your longest day of " + title + " was " + DataProcessor.daysOfWeek[longestDay] + ". You spent "
                + DataProcessor.formatTime(lengthEachDay[longestDay]) + " on " + title + " that day.")
    }

    companion object{ const val KEY = "ledar" }
    override fun key(): String {
        return KEY
    }

    override fun hasView() = avgLength != 0L

    override fun getView(parent: ViewGroup): View {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.widget_analysis_chart, parent, false)
        val binding = WidgetAnalysisChartBinding.bind(layout)
        val chart = binding.chart
        val textView = binding.textView
        textView.text = "Length each day ${DataProcessor.formatWeekIndex(weekNumber).lowercase()}:\n$description"
        chart.setData(Array(1) { lengthEachDay })
        chart.setColors(IntArray(1) { MainActivity.viewModel.colors[title] ?: Color.LTGRAY })
        chart.setXLabels(DataProcessor.dayOfWeekInitials)
        chart.xLabelsFontSize = 30.0f
        chart.xLabelsPad = 10.0f
        val h = lengthEachDay[longestDay] / DataProcessor.MILLISECONDS_PER_HOUR + 1
        chart.barRef = h * DataProcessor.MILLISECONDS_PER_HOUR
        val maxYLabels = 8
        var numYLabels = h.toInt()-1
        var yLabelIndexInc = 1
        while(numYLabels > maxYLabels){
            numYLabels/=2
            yLabelIndexInc*=2
        }
        val yLabels = Array(numYLabels){ i -> "${(i+1) * yLabelIndexInc}h" }
        val yHeights = LongArray(numYLabels){ i->
            (i+1) * yLabelIndexInc * DataProcessor.MILLISECONDS_PER_HOUR
        }
        chart.setYLabels(yLabels, yHeights)
        chart.yLabelsFontSize = 30.0f
        chart.setEmphLine(avgLength, "avg")
        return layout
    }

}
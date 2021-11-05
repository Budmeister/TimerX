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
class TimeOfDayAnalysisResult(
    override var title: String?,
    override var relevance: Int,
    override var weekNumber: Int,
    @JvmField var lengthEachTimeOfDay: LongArray,
    @JvmField var longestTimeOfDay: Int
) : AnalysisResult() {
    override var description: String?
        get() = "Most of your " + title.toString() + " was done in the " + DataProcessor.timesOfDay[longestTimeOfDay].toString() + " with " + DataProcessor.formatTime(
            lengthEachTimeOfDay[longestTimeOfDay]
        ).toString() + " spent."
        set(description) {
            this@TimeOfDayAnalysisResult.description = description
        }

    companion object{ const val KEY = "todar" }
    override fun key(): String {
        return KEY
    }

    override fun getView(parent: ViewGroup): View {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.widget_analysis_chart, parent, false)
        val binding = WidgetAnalysisChartBinding.bind(layout)
        val chart = binding.chart
        val textView = binding.textView
        textView.text = "Time of day ${DataProcessor.formatWeekIndex(weekNumber).lowercase()}:\n$description"
        chart.setData(Array(1) { lengthEachTimeOfDay })
        chart.setColors(IntArray(1) { MainActivity.viewModel.colors[title] ?: Color.LTGRAY })
        chart.setXLabels(DataProcessor.timesOfDay)
        chart.xLabelsFontSize = 25.0f
        chart.xLabelsPad = 10.0f
        val h = lengthEachTimeOfDay[longestTimeOfDay] / DataProcessor.MILLISECONDS_PER_HOUR + 1
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
        return layout
    }
}
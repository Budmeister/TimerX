package analysis

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.timerx.thePackage.*
import com.timerx.thePackage.databinding.WidgetAnalysisChartBinding
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
class LengthEachDayMetaAnalysisResult(
    override var title: String?,
    override var relevance: Int,
    override var weekNumber: Int,
    var lengths: Array<HashMap<String, LongArray>>,
    var dayLengths: Array<LongArray?>,
    var longestDay: IntArray,
    var avgLengthPerDay: LongArray
) : MetaAnalysisResult() {
    override var description: String?
        get() = if (longestDay.size != 0 && dayLengths[0] != null) "You tracked " + DataProcessor.formatTime(
            dayLengths[0]!![longestDay[0]]
        ) + " on " + DataProcessor.daysOfWeek[longestDay[0]] + "." else "You tracked 0ms on null."
        set(description) {
            this@LengthEachDayMetaAnalysisResult.description = description
        }

    fun getDescription(week: Int): String{
        if(longestDay.size != 0 && dayLengths[week] != null)
//            return "You tracked " + DataProcessor.formatTime(dayLengths[week]!![longestDay[week]]) +
//                    " on " + DataProcessor.daysOfWeek[longestDay[0]] + "."
            return "Your longest day was " + DataProcessor.daysOfWeek[longestDay[week]] + " with " +
                DataProcessor.formatTime(dayLengths[week]!![longestDay[week]]) + " total.\n" +
                "You averaged " + DataProcessor.formatTime(avgLengthPerDay[week]) + " per day."
        return ""

    }

    init {
        require(longestDay.size == lengths.size) { "You gave " + lengths.size + " weeks of lengths but " + longestDay.size + " weeks of longest day labels." }
    }

    companion object{ const val KEY = "ledmar" }
    override fun key(): String {
        return KEY
    }

    override fun getView(parent: ViewGroup): View {
        val recyclerView = RecyclerView(parent.context)
        val adapter = MetaAnalysisRecyclerViewAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(parent.context, LinearLayoutManager.HORIZONTAL, true)
        return recyclerView
    }

    override fun getSize() = lengths.size

    override fun getItemView(parent: ViewGroup, viewType: Int): View {
        val layout = LayoutInflater.from(parent.context).inflate(R.layout.widget_analysis_chart, parent, false)
        val binding = WidgetAnalysisChartBinding.bind(layout)
        val chart = binding.chart
        val textView = binding.textView
        textView.text = "Length each day ${DataProcessor.formatWeekIndex(viewType).lowercase()}:\n${getDescription(viewType)}"

        val names = lengths[viewType].keys.toTypedArray()
        val data = lengths[viewType].values.toTypedArray()
        chart.setData(data)
        chart.setColors(IntArray(data.size) { MainActivity.viewModel.colors.get(names[it])?: Color.LTGRAY })
        chart.setKey(names)
        chart.setXLabels(DataProcessor.dayOfWeekInitials)
        chart.xLabelsFontSize = 30.0f
        chart.xLabelsPad = 10.0f
        var height = 0L
        for(i in data.indices)
            height+=data[i][longestDay[viewType]]
        val h = height / DataProcessor.MILLISECONDS_PER_HOUR + 1
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
        chart.setEmphLine(avgLengthPerDay[viewType], "avg")
        return layout
    }
}
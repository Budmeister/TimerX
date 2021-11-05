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
class TimeOfDayMetaAnalysisResult(
    override var title: String?,
    override var relevance: Int,
    override var weekNumber: Int,
    var lengths: Array<HashMap<String, LongArray>>,
    var timeLengths: Array<LongArray?>,
    var longestTime: IntArray
) : MetaAnalysisResult() {

    override var description: String?
        get() = if (lengths.size != 0 && timeLengths[0] != null) "You spent " + DataProcessor.formatTime(
            timeLengths[0]!![longestTime[0]]
        ) + " in the " + DataProcessor.timesOfDay[longestTime[0]] + " this week." else "You spent 0ms in the null this week."
        set(description)
        {
            this@TimeOfDayMetaAnalysisResult.description = description
        }

    init {
        require(!(lengths.size != timeLengths.size || timeLengths.size != longestTime.size)) { "Illegal numbers of weeks: " + lengths.size + "," + timeLengths.size + "," + longestTime.size }
    }

    companion object{ const val KEY = "todmar" }
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
        textView.text = "Time of day ${DataProcessor.formatWeekIndex(viewType).lowercase()}:\n$description"
        val names = lengths[viewType].keys.toTypedArray()
        val data = lengths[viewType].values.toTypedArray()
        chart.setData(data)
        chart.setColors(IntArray(data.size) { MainActivity.viewModel.colors[names[it]] ?: Color.LTGRAY })
        chart.setKey(names)
        chart.setXLabels(DataProcessor.timesOfDay)
        chart.xLabelsFontSize = 25.0f
        chart.xLabelsPad = 10.0f
        var height = 0L
        for(i in data.indices)
            height+=data[i][longestTime[viewType]]
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
        return layout


//        val chart = BarChart(MainActivity.mainActivity)
//        val names = lengths[viewType].keys.toTypedArray()
//        val data = lengths[viewType].values.toTypedArray()
//        chart.setData(data)
//        chart.setColors(IntArray(data.size) { MainActivity.colors[names[it]]?: Color.LTGRAY })
//        chart.setKey(names)
//        chart.setXLabels(DataProcessor.timesOfDay)
//        var height = 0L
//        for(i in data.indices)
//            height+=data[i][longestTime[viewType]]
//        val h = height / DataProcessor.MILLISECONDS_PER_HOUR + 1
//        chart.barRef = h * DataProcessor.MILLISECONDS_PER_HOUR
//        val yLabels = Array<String>(h.toInt() - 1) { i ->
//            DataProcessor.formatTime((i + 1) * DataProcessor.MILLISECONDS_PER_HOUR)
//        }
//        val yHeights = LongArray(h.toInt()-1) { i ->
//            (i + 1) * DataProcessor.MILLISECONDS_PER_HOUR
//        }
//        chart.setYLabels(yLabels, yHeights)
//        return chart
    }
}
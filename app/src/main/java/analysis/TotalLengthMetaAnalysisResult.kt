package analysis

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.timerx.thePackage.*
import com.timerx.thePackage.databinding.WidgetAnalysisChartBinding
import custom_views.BarChart
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
class TotalLengthMetaAnalysisResult(
    override var title: String?,
    override var relevance: Int,
    override var weekNumber: Int,
    var lengths: Array<HashMap<String, Long?>?>,
    var longestExercise: Array<String>
) : MetaAnalysisResult() {
    override var description: String?
        get() = getDescription(0)
        set(description) {
            this@TotalLengthMetaAnalysisResult.description = description
        }

    fun getDescription(week: Int): String{
        if (longestExercise.size != 0 && lengths[week] != null && lengths[week]!![longestExercise[week]] != null)
            return "You spent the most time on " + longestExercise[week] + " " +
                    DataProcessor.formatWeekIndex(week).lowercase() + " with " +
                    DataProcessor.formatTime(lengths[week]!![longestExercise[week]]!!) +
                    " total!"
        return ""
    }

    init {
        require(lengths.size == longestExercise.size) {
            "You gave " + lengths.size + " weeks of lengths but " + longestExercise.size + " weeks of longest exercise labels."
        }
    }

    companion object{ const val KEY = "tlmar" }
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
        if(lengths[viewType] == null)
            return BarChart(MainActivity.mainActivity)

        val week = lengths[viewType] as HashMap<String, Long?>

        val layout = LayoutInflater.from(parent.context).inflate(R.layout.widget_analysis_chart, parent, false)
        val binding = WidgetAnalysisChartBinding.bind(layout)
        val chart = binding.chart
        val textView = binding.textView
        textView.text = "Length of each exercise ${DataProcessor.formatWeekIndex(viewType).lowercase()}:\n${getDescription(viewType)}"
        val names = week.keys.toTypedArray()
        val data = week.values.toTypedArray()
        chart.setData(Array(data.size) { a ->
            LongArray(data.size) { b ->
                if(a == b)
                    data[a]?:0
                else
                    0
            }
        })
        chart.setColors(IntArray(data.size) { MainActivity.viewModel.colors[names[it]] ?: Color.LTGRAY })
        chart.setXLabels(names)
        chart.xLabelsFontSize = 30.0f
        chart.xLabelsPad = 10.0f
//        val height = data[names.indexOf(longestExercise[viewType])]
        val height = week[longestExercise[viewType]]?: 4 * DataProcessor.MILLISECONDS_PER_HOUR
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
    }
}
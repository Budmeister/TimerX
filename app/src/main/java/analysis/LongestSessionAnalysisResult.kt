package analysis

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.timerx.thePackage.DataProcessor
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
class LongestSessionAnalysisResult(
    override var title: String?,
    override var relevance: Int,
    override var weekNumber: Int,
    var length: Long,
    var time: Long
) : AnalysisResult() {
    override var description: String?
        get() {
            val c = Calendar.getInstance()
            val d = Date(time)
            c.time = d
            return "You spent " + DataProcessor.formatTime(length) + " on " + title + " on " +
                    DataProcessor.daysOfWeek[c[Calendar.DAY_OF_WEEK]-1] + "."
        }
        set(description) {
            this@LongestSessionAnalysisResult.description = description
        }

    companion object{ const val KEY = "lsar" }
    override fun key(): String {
        return KEY
    }

    override fun getView(parent: ViewGroup): View {
        val view = TextView(parent.context)
        view.text = description
        return view
        // TODO Create LongestSessionAnalysisResult view
    }
}
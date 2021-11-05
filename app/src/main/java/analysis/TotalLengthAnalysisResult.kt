package analysis

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.timerx.thePackage.DataProcessor
import kotlinx.serialization.Serializable

@Serializable
class TotalLengthAnalysisResult(
    override var title: String?,
    override var relevance: Int,
    override var weekNumber: Int,
    var totalLength: Long
) : AnalysisResult() {
    override var description: String?
        get() = "You spent " + DataProcessor.formatTime(totalLength) + " on " + title
        set(description) {
            this@TotalLengthAnalysisResult.description = description
        }

    companion object{ const val KEY = "tlar" }
    override fun key(): String {
        return KEY
    }

    override fun getView(parent: ViewGroup): View {
        val view = TextView(parent.context)
        view.text = description
        return view
        // TODO Create TotalLengthAnalysisResult view
    }
}
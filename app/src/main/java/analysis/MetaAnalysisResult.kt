package analysis

import android.view.View
import android.view.ViewGroup

abstract class MetaAnalysisResult : AnalysisResult() {

    abstract fun getSize() : Int

    abstract fun getItemView(parent: ViewGroup, viewType: Int) : View

}
package analysis

import android.view.View
import android.view.ViewGroup
import kotlinx.serialization.Serializable

/**
 * Holds the result of a specific type of analysis. Contains a field,
 * `data` to be transformed into a graph and can be `null`.
 * `getDescription()` should be overridden to return a short description
 * of the data for the user.
 */
@Serializable
sealed class AnalysisResult : Comparable<AnalysisResult> {
    abstract var title: String?
    abstract var relevance: Int
    abstract var weekNumber: Int

    constructor(){}

    constructor(title: String, relevance: Int, weekNumber: Int) {
        this.title = title
        this.relevance = relevance
        this.weekNumber = weekNumber
    }
    /**
     * Creates a short description of the data for the user.
     * @return a description of the data.
     */
    abstract var description: String?
    override fun compareTo(other: AnalysisResult): Int {
        return relevance - other.relevance
    }

    override fun toString(): String {
        return "[$title,$relevance]"
    }

    abstract fun key(): String

    abstract fun hasView(): Boolean

    abstract fun getView(parent: ViewGroup): View

}

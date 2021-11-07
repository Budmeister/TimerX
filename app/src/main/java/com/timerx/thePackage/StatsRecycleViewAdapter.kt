package com.timerx.thePackage

import analysis.AnalysisResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.timerx.thePackage.databinding.WidgetClearButtonBinding
import java.util.*

/**
 * Handles the RecyclerView of the Stats Fragment.
 * @author Brian Smith
 */
class StatsRecycleViewAdapter(
    val fragment: StatsFragment
) : RecyclerView.Adapter<StatsRecycleViewAdapter.ResultViewHolder>() {

    class ResultViewHolder(resultView: View) : RecyclerView.ViewHolder(resultView)

    private val results = mutableListOf<AnalysisResult>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ResultViewHolder {
        if(viewType == itemCount - 1) {  // clear data button
            val layout = LayoutInflater.from(parent.context).inflate(
                R.layout.widget_clear_button,
                parent,
                false
            )
            val binding = WidgetClearButtonBinding.bind(layout)
            binding.clear.setOnClickListener { fragment.showFirstConfirmClearAllDataDialog() }
            binding.clear.setBackgroundColor(0xffe63232.toInt())
            binding.generate.setOnClickListener { fragment.showConfirmGenerateDataDialog() }
            binding.generate.setBackgroundColor(0xff13f25e.toInt())
            return ResultViewHolder(layout)
        }else {                         // normal view
            return ResultViewHolder(results[viewType].getView(parent))
        }
    }

    override fun getItemViewType(position: Int) = position

    // View holder bound in onCreateViewHolder
    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {}

    override fun getItemCount() = results.size + 1  // include generate data button and clear data button

    // to be called before loading results
    fun clearResultsWithoutNotifying(){
        results.clear()
    }

    fun loadResults(relevantResults: PriorityQueue<AnalysisResult>) {
//        results.addAll(relevantResults)
        while(relevantResults.isNotEmpty()) {
            val result = relevantResults.poll()!!
            if (result.hasView())
                results.add(result)
        }
        notifyDataSetChanged()
    }

}
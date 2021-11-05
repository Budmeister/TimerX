package com.timerx.thePackage

import analysis.AnalysisResult
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class StatsRecycleViewAdapter(
) : RecyclerView.Adapter<StatsRecycleViewAdapter.ResultViewHolder>() {

    class ResultViewHolder(resultView: View) : RecyclerView.ViewHolder(resultView)

    private val results = mutableListOf<AnalysisResult>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ResultViewHolder(results[viewType].getView(parent))

    override fun getItemViewType(position: Int) = position

    // View holder bound in onCreateViewHolder
    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {}

    override fun getItemCount() = results.size

    fun loadResults(relevantResults: PriorityQueue<AnalysisResult>) {
        results.addAll(relevantResults)
        notifyDataSetChanged()
    }

}
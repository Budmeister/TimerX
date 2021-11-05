package com.timerx.thePackage

import analysis.MetaAnalysisResult
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class FeedSecondaryRecycleViewAdapter(
    val week: Int
) : RecyclerView.Adapter<FeedSecondaryRecycleViewAdapter.FeedSecondaryViewHolder>() {

    class FeedSecondaryViewHolder(resultView: View) : RecyclerView.ViewHolder(resultView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : FeedSecondaryViewHolder {
        val metaWeek = MainActivity.viewModel.dataProcessor.organizedResults
            ?.get(DataProcessor.META_ANALYSIS_TITLE)
            ?: return FeedSecondaryViewHolder(View(parent.context))
        val metaResults = metaWeek[0]
        if(metaWeek.isEmpty() || metaResults == null || metaResults.isEmpty())
            return FeedSecondaryViewHolder(View(parent.context))
        return FeedSecondaryViewHolder((metaResults[viewType] as MetaAnalysisResult).getItemView(parent, week))
    }

    override fun getItemViewType(position: Int) = position

    // View holder bound in onCreateViewHolder
    override fun onBindViewHolder(holder: FeedSecondaryViewHolder, position: Int) {}

    override fun getItemCount() =
        MainActivity.viewModel.dataProcessor.organizedResults
            ?.get(DataProcessor.META_ANALYSIS_TITLE)
            ?.get(0)?.size
        ?:0

}
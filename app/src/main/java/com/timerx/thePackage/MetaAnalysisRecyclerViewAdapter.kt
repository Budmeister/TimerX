package com.timerx.thePackage

import analysis.MetaAnalysisResult
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MetaAnalysisRecyclerViewAdapter(
    val result: MetaAnalysisResult,
) : RecyclerView.Adapter<MetaAnalysisRecyclerViewAdapter.MetaAnalysisViewHolder>() {

    class MetaAnalysisViewHolder(result: View) : RecyclerView.ViewHolder(result)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MetaAnalysisViewHolder(result.getItemView(parent, viewType))

    override fun getItemViewType(position: Int) = position

    override fun onBindViewHolder(holder: MetaAnalysisViewHolder, position: Int) {}

    override fun getItemCount() = result.getSize()

}
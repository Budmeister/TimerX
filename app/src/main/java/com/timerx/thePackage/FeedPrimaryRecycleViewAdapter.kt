package com.timerx.thePackage

import analysis.MetaAnalysisResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.timerx.thePackage.databinding.RecycleLayoutFeedPrimaryBinding

class FeedPrimaryRecycleViewAdapter() : RecyclerView.Adapter<FeedPrimaryRecycleViewAdapter.FeedPrimaryViewHolder>() {

    class FeedPrimaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedPrimaryViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(
            R.layout.recycle_layout_feed_primary,
            parent,
            false
        )
        val secondaryBinding = RecycleLayoutFeedPrimaryBinding.bind(layout)
        val secondaryRecycler = secondaryBinding.recyclerView
        val secondaryAdapter = FeedSecondaryRecycleViewAdapter(viewType, this)
        secondaryRecycler.adapter = secondaryAdapter
        secondaryRecycler.layoutManager = LinearLayoutManager(parent.context)
        return FeedPrimaryViewHolder(layout)
    }

    override fun getItemViewType(position: Int) = position

    override fun onBindViewHolder(holder: FeedPrimaryViewHolder, position: Int) {
    }

    override fun getItemCount() =
        (MainActivity.viewModel.dataProcessor.organizedResults
            ?.get(DataProcessor.META_ANALYSIS_TITLE)
            ?.get(0)
            ?.get(0) as MetaAnalysisResult?)
            ?.getSize()
        ?:0

}
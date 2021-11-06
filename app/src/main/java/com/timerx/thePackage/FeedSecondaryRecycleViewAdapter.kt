package com.timerx.thePackage

import analysis.MetaAnalysisResult
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.colorpicker.util.setVisibility
import com.timerx.thePackage.databinding.WidgetTitleBinding

class FeedSecondaryRecycleViewAdapter(
    val week: Int,
    val primary: FeedPrimaryRecycleViewAdapter
) : RecyclerView.Adapter<FeedSecondaryRecycleViewAdapter.FeedSecondaryViewHolder>() {

    class FeedSecondaryViewHolder(resultView: View) : RecyclerView.ViewHolder(resultView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : FeedSecondaryViewHolder {
        if(viewType == 0) { // title widget
            val layout = LayoutInflater.from(parent.context).inflate(
                R.layout.widget_title,
                parent,
                false
            )
            val binding = WidgetTitleBinding.bind(layout)
            if(week == 0) {
                binding.title.text = DataProcessor.formatWeekIndex(week)
                binding.rightArrow.setImageResource(R.drawable.ic_empty)
            }else {
                binding.title.text = MainActivity.viewModel.dataProcessor.formatWeekOfDate(week)
            }
            if(week == primary.itemCount - 1)
                binding.leftArrow.setImageResource(R.drawable.ic_empty)
            return FeedSecondaryViewHolder(layout)
        }else {             // normal widget
            val metaWeek = MainActivity.viewModel.dataProcessor.organizedResults
                ?.get(DataProcessor.META_ANALYSIS_TITLE)
                ?: return FeedSecondaryViewHolder(View(parent.context))
            val metaResults = metaWeek[0]
            if(metaWeek.isEmpty() || metaResults == null || metaResults.isEmpty())
                return FeedSecondaryViewHolder(View(parent.context))
            return FeedSecondaryViewHolder((metaResults[viewType-1] as MetaAnalysisResult).getItemView(parent, week))
        }
    }

    override fun getItemViewType(position: Int) = position

    // View holder bound in onCreateViewHolder
    override fun onBindViewHolder(holder: FeedSecondaryViewHolder, position: Int) {}

    override fun getItemCount() =
        (MainActivity.viewModel.dataProcessor.organizedResults
            ?.get(DataProcessor.META_ANALYSIS_TITLE)
            ?.get(0)?.size
        ?:0) + 1    // include title widget

}
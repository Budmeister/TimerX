package com.timerx.thePackage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.timerx.thePackage.databinding.WidgetCalendarBinding

class CalendarRecycleViewAdapter(
    val data: HashMap<String, ArrayList<ExerciseWeek>>,
    val numWeeks: Int
): RecyclerView.Adapter<CalendarRecycleViewAdapter.CalendarViewHolder>() {

    class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(
            R.layout.widget_calendar,
            parent,
            false
        )

        return CalendarViewHolder(layout)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val layoutBinding = WidgetCalendarBinding.bind(holder.itemView)
        val calendar = layoutBinding.calendar
        calendar.colorProvider = { er ->
            MainActivity.viewModel.colors[(er as ExerciseRecord).title]
        }
        val localRecords = mutableListOf<ExerciseRecord>()
        for((_, week) in data)
            if(position < week.size)
                localRecords.addAll(week[position].records)
        val currentRecord = MainActivity.viewModel.currentRecord
        if(position == 0 && currentRecord != null)
            localRecords.add(currentRecord)
        calendar.setData(localRecords)

    }

    override fun getItemCount() = numWeeks

}
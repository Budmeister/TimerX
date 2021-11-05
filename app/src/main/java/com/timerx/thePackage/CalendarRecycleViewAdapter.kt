package com.timerx.thePackage

import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.timerx.thePackage.databinding.WidgetCalendarBinding

class CalendarRecycleViewAdapter(
    val fragment: CalendarFragment,
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
        val textView = layoutBinding.tvTitle
        textView.text = DataProcessor.formatWeekIndex(position)

        val calendar = layoutBinding.calendar
        calendar.colorProvider = { er ->
            MainActivity.viewModel.colors[(er as ExerciseRecord).title]?: Color.LTGRAY
        }
        val localRecords = mutableListOf<TimeElement>()
        for((_, week) in data)
            if(position < week.size)
                localRecords.addAll(week[position].records)
        val currentRecord = MainActivity.viewModel.currentRecord
        if(position == 0 && currentRecord != null)
            localRecords.add(currentRecord)
        calendar.setData(localRecords)
        calendar.setOnTouchListener { v, event ->
            when(event.action){
                MotionEvent.ACTION_DOWN -> {
                    val x = event.x; val y = event.y
                    val element = calendar.getElementAt(x, y) as ExerciseRecord?
                    if(element != null)
                        fragment.showToast(element.title)
                }
                MotionEvent.ACTION_UP -> v.performClick()
            }
            true
        }

    }

    override fun getItemCount() = numWeeks

}
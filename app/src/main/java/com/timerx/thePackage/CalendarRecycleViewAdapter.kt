package com.timerx.thePackage

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.timerx.thePackage.databinding.WidgetCalendarBinding
import java.util.*

/**
 * Handles the horizontal RecyclerView in the calendar fragment.
 * @author Brian Smith
 */
class CalendarRecycleViewAdapter(
    val fragment: CalendarFragment,
    val data: HashMap<String, ArrayList<ExerciseWeek>>,
    val numWeeks: Int
): RecyclerView.Adapter<CalendarRecycleViewAdapter.CalendarViewHolder>() {

    class CalendarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    var startTime = 4 * DataProcessor.MILLISECONDS_PER_HOUR
    var endTime = 20 * DataProcessor.MILLISECONDS_PER_HOUR

    private lateinit var mRecyclerView: RecyclerView

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

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
        // title
        val textView = layoutBinding.tvTitle
        if(position == 0) {
            textView.text = DataProcessor.formatWeekIndex(position)
            layoutBinding.rightArrow.setImageResource(R.drawable.ic_empty)
        }else {
            textView.text = MainActivity.viewModel.dataProcessor.formatWeekOfDate(position)
            layoutBinding.rightArrow.setOnClickListener {
                fragment.flingListener.flingRightFromStationary()
            }
        }
        if (position == numWeeks - 1) {
            layoutBinding.leftArrow.setImageResource(R.drawable.ic_empty)
        }else {
            layoutBinding.leftArrow.setOnClickListener {
                fragment.flingListener.flingLeftFromStationary()
            }
        }

        // calendar view
        val calendar = layoutBinding.calendar
        calendar.startTime = startTime
        calendar.endTime = endTime
        calendar.colorProvider = { er ->
            MainActivity.viewModel.colors[(er as ExerciseRecord).title]?: Color.LTGRAY
        }
        val localRecords = mutableListOf<TimeElement>()
        for((_, week) in data)
            if(position < week.size && week[position] != null)
                localRecords.addAll(week[position].records)
        val currentRecord = MainActivity.viewModel.currentRecord
        if(position == 0 && currentRecord != null) {
            val currentRecordCopy = currentRecord.copy()
            currentRecordCopy.endTime = Calendar.getInstance().timeInMillis
            localRecords.add(currentRecordCopy)
        }
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
        calendar.showNowBar = position == 0
    }

    override fun getItemCount() = numWeeks

}
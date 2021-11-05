package com.timerx.thePackage

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.timerx.thePackage.databinding.FragmentCalendarBinding

class CalendarFragment(
    val mainActivity: MainActivity
) : Fragment(R.layout.fragment_calendar) {

    private lateinit var binding: FragmentCalendarBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCalendarBinding.bind(view)


        // setup
        binding.recycler.adapter = CalendarRecycleViewAdapter(
            this,
            MainActivity.viewModel.dataProcessor.weeks,
            5
        )
        binding.recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true)

        val snapHelper = FlinglessLinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.recycler)
        val minVelocity = 450
        binding.recycler.onFlingListener = object: RecyclerView.OnFlingListener(){
            override fun onFling(velocityX: Int, velocityY: Int): Boolean { // just snap
                val layoutManager = binding.recycler.layoutManager ?: return false
                val snapView =
                    if(velocityX > minVelocity)
                        layoutManager.getChildAt(0) ?: return false
                    else if(velocityX < -minVelocity)
                        layoutManager.getChildAt(1) ?: return false
                    else
                        snapHelper.findSnapView(layoutManager) ?: return false
                val snapDistance = snapHelper.calculateDistanceToFinalSnap(layoutManager, snapView)
                if (snapDistance!![0] != 0 || snapDistance[1] != 0) {
                    binding.recycler.smoothScrollBy(snapDistance[0], snapDistance[1])
                }
                return true
            }
        }
    }

    fun showToast(message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

}
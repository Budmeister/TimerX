package com.timerx.thePackage

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.timerx.thePackage.databinding.FragmentCalendarBinding

class CalendarFragment(
    val mainActivity: MainActivity
) : Fragment(R.layout.fragment_calendar) {

    private lateinit var binding: FragmentCalendarBinding

    val minVelocityToFling = 450
    lateinit var flingListener: FlinglessFlingListener

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
        flingListener = FlinglessFlingListener(binding.recycler, minVelocityToFling, snapHelper)
        binding.recycler.onFlingListener = flingListener

    }

    fun showToast(message: String){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun updateAllData() {
        if(this::binding.isInitialized)
            binding.recycler.adapter?.notifyDataSetChanged()
    }

}
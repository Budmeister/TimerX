package com.timerx.thePackage

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.timerx.thePackage.databinding.FragmentHomeBinding
import java.util.*

class HomeFragment(
    private val mainActivity: MainActivity
) : Fragment(R.layout.fragment_home) {

    private lateinit var binding : FragmentHomeBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        // setup

        val exercises = mutableListOf<ExerciseWidget>()

        for((name, color) in MainActivity.viewModel.colors)
            exercises.add(ExerciseWidget(name, color))
        exercises.sortBy { widget -> if(widget.name == "") "zzzzzzzzzzzzzzzzzzzzzz" else widget.name }
        binding.rvHome.adapter = HomeRecycleViewAdapter(this, exercises)
        binding.rvHome.layoutManager = LinearLayoutManager(binding.root.context)
    }

    fun createExercise(name: String, color: Int){
        MainActivity.viewModel.colors[name] = color
    }

    fun storeExerciseRecord() =
        (binding.rvHome.adapter as HomeRecycleViewAdapter).storeExerciseRecord()

    fun createExerciseRecord(name: String) =
        (binding.rvHome.adapter as HomeRecycleViewAdapter).createExerciseRecord(name)

}
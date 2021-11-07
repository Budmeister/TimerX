package com.timerx.thePackage

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.timerx.thePackage.databinding.FragmentHomeBinding
import java.util.*

class HomeFragment(
    private val mainActivity: MainActivity
) : Fragment(R.layout.fragment_home) {

    private lateinit var binding : FragmentHomeBinding
    private lateinit var adapter: HomeRecycleViewAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        // setup

//        val exercises = mutableListOf<ExerciseWidget>()
//
//        for((name, color) in MainActivity.viewModel.colors)
//            exercises.add(ExerciseWidget(name, color))
//        exercises.sortBy { widget -> if(widget.name == "") "zzzzzzzzzzzzzzzzzzzzzz" else widget.name }
        adapter = HomeRecycleViewAdapter(this)
        binding.rvHome.adapter = adapter
        updateAllData()
        binding.rvHome.layoutManager = LinearLayoutManager(binding.root.context)
    }

    fun createExercise(name: String, color: Int){
        MainActivity.viewModel.colors[name] = color
    }

    fun storeExerciseRecord() =
        (binding.rvHome.adapter as HomeRecycleViewAdapter).storeExerciseRecord()

    fun createExerciseRecord(name: String) =
        (binding.rvHome.adapter as HomeRecycleViewAdapter).createExerciseRecord(name)

    fun updateAllData() {
        if(!this::binding.isInitialized)
            return
        val exercises = mutableListOf<ExerciseWidget>()

        for((name, color) in MainActivity.viewModel.colors)
            exercises.add(ExerciseWidget(name, color))
        exercises.sortBy { widget -> if(widget.name == "") "zzzzzzzzzzzzzzzzzzzzzz" else widget.name }
        (binding.rvHome.adapter as HomeRecycleViewAdapter).setExercises(exercises)
    }



    fun showConfirmDeleteExerciseDialog(name: String, index: Int){
        val builder: AlertDialog.Builder = AlertDialog.Builder(MainActivity.mainActivity)
        builder.setTitle("Delete $name?")

        builder.setPositiveButton("OK") { dialog, which ->
            MainActivity.mainActivity.deleteExercise(name)
            adapter.deleteExerciseWidget(index)
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()

    }

    private fun showAddExerciseColorPicker(name: String){
        ColorPickerDialog
            .Builder(MainActivity.mainActivity)
            .setTitle(name)
            .setColorShape(ColorShape.SQAURE)
            .setDefaultColor(R.color.light_blue_900)
            .setColorListener { color, colorHex -> adapter.addExerciseWidget(name, color) }
            .show()
    }

    fun showAddExerciseDialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(MainActivity.mainActivity)
        builder.setTitle("Add exercise")

        val input = EditText(MainActivity.mainActivity)
        input.hint = "Exercise Title"
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, which ->
            val name = input.text.toString()
            showAddExerciseColorPicker(name)
            dialog.cancel()
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()

    }


}
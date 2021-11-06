package com.timerx.thePackage

import android.app.AlertDialog
import android.graphics.Color
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.timerx.thePackage.databinding.WidgetExerciseBinding
import com.timerx.thePackage.databinding.WidgetExerciseSingleBinding
import kotlin.collections.HashMap

class HomeRecycleViewAdapter(
    private val fragment: HomeFragment,
    private val exercises: MutableList<ExerciseWidget>
) : RecyclerView.Adapter<HomeRecycleViewAdapter.ExerciseViewHolder>() {

    init{
        exercises.add(ExerciseWidget("", Color.LTGRAY))
    }

    private val buttons = HashMap<String, ExerciseWidgetView>()
    val play  = ResourcesCompat.getDrawable(MainActivity.mainActivity.resources, R.drawable.ic_play, null)
    val pause = ResourcesCompat.getDrawable(MainActivity.mainActivity.resources, R.drawable.ic_pause, null)
    val add   = ResourcesCompat.getDrawable(MainActivity.mainActivity.resources, R.drawable.ic_add, null)

    class ExerciseViewHolder(exerciseView: View) : RecyclerView.ViewHolder(exerciseView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(
            if(viewType == 1)
                R.layout.widget_exercise_single
            else
                R.layout.widget_exercise,
            parent,
            false
        )
        return ExerciseViewHolder(layout)
    }

    override fun getItemViewType(position: Int): Int {
        return if(position == exercises.size / 2)
            1
        else
            0
    }

    fun createExerciseRecord(name: String){
        storeExerciseRecord()
        MainActivity.viewModel.createCurrentRecord(name)
        buttons[name]?.setIsRunning(true)
        buttons[name]?.setIcon(pause)
    }

    fun storeExerciseRecord(){
        val currentRecord = MainActivity.viewModel.currentRecord
        if(currentRecord != null) {
            buttons[currentRecord.title]?.setIsRunning(false)
            buttons[currentRecord.title]?.setIcon(play)
        }
        MainActivity.viewModel.storeCurrentRecordToDataProcessor()
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val buttons = mutableListOf<ExerciseWidgetView>()
        if(position == exercises.size / 2) {    // single widget
            val viewBinding = WidgetExerciseSingleBinding.bind(holder.itemView)
            buttons.add(viewBinding.btnLeft)
        }else{                                      // double widget
            val viewBinding = WidgetExerciseBinding.bind(holder.itemView)
            buttons.add(viewBinding.btnLeft)
            buttons.add(viewBinding.btnRight)
        }
        for(b in buttons.indices){
            val button = buttons[b]
            val name = exercises[2 * position + b].name
            val color = exercises[2 * position + b].color
            button.setText(name)
            button.setColor(color)
            if(name == ""){ // the add-exercise button
                button.setOnClickListener {
                    showAddExerciseDialog()
                }
                button.setIcon(add)
            }else {
                button.setIcon(play)
                val currentRecord = MainActivity.viewModel.currentRecord
                if(currentRecord != null && currentRecord.title == name){
                    button.setIsRunning(true)
                    button.setIcon(pause)
                }
                button.setOnClickListener {
                        if (button.isRunning()) {
                            MainActivity.viewModel.currentRecord?.endTime = -1
                            storeExerciseRecord()
                        }else
                            createExerciseRecord(name)
                }
                button.setOnLongClickListener {

                    showConfirmDeleteExerciseDialog(name, 2 * position + b)
                    true
                }
            }

            this.buttons[name] = buttons[b]
        }
    }

    private fun addExerciseWidget(name: String, color: Int){
        exercises.add(exercises.size - 1, ExerciseWidget(name, color))
        fragment.createExercise(name, color)
        exercises.sortBy { widget -> if(widget.name == "") "zzzzzzzzzzzzzzzzzzzzzz" else widget.name }
        for(i in 0 until itemCount)
            notifyItemChanged(i)
        if(itemCount % 2 == 1)
            notifyItemInserted(itemCount - 1)
        for(pair in buttons)
            pair.value.setIcon(play)

    }

    private fun showConfirmDeleteExerciseDialog(name: String, index: Int){
        val builder: AlertDialog.Builder = AlertDialog.Builder(MainActivity.mainActivity)
        builder.setTitle("Delete $name?")

        builder.setPositiveButton("OK") { dialog, which ->
            MainActivity.mainActivity.deleteExercise(name)
            deleteExerciseWidget(index)
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()

    }

    private fun deleteExerciseWidget(index: Int) {
        exercises.removeAt(index)
        exercises.sortBy { widget -> if(widget.name == "") "zzzzzzzzzzzzzzzzzzzzzz" else widget.name }
        notifyDataSetChanged()
    }

    private fun showAddExerciseColorPicker(name: String){
        ColorPickerDialog
            .Builder(MainActivity.mainActivity)
            .setTitle(name)
            .setColorShape(ColorShape.SQAURE)
            .setDefaultColor(R.color.light_blue_900)
            .setColorListener { color, colorHex -> addExerciseWidget(name, color) }
            .show()
    }

    private fun showAddExerciseDialog(){
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

    override fun getItemCount(): Int {
        return (exercises.size + 1) / 2
    }

}
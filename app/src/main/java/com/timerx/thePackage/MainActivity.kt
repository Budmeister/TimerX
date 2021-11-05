package com.timerx.thePackage

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.timerx.thePackage.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.lang.NumberFormatException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object{
//        lateinit var dataProcessor: DataProcessor

        lateinit var homeFragment: HomeFragment
        lateinit var statsFragment: StatsFragment
        lateinit var feedFragment: FeedFragment

        lateinit var mainActivity: MainActivity
        lateinit var viewModel: MainViewModel

//        lateinit var settings: Settings

//        var colors = HashMap<String, Int>()
        var deletingExercises = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("MainActivity", "Activity created")
        val model: MainViewModel by viewModels()
        viewModel = model

        binding = ActivityMainBinding.inflate(layoutInflater)
        mainActivity = this

        // initialize fragments
        homeFragment = HomeFragment(this)
        statsFragment = StatsFragment(this)
        feedFragment = FeedFragment(this)

//        viewModel.getLiveCurrentRecord().observe(this){
//            if(it != null)
//                onRecordCreated(it)
//        }

//        lifecycleScope.launch{
//            settings = Settings(mutableListOf(), mutableListOf())
//            saveColorsToSettingsObject()
        setContentView(binding.root)
        setCurrentFragment(homeFragment)
//            FileManager.saveSettings(this@MainActivity, settings)
//        }
        lifecycleScope.launch { statsFragment.loadResults() }

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.miHome  -> setCurrentFragment(homeFragment)
                R.id.miStats -> setCurrentFragment(statsFragment)
                R.id.miFeed  -> setCurrentFragment(feedFragment)
            }
            true
        }
        if(viewModel.currentRecord != null)
            showVerifyCurrentRecordDialog()

    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        menuInflater.inflate(R.menu.delete_exercise_menu, menu)
//        return true
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        if(item.title == "Delete") {
//            item.title = "Done"
//            deletingExercises = true
//        }else {
//            item.title = "Delete"
//            deletingExercises = false
//        }
//        return true
//    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply{
            replace(R.id.flSelFragment, fragment)
            commit()
        }

//    fun onRecordCreated(er: ExerciseRecord){
//        viewModel.getLiveDataProcessor().value!!.addRecord(er.title, er)
//        Log.i("MainActivity", "Exercise created: $er, " + DataProcessor.formatTime(er.length()))
//    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivity", "Activity Paused")
        lifecycleScope.launch {
            Log.i("Main Activity", "Writing files")
            viewModel.apply{
                saveCurrentRecordToSettingsObject()
                saveColorsToSettingsObject()
                writeSettings()
                writeRecords()
            }
            Log.i("MainActivity", "Files written")
        }
//        dataProcessor.writeRecords(this)
//        lifecycleScope.launch {
//            homeFragment.forceRecordCreated()
//            viewModel.writeRecords()
//            saveColorsToSettingsObject()
//            FileManager.saveSettings(this@MainActivity, settings)
//        }
    }

    override fun onStop(){
        super.onStop()
    }

    fun deleteExercise(name: String) {
        viewModel.colors.remove(name)
    }

    fun showIncorrectCurrentRecordVerification(invalidString: String){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Invalid time: $invalidString")

        builder.setPositiveButton("OK") { dialog, which ->
            showVerifyCurrentRecordDialog()
        }

        builder.show()
    }

    fun showVerifyCurrentRecordDialog(){
        val currentRecord = viewModel.currentRecord?: return
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Last time, you were doing ${currentRecord.title}. How long did that last?")

        val input = EditText(this)
        input.hint = "hours"
        input.inputType = InputType.TYPE_DATETIME_VARIATION_TIME
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, which ->
            try {
                val hours = input.text.toString().toFloat()
                currentRecord.endTime = currentRecord.startTime + (DataProcessor.MILLISECONDS_PER_HOUR * hours).toLong()
//                viewModel.storeCurrentRecordToDataProcessor()
                homeFragment.storeExerciseRecord()
            } catch(e: NumberFormatException){
                showIncorrectCurrentRecordVerification(input.text.toString())
            }finally{
                dialog.cancel()
            }
        }
        builder.setNegativeButton("I am still going!"){ dialog, which ->
            currentRecord.endTime = -1
            dialog.cancel()
        }

        builder.show()
    }


}
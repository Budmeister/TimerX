package com.timerx.thePackage

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.timerx.thePackage.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

/**
 * The main activity of the app.
 * @author Brian Smith
 * @see [MainViewModel]
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object{

        lateinit var homeFragment: HomeFragment
        lateinit var statsFragment: StatsFragment
        lateinit var calendarFragment: CalendarFragment
        lateinit var feedFragment: FeedFragment

        lateinit var mainActivity: MainActivity
        lateinit var viewModel: MainViewModel
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
        calendarFragment = CalendarFragment(this)
        feedFragment = FeedFragment(this)

        setContentView(binding.root)
        setCurrentFragment(homeFragment)

        lifecycleScope.launch { statsFragment.loadResults() }

        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.miHome     -> setCurrentFragment(homeFragment)
                R.id.miStats    -> setCurrentFragment(statsFragment)
                R.id.miCalendar -> setCurrentFragment(calendarFragment)
                R.id.miFeed     -> setCurrentFragment(feedFragment)
            }
            true
        }
        if(viewModel.currentRecord != null)
            showVerifyCurrentRecordDialog()

    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply{
            replace(R.id.flSelFragment, fragment)
            commit()
        }

    override fun onPause() {
        super.onPause()
        Log.i("MainActivity", "Activity Paused")
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

    fun clearAllData(){
        viewModel.clearAllData()
        homeFragment.updateAllData()
        feedFragment.updateAllData()
        calendarFragment.updateAllData()
        binding.bottomNavigationView.selectedItemId = R.id.miHome
        statsFragment.updateAllData()
    }

    fun generateData(){
        viewModel.generateData()
        homeFragment.updateAllData()
        feedFragment.updateAllData()
        calendarFragment.updateAllData()
        binding.bottomNavigationView.selectedItemId = R.id.miHome
        statsFragment.updateAllData()
    }


}
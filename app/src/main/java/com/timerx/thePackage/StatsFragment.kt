package com.timerx.thePackage

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.timerx.thePackage.databinding.FragmentStatsBinding

/**
 * The Stats fragment of the main screen.
 * @author Brian Smith
 */
class StatsFragment(
    private val mainActivity: MainActivity
) : Fragment(R.layout.fragment_stats) {

    private lateinit var binding : FragmentStatsBinding

    private var adapter: StatsRecycleViewAdapter = StatsRecycleViewAdapter(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentStatsBinding.bind(view)
        // setup
        binding.rvFeed.adapter = adapter
        binding.rvFeed.layoutManager = LinearLayoutManager(binding.root.context)
    }

    fun loadResults(){
        // TODO create Feed Fragment, recycle view, and result views
        Log.i("StatsFragment", "Waiting for results...")
        while(!MainActivity.viewModel.dataProcessor.isRelevantResultsReady);
        Log.d("StatsFragment", "Results: ${MainActivity.viewModel.dataProcessor.relevantResults}")
        adapter.loadResults(MainActivity.viewModel.dataProcessor.relevantResults)
    }

    fun showConfirmGenerateDataDialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("Generate random data? (This will clear your current data.)")

        builder.setPositiveButton("Yes") { dialog, which ->
            mainActivity.generateData()
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    fun showFirstConfirmClearAllDataDialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        builder.setTitle("Clear all data?")

        builder.setPositiveButton("Yes") { dialog, which ->
            showSecondConfirmClearAllDataDialog()
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    fun showSecondConfirmClearAllDataDialog(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(MainActivity.mainActivity)
        builder.setTitle("Are you sure?")

        builder.setPositiveButton("Yes") { dialog, which ->
            mainActivity.clearAllData()
        }
        builder.setNegativeButton("Cancel") { dialog, which -> dialog.cancel() }

        builder.show()
    }

    fun updateAllData() {
        if(this::binding.isInitialized) {
            adapter.clearResultsWithoutNotifying()
            loadResults()
        }
    }

}
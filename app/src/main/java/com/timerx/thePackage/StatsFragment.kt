package com.timerx.thePackage

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.timerx.thePackage.databinding.FragmentStatsBinding

class StatsFragment(
    private val mainActivity: MainActivity
) : Fragment(R.layout.fragment_stats) {

    private lateinit var binding : FragmentStatsBinding

    private var adapter: StatsRecycleViewAdapter = StatsRecycleViewAdapter()

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
        adapter.loadResults(MainActivity.viewModel.dataProcessor.relevantResults)
    }

}
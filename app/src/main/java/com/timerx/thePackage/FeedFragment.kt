package com.timerx.thePackage

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.timerx.thePackage.databinding.FragmentFeedBinding


class FeedFragment(
    private val mainActivity: MainActivity
) : Fragment(R.layout.fragment_feed) {

    private lateinit var binding : FragmentFeedBinding

    val minVelocityToFling = 450
    lateinit var flingListener: FlinglessFlingListener

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFeedBinding.bind(view)
        // setup
        binding.primaryRecycler.adapter = FeedPrimaryRecycleViewAdapter(this)
        binding.primaryRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true)

        val snapHelper = FlinglessLinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.primaryRecycler)
        flingListener = FlinglessFlingListener(binding.primaryRecycler, minVelocityToFling, snapHelper)
        binding.primaryRecycler.onFlingListener = flingListener
    }

    fun updateAllData() {
        if(this::binding.isInitialized)
            binding.primaryRecycler.adapter?.notifyDataSetChanged()
    }

}
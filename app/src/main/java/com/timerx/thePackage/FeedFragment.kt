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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFeedBinding.bind(view)
        // setup
        binding.primaryRecycler.adapter = FeedPrimaryRecycleViewAdapter()
        binding.primaryRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, true)

        val snapHelper = FlinglessLinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.primaryRecycler)
        val minVelocity = 450
        binding.primaryRecycler.onFlingListener = object: RecyclerView.OnFlingListener(){
            override fun onFling(velocityX: Int, velocityY: Int): Boolean { // just snap
                Log.d("FeedFragment", "$velocityX")
                val layoutManager = binding.primaryRecycler.layoutManager ?: return false
                val snapView =
                    if(velocityX > minVelocity)
                        layoutManager.getChildAt(0) ?: return false
                    else if(velocityX < -minVelocity)
                        layoutManager.getChildAt(1) ?: return false
                    else
                        snapHelper.findSnapView(layoutManager) ?: return false
                val snapDistance = snapHelper.calculateDistanceToFinalSnap(layoutManager, snapView)
                if (snapDistance!![0] != 0 || snapDistance[1] != 0) {
                    binding.primaryRecycler.smoothScrollBy(snapDistance[0], snapDistance[1])
                }
                return true
            }
        }
    }

}
package com.timerx.thePackage

import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView

/**
 * Allows a `RecyclerView` to always snap by one object rather than
 * scrolling until it stops. Used in [FeedPrimaryRecycleViewAdapter]
 * and [CalendarRecycleViewAdapter]. To be used in conjunction with
 * [FlinglessLinearSnapHelper]
 * @author Brian Smith
 */
class FlinglessFlingListener(
    val mRecyclerView: RecyclerView,
    val minVelocityToFling: Int,
    val snapHelper: LinearSnapHelper
) : RecyclerView.OnFlingListener() {

    override fun onFling(velocityX: Int, velocityY: Int): Boolean { // just snap
        val layoutManager = mRecyclerView.layoutManager ?: return false
        val snapView =
            if(velocityX >= minVelocityToFling)
                layoutManager.getChildAt(0) ?: return false
            else if(velocityX <= -minVelocityToFling)
                layoutManager.getChildAt(1) ?: return false
            else
                snapHelper.findSnapView(layoutManager) ?: return false
        val snapDistance = snapHelper.calculateDistanceToFinalSnap(layoutManager, snapView)
        if (snapDistance!![0] != 0 || snapDistance[1] != 0) {
            mRecyclerView.smoothScrollBy(snapDistance[0], snapDistance[1])
        }
        return true
    }

    fun flingLeftFromStationary(){
        val layoutManager = mRecyclerView.layoutManager ?: return
        val snapView =
                layoutManager.getChildAt(0)?: return
        val snapDistance = -snapView.width
        mRecyclerView.smoothScrollBy(snapDistance, 0)
    }

    fun flingRightFromStationary(){
        val layoutManager = mRecyclerView.layoutManager ?: return
        val snapView =
            layoutManager.getChildAt(0)?: return
        val snapDistance = snapView.width
        mRecyclerView.smoothScrollBy(snapDistance, 0)
    }

}
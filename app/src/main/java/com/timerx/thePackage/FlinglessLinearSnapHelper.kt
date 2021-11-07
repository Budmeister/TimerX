package com.timerx.thePackage

import android.view.View
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

/**
 * Allows a `RecyclerView` to always snap by one object rather than
 * scrolling until it stops. Used in [FeedPrimaryRecycleViewAdapter]
 * and [CalendarRecycleViewAdapter]. To be used in conjunction with
 * [FlinglessFlingListener]
 * @author Brian Smith
 */
class FlinglessLinearSnapHelper : LinearSnapHelper() {

    private fun findCenterView(
        layoutManager: RecyclerView.LayoutManager,
        helper: OrientationHelper
    ): View? {
        val childCount = layoutManager.childCount
        if (childCount == 0) {
            return null
        }
        var closestChild: View? = null
        val center = helper.startAfterPadding + helper.totalSpace / 2
        var absClosest = Int.MAX_VALUE
        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i)
            val childCenter = (helper.getDecoratedStart(child)
                    + helper.getDecoratedMeasurement(child) / 2)
            val absDistance = abs(childCenter - center)
            /** if child center is closer than previous closest, set it as closest   */
            if (absDistance < absClosest) {
                absClosest = absDistance
                closestChild = child
            }
        }
        return closestChild
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager?): View? {
        if(layoutManager == null)
            return null
        if (layoutManager.canScrollVertically()) {
            return findCenterView(layoutManager, OrientationHelper.createVerticalHelper(layoutManager))
        } else if (layoutManager.canScrollHorizontally()) {
            return findCenterView(layoutManager, OrientationHelper.createHorizontalHelper(layoutManager))
        }
        return null
    }

}
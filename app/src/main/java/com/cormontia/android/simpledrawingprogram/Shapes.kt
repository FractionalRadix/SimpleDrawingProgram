package com.cormontia.android.simpledrawingprogram

import android.graphics.PointF

data class Circle(val center: PointF, val radius: Double, val color:Int)

class PointsList(val points:MutableList<PointF>, val color:Int) {
    fun add(p: PointF) {
        // Only add this point if it is different from the last point.
        if (!points.last().equals(p)) {
            points.add(p)
        }
    }
}
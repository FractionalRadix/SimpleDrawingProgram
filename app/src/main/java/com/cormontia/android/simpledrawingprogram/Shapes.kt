package com.cormontia.android.simpledrawingprogram

import android.graphics.PointF

data class Circle(val center: PointF, val radius: Double, val color:Int)

class PointsList(val points:MutableList<PointF>, val color:Int) {
    fun add(p: PointF) {
        if (points.isEmpty()) {
            points.add(p)
        } else if (!points.last().equals(p)) {
            // Only add this point if it is different from the last point.
            points.add(p)
        }
    }
}

data class LineSegment(val p: PointF, val q: PointF, val color:Int)
package com.cormontia.android.simpledrawingprogram

import android.graphics.PointF

interface Shape {
    fun svg(color: Int): String
}

fun colorAsRgb(color: Int) : String {
    val r = color.and(0x00FF0000).shr(16)
    val g = color.and(0x0000FF00).shr( 8)
    val b = color.and(0x000000FF)
    return """rgb($r,$g,$b)"""
}

class Circle(val center: PointF, val radius: Double): Shape {
    override fun svg(color: Int) =
        """<circle cx="${center.x}" cy="${center.y}" r="$radius" stroke="${colorAsRgb(color)}" stroke-width="1" fill="none"/>"""
}

class PointsList(val points:MutableList<PointF>): Shape {
    fun add(p: PointF) {
        if (points.isEmpty()) {
            points.add(p)
        } else if (!points.last().equals(p)) {
            // Only add this point if it is different from the last point.
            points.add(p)
        }
    }

    override fun svg(color: Int): String {
        val p0 = points[0]
        val startOfPath = "M${p0.x} ${p0.y}"
        val restOfPath = points
            .drop(1)
            .map { p -> "L${p.x} ${p.y}" }
            .joinToString(separator=" ")
        val path = "$startOfPath $restOfPath"

        return """<path d="$path" stroke="${colorAsRgb(color)}" fill="none"/>"""
    }
}

class LineSegment(val p: PointF, val q: PointF): Shape {
    override fun svg(color: Int) =
        """<line x1="${p.x}" y1="${p.y}" x2="${q.x}" y2="${q.y}" style="stroke:${colorAsRgb(color)};stroke-width:1" />"""
}
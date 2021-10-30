package com.cormontia.android.simpledrawingprogram

import androidx.lifecycle.ViewModel

class PaintingViewModel : ViewModel() {
    private val lineSegments = mutableListOf<LineSegment>()
    private val circles = mutableListOf<Circle>()
    private val pointsLists = mutableListOf<PointsList>()

    fun pointsListsIterator(): Iterator<PointsList> = pointsLists.iterator()

    fun addPointsList(pointsList: PointsList) {
        pointsLists.add(pointsList)
    }

    fun lineSegmentIterator(): Iterator<LineSegment> = lineSegments.iterator()

    fun addLineSegment(newLineSegment: LineSegment) {
        lineSegments.add(newLineSegment)
    }

    fun circleIterator(): Iterator<Circle> = circles.iterator()

    fun addCircle(newCircle: Circle) {
        circles.add(newCircle)
    }
}
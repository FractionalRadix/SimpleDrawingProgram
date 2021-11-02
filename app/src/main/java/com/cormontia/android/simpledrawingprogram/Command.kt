package com.cormontia.android.simpledrawingprogram

import android.graphics.Canvas

interface Command {
    fun execute(view: PaintingView, canvas: Canvas)
}

class DrawLineSegmentCommand(val lineSegment: LineSegment) : Command {
    override fun execute(view: PaintingView, canvas: Canvas) =
        view.drawLineSegment(canvas, lineSegment)
}

class DrawCircleCommand(val circle: Circle) : Command {
    override fun execute(view: PaintingView, canvas: Canvas) =
        view.drawCircle(canvas, circle)
}

class DrawPointsListCommand(val pointsList: PointsList) : Command {
    override fun execute(view: PaintingView, canvas: Canvas) =
        view.drawPointsList(canvas, pointsList)
}
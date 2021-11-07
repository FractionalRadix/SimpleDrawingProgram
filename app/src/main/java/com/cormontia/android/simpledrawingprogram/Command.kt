package com.cormontia.android.simpledrawingprogram

import android.graphics.Canvas

interface Command {
    fun execute(view: PaintingView, canvas: Canvas)
}

class DrawLineSegmentCommand(val lineSegment: LineSegment, val color: Int) : Command {
    override fun execute(view: PaintingView, canvas: Canvas) =
        view.drawLineSegment(canvas, lineSegment, color)
}

class DrawCircleCommand(val circle: Circle, val color: Int) : Command {
    override fun execute(view: PaintingView, canvas: Canvas) =
        view.drawCircle(canvas, circle, color)
}

class DrawPointsListCommand(val pointsList: PointsList, val color: Int) : Command {
    override fun execute(view: PaintingView, canvas: Canvas) =
        view.drawPointsList(canvas, pointsList, color)
}
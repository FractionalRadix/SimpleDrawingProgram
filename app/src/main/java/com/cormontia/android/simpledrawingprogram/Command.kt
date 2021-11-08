package com.cormontia.android.simpledrawingprogram

import android.graphics.Canvas

interface Command {
    fun execute(view: PaintingView, canvas: Canvas)
}

interface DrawingCommand : Command {
    fun shape(): Shape
    fun color(): Int
}

class DrawLineSegmentCommand(val lineSegment: LineSegment, val color: Int) : DrawingCommand {
    override fun execute(view: PaintingView, canvas: Canvas) =
        view.drawLineSegment(canvas, lineSegment, color)

    override fun shape() = lineSegment
    override fun color() = color
}

class DrawCircleCommand(val circle: Circle, val color: Int) : DrawingCommand {
    override fun execute(view: PaintingView, canvas: Canvas) =
        view.drawCircle(canvas, circle, color)

    override fun shape() = circle
    override fun color() = color
}

class DrawPointsListCommand(val pointsList: PointsList, val color: Int) : DrawingCommand {
    override fun execute(view: PaintingView, canvas: Canvas) =
        view.drawPointsList(canvas, pointsList, color)

    override fun shape() = pointsList
    override fun color() = color
}
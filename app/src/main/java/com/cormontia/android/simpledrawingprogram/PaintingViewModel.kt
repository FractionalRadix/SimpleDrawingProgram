package com.cormontia.android.simpledrawingprogram

import androidx.lifecycle.ViewModel

class PaintingViewModel : ViewModel() {

    private val commands = mutableListOf<Command>()
    private var commandIndex = 0

    private val lineSegments = mutableListOf<LineSegment>()
    private val circles = mutableListOf<Circle>()
    private val pointsLists = mutableListOf<PointsList>()

    fun addPointsList(pointsList: PointsList, color:Int) {
        pointsLists.add(pointsList)
        addNewCommand(DrawPointsListCommand(pointsList, color))
    }

    fun addLineSegment(newLineSegment: LineSegment, color: Int) {
        lineSegments.add(newLineSegment)
        addNewCommand(DrawLineSegmentCommand(newLineSegment, color))
    }

    fun addCircle(newCircle: Circle, color: Int) {
        circles.add(newCircle)
        addNewCommand(DrawCircleCommand(newCircle, color))
    }

    private fun addNewCommand(command: Command) {
        while (commands.size > commandIndex) {
            commands.removeLast()
        }
        commands.add(command)
        commandIndex++
    }

    //TODO?~ List.iterator returns a *mutable* Iterator... do we want that...?
    fun commandIterator() = commands.take(commandIndex).iterator()

    fun undo() {
        if (commandIndex > 0) {
            commandIndex--
        }
    }

    fun redo() {
        if (commandIndex < commands.size) {
            commandIndex++
        }
    }

    fun clear() {
        commands.clear()
    }
}
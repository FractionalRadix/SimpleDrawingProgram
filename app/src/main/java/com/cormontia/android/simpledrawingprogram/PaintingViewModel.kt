package com.cormontia.android.simpledrawingprogram

import android.util.Log
import androidx.lifecycle.ViewModel

class PaintingViewModel : ViewModel() {

    private val tag = "SimpleDrawingProgram"

    private val commands = mutableListOf<Command>()
    private var commandIndex = 0

    private val lineSegments = mutableListOf<LineSegment>()
    private val circles = mutableListOf<Circle>()
    private val pointsLists = mutableListOf<PointsList>()

    fun addPointsList(pointsList: PointsList) {
        pointsLists.add(pointsList)
        addNewCommand(DrawPointsListCommand(pointsList))
    }

    fun addLineSegment(newLineSegment: LineSegment) {
        lineSegments.add(newLineSegment)
        addNewCommand(DrawLineSegmentCommand(newLineSegment))
    }

    fun addCircle(newCircle: Circle) {
        circles.add(newCircle)
        addNewCommand(DrawCircleCommand(newCircle))
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
}
package com.cormontia.android.simpledrawingprogram

import androidx.lifecycle.ViewModel
import java.util.*

//TODO!~ Working on an "Undo" functionality.

class Command {
    // Only ONE of these is allowed to have a value in each Command.
    var lineSegment: LineSegment? = null
    get() { return field }
    private set(value) { field = value }

    var circle: Circle? = null
    get() { return field }
    private set(value) { field = value }

    var pointsList: PointsList? = null
    get() { return field }
    private set(value) { field = value }

    constructor(lineSegment: LineSegment) {
        this.lineSegment = lineSegment
    }

    constructor(circle: Circle) {
        this.circle = circle
    }

    constructor(pointsList: PointsList) {
        this.pointsList = pointsList
    }
}

class PaintingViewModel : ViewModel() {

    private val commands = mutableListOf<Command>()
    private var commandIndex = 0

    private val lineSegments = mutableListOf<LineSegment>()
    private val circles = mutableListOf<Circle>()
    private val pointsLists = mutableListOf<PointsList>()

    fun addPointsList(pointsList: PointsList) {
        pointsLists.add(pointsList)
        addNewCommand(Command(pointsList))
    }

    fun addLineSegment(newLineSegment: LineSegment) {
        lineSegments.add(newLineSegment)
        addNewCommand(Command(newLineSegment))
    }

    fun addCircle(newCircle: Circle) {
        circles.add(newCircle)
        addNewCommand(Command(newCircle))
    }

    private fun addNewCommand(command: Command) {
        // Suppose "commands" has 5 elements, numbered 0,1,2,3,4.
        // Suppose "commandIndex" is currently 2.
        // Then upon executing a new command:
        //  - Elements 3, 4, and 5 must be dropped.
        //  - The new command must be added.
        //  - The commandIndex must be increased by 1.

        val discard = commands.size - commandIndex
        if (discard > 0) {
            commands.dropLast(discard)
        }
        commands.add(command)
        commandIndex++

    }

    //TODO?~ For some reason, List.iterator returns a *mutable* Iterator... do we want that...?
    fun commandIterator() = commands.take(commandIndex).iterator()

    fun undo() {
        if (commandIndex > 0) {
            commandIndex--
        }
    }

    fun redo() {
        if (commandIndex < commands.size - 1) {
            commandIndex++
        }
    }

}
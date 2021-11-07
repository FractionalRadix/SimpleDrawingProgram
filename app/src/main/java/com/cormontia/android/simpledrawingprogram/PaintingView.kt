package com.cormontia.android.simpledrawingprogram

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View

class PaintingView : View {

    //TODO!+ Add an Eraser

    //TODO!+ Helper #1: if something looks like a straight line, or circle, make it a straight line or circle - instead of a list of points.
    //TODO!+ Helper #2: if something looks like a stick figure, make it a stick figure... and allow it to rotate! And to move its limbs.

    val tag = "SimpleDrawingProgram"

    //private var activeLineSegments = mutableMapOf<Int, LineSegment>()
    private var activePointsLists = mutableMapOf<Int, PointsList>()

    var selectedColor = R.color.red
        set(value) { field = value }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {
    }

    private fun owningActivity(): MainActivity? =
        if (context is MainActivity)
            context as MainActivity
        else
            null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // TODO: consider storing these as member variables to reduce
        // allocations per draw cycle.
        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val paddingRight = paddingRight
        val paddingBottom = paddingBottom

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        activePointsLists.forEach {
            entry -> drawPointsList(canvas, entry.value, selectedColor)
        }

        val owner = owningActivity()
        if (owner != null) {
            owner.commandIterator().forEach { draw(canvas, it) }
        }
    }

    private fun draw(canvas: Canvas, command: Command) {
        command.execute(this, canvas)
    }

    fun drawPointsList(canvas: Canvas, pointsList: PointsList, color: Int) {
        val paint = Paint()
        paint.style = Paint.Style.FILL
        //paint.color = pointsList.color
        paint.color = color
        paint.strokeWidth = 3f
        //TODO?+ strokeMiter ?
        //TODO?~ zipWithNext() might not be very performant here.
        //  (Should we profile?)
        pointsList.points.zipWithNext().forEach {
            canvas.drawLine(it.first.x, it.first.y, it.second.x, it.second.y, paint)
        }
    }

    fun drawLineSegment(canvas: Canvas, lineSegment: LineSegment, color: Int) {
        val paint = Paint()
        paint.style = Paint.Style.FILL
        //paint.color = lineSegment.color
        paint.color = color
        paint.strokeWidth = 3f
        with (lineSegment) {
            canvas.drawLine(p.x, p.y, q.x, q.y, paint)
        }
    }

    fun drawCircle(canvas: Canvas, circle: Circle, color: Int) {
        val paint = Paint()
        paint.style = Paint.Style.STROKE
        //paint.color = circle.color
        paint.color = color
        paint.strokeWidth = 3f

        canvas.drawCircle(circle.center.x, circle.center.y, circle.radius.toFloat(), paint)
    }

    override fun onTouchEvent(evt: MotionEvent): Boolean {
        val pointer = evt.getPointerId(evt.actionIndex)
        when (evt.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // A new pointer has started; there were no pointers before.
                Log.i(tag, "evt.actionMasked == MotionEvent.ACTION_DOWN")
                if (activePointsLists.containsKey(pointer)) {
                    Log.e(tag, "A new pointer is created ($pointer), but there already is a shape using that pointer...")
                }
                activePointsLists[pointer] = PointsList(mutableListOf())
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                Log.i(tag, "evt.actionMasked == MotionEvent.ACTION_POINTER_DOWN")
                // A new pointer has started; there were other pointers already active.
                if (activePointsLists.containsKey(pointer)) {
                    Log.e(tag, "A new pointer is created ($pointer), but there already is a shape using that pointer...")
                }
                activePointsLists[pointer] = PointsList(mutableListOf())
            }
            MotionEvent.ACTION_UP -> {
                // The primary pointer has gone up.
                Log.i(tag, "evt.actionMasked == MotionEvent.ACTION_UP")

                val pointsList = activePointsLists[pointer]
                if (pointsList != null) { // ... and it better be...
                    owningActivity()?.addPointsListToViewModel(pointsList)
                    activePointsLists.remove(pointer)
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                // A non-primary pointer has gone up.
                Log.i(tag, "evt.actionMasked == MotionEvent.ACTION_POINTER_UP")

                val pointsList = activePointsLists[pointer]
                if (pointsList != null) { // ... and it better be...
                    owningActivity()?.addPointsListToViewModel(pointsList)
                    activePointsLists.remove(pointer)
                }
            }
        }

        for (pointerIdx in 0 until evt.pointerCount) {
            val pointerId = evt.getPointerId(pointerIdx)
            //if (activeLineSegments.containsKey(pointerId)) {
            if (activePointsLists.containsKey(pointerId)) {
                val x = evt.getX(pointerIdx)
                val y = evt.getY(pointerIdx)
                val p = PointF(x, y)
                //activeLineSegments[pointerId]?.points?.add(p)
                activePointsLists[pointerId]?.add(p)
            }
        }

        invalidate()
        return true
    }
}
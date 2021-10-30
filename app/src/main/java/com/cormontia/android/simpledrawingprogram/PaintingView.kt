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

    //TODO!+ Add an Undo
    //TODO!+ Add an Eraser
    //TODO!+ Add a menu. It should contain (at least) the options "Save", "Load", and "New".

    //TODO!+ Helper #1: if something looks like a straight line, or circle, make it a straight line or circle - instead of a list of points.
    //TODO!+ Helper #2: if something looks like a stick figure, make it a stick figure... and allow it to rotate! And to move its limbs.

    val tag = "SimpleDrawingProgram"

    private var activeLineSegments = mutableMapOf<Int, LineSegment>()

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

        activeLineSegments.forEach {
            entry -> drawLineSegment(canvas, entry.component2())
        }

        val owner = owningActivity()
        if (owner != null) {
            val lineSegmentIterator = owner.lineSegmentIterator()
            while (lineSegmentIterator.hasNext()) {
                val segment = lineSegmentIterator.next()
                drawLineSegment(canvas, segment)
            }

            val circleIterator = owner.circleIterator()
            while (circleIterator.hasNext()) {
                val circle = circleIterator.next()
                drawCircle(canvas, circle)
            }

            //owner.circleIterator().forEach { it -> drawCircle(canvas, it) }

        }
    }

    private fun drawLineSegment(canvas: Canvas, lineSegment: LineSegment) {
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = lineSegment.color
        paint.strokeWidth = 3f
        //TODO?+ strokeMiter ?
        //TODO?~ zipWithNext() might not be very performant here.
        //  (Should we profile?)
        lineSegment.points.zipWithNext().forEach {
            canvas.drawLine(it.first.x, it.first.y, it.second.x, it.second.y, paint)
        }
    }

    private fun drawCircle(canvas: Canvas, circle: Circle) {
        val paint = Paint()
        paint.style = Paint.Style.FILL
        paint.color = circle.color
        paint.strokeWidth = 3f

        canvas.drawCircle(circle.center.x, circle.center.y, circle.radius.toFloat(), paint)
    }

    override fun onTouchEvent(evt: MotionEvent): Boolean {
        //Log.i(tag,"In onTouchEvent")
        val pointer = evt.getPointerId(evt.actionIndex)
        when (evt.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // A new pointer has started; there were no pointers before.
                Log.i(tag, "evt.actionMasked == MotionEvent.ACTION_DOWN")
                if (activeLineSegments.containsKey(pointer)) {
                    Log.e(tag, "A new pointer is created ($pointer), but there already is a shape using that pointer...")
                }
                activeLineSegments[pointer] = LineSegment(mutableListOf(), selectedColor)
                //activeLineSegments[pointer]?.points?.add(PointF(evt.x, evt.y))
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                Log.i(tag, "evt.actionMasked == MotionEvent.ACTION_POINTER_DOWN")
                // A new pointer has started; there were other pointers already active.
                if (activeLineSegments.containsKey(pointer)) {
                    Log.e(tag, "A new pointer is created ($pointer), but there already is a shape using that pointer...")
                }
                activeLineSegments[pointer] = LineSegment(mutableListOf(), selectedColor)
            }
            MotionEvent.ACTION_UP -> {
                // The primary pointer has gone up.
                Log.i(tag, "evt.actionMasked == MotionEvent.ACTION_UP")

                val lineSegment = activeLineSegments[pointer]
                if (lineSegment != null) {   // ... and it better be...
                    owningActivity()?.addLineSegmentToViewModel(lineSegment)
                    activeLineSegments.remove(pointer)
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                // A non-primary pointer has gone up.
                Log.i(tag, "evt.actionMasked == MotionEvent.ACTION_POINTER_UP")

                val lineSegment = activeLineSegments[pointer]
                if (lineSegment != null) {   // ... and it better be...
                    owningActivity()?.addLineSegmentToViewModel(lineSegment)
                    activeLineSegments.remove(pointer)
                }
            }
        }

        for (pointerIdx in 0 until evt.pointerCount) {
            val pointerId = evt.getPointerId(pointerIdx)
            if (activeLineSegments.containsKey(pointerId)) {
                val x = evt.getX(pointerIdx)
                val y = evt.getY(pointerIdx)
                val p = PointF(x, y)
                activeLineSegments[pointerId]?.points?.add(p)
            }
        }

        invalidate()
        return true
    }

    fun oldOnTouchEvent(evt: MotionEvent): Boolean {
        //Log.i("SimpleDrawingProgram", "Entered onTouchEvent")

        //TODO?~ Use the amount of pressure to determine line width?

        val activePointers = activeLineSegments.keys
        val pointersInEvent = (0 until evt.pointerCount).map { idx -> evt.getPointerId(idx) }
        val activePointersNotInEvent = activePointers.filter { x -> x !in pointersInEvent }
        for (pointer in activePointersNotInEvent) {
            val lineSegment = activeLineSegments[pointer]
            if (lineSegment != null) {
                owningActivity()?.addLineSegmentToViewModel(lineSegment)
                activeLineSegments.remove(pointer)
            }
        }

        for (pointerIndex in 0 until evt.pointerCount) {
            val cx = evt.getX(pointerIndex)
            val cy = evt.getY(pointerIndex)
            val pointer = evt.getPointerId(pointerIndex)

            if (activeLineSegments[pointer] == null) {
                activeLineSegments[pointer] = LineSegment(mutableListOf(), selectedColor)
            }

            activeLineSegments[pointer]?.points?.add(PointF(cx, cy))
        }

        invalidate()
        return true
    }
}
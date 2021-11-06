package com.cormontia.android.simpledrawingprogram

import android.graphics.Color
import android.graphics.PointF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import androidx.activity.viewModels
import kotlin.math.sqrt

//TODO!+ Add a menu. It should contain (at least) the options "Save", "Load", and "New".

class MainActivity : AppCompatActivity() {
    private val viewModel: PaintingViewModel by viewModels()
    private lateinit var paintingView: PaintingView
    private val tag = "SimpleDrawingProgram"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        paintingView = findViewById(R.id.paintingView)

        /*
        // Android weirdness:
        // R.color.red is set to 0xFFFF0000. But tmp1 is 2131034333 (0x7F0500DD)
        // Presumably R.color.red is an _ID_ rather than a value... but then how to get the value?
        val tmp1 = R.color.red
        Log.i("SimpleDrawingProgram", "R.color.red==$tmp1")
        // R.color.orange is set to 0xFFFF8000. But tmp2 is 2131034332 (0x7F0500DC)
        // Presumably R.color.orange is an _ID_ rather than a value... but then how to get the value?
        val tmp2 = R.color.orange
        Log.i("SimpleDrawingProgram", "R.color.orange==$tmp2")
         */
    }

    private fun square(x: Double) = x * x

    /**
     * Given a set of points, check if they roughly form a circle.
     * If yes, draw the circle; if no, return <code>false</code>.
     * @param pointsList A list of 2D points.
     * @return <code>true</code> if and only if a circle was drawn.
     */
    private fun drawIfCircle(pointsList: PointsList) : Boolean {
        val meanX = pointsList.points.map { p -> p.x }.average()
        val meanY = pointsList.points.map { p -> p.y }.average()

        val squaredDistances    = pointsList.points.map { p -> square(p.x - meanX) + square(p.y - meanY) }
        val meanSquaredDistance = squaredDistances.average()

        // Determining if our points approximate a circle.
        // (Standard deviation is not the way to go. Standard deviation in a straight line seems way smaller than in a circle...)
        // Naive algorithm (to be replaced by a least squares approximation later):
        // Take the circle with its center at (meanX, meanY), and radius equal to the average distance.
        val distancesToCenter = pointsList.points.map { p -> sqrt( square(p.x - meanX) + square(p.y - meanY )) }
        val radius = distancesToCenter.average()
        // Then determine, for each point, how far away it is from this perfect circle.
        val differences = distancesToCenter.map { it / radius }
        // If all points are within 80%-120% of the radius, we have us a circle.
        // ... note that small straight lines might also satisfy this criterion!
        // ....We need to find a way to determine if the points are spread AROUND our circle.
        if (differences.all { it in 0.8 .. 1.2 }) {
            val newCircle = Circle(
                PointF(meanX.toFloat(), meanY.toFloat()),
                sqrt(meanSquaredDistance),
                pointsList.color //drawingColor
            )
            viewModel.addCircle(newCircle)
            return true
        }
        return false
    }

    /**
     * Given a set of points, check if they approximate a segment of a straight line.
     * If yes, draw the line; if no, return <code>false</code>.
     * @param pointsList A list of 2D points.
     * @return <code>true</code> if and only if a line segment was drawn.
     */
    private fun drawIfStraightLine(pointsList: PointsList) : Boolean {
        val meanX = pointsList.points.map { p -> p.x }.average()
        val meanY = pointsList.points.map { p -> p.y }.average()

        // Next, let's try if it's a straight line.
        val p0 = pointsList.points[0]
        val p1 = pointsList.points.last()

        var q0: PointF
        var q1: PointF

        // Assume that the points should form a straight line segment.
        // A line segment is part of a line; we determine the line using least squares approximation.
        // After that, we need the starting point and ending point of our line segment.
        // The starting point and ending point of our line _segment_ are those points on the line, that are closest to our first and last point.

        // First, determine the lien.
        // The slope of the line is a rational, we can determine its numerator and denominator:
        val numerator = pointsList.points.sumOf { p -> (p.x - meanX) * (p.y - meanY) }
        val denominator = pointsList.points.sumOf { p -> square(p.x - meanX) }

        // There are two special cases to take care of: if the line is horizontal, or vertical.
        // If numerator == 0, then the line is horizontal.
        // If denominator == 0, then the line is vertical.
        // In both cases, the first and last point drawn, are also the first and last point of the idealized line segment.
        if (numerator == 0.0 || denominator == 0.0) {
            q0 = PointF(p0.x, p0.y)
            q1 = PointF(p1.x, p1.y)
            val idealizedLineSegment = LineSegment(q0, q1, pointsList.color)
            viewModel.addLineSegment(idealizedLineSegment)
            return true
        } else {
            val slope = numerator / denominator
            val yIntercept = meanY - slope * meanX

            //TODO!+ What happens to these values (squaredDifferences, Syy) if numerator==0?
            //TODO!+ What happens to these values (squaredDifferences, Syy) if denominator==0?
            // Before we continue, let's check if a straight line really is a good fit for the data...
            // First, calculate SSr: the sum of the squared difference between the points on the line and the points drawn.

            val squaredDifferences = pointsList.points
                .map { square(it.y - slope * it.x - yIntercept) }
                .sum()
            // Second, determine Syy, the sum of the squared differences of the y values:
            val Syy = pointsList.points
                .map { square(it.y - meanY) }
                .sum()
            val coefficientOfDetermination = 1 - squaredDifferences / Syy
            //NOTE. If Syy==0, then we'd have a division by zero. Note however that this can only happen if EVERY y is equal to meanY.
            // So, this would only happen if the line were horizontal.
            val indexOfFit = sqrt(coefficientOfDetermination)
            //TODO?~ Find proof that coefficientOfDetermination >= 0?

            //TODO?~ Change the sensitivity? The closer to 1.0, the more of a straight line the data already is.
            if (indexOfFit >= 0.95) {
                // The points fit a straight line.

                // Now we need to find the points on this line, that are closest to our starting point and ending point.
                val q0x = (p0.y - p0.x / slope - yIntercept) / (slope - 1 / slope)
                val q0y = (1 / slope) * p0.x + p0.y - p0.x / slope
                q0 = PointF(q0x.toFloat(), q0y.toFloat())

                val q1x = (p1.y - p1.x / slope - yIntercept) / (slope - 1 / slope)
                val q1y = (1 / slope) * p1.x + p1.y - p1.x / slope
                q1 = PointF(q1x.toFloat(), q1y.toFloat())

                val idealizedLineSegment = LineSegment(q0, q1, pointsList.color)
                viewModel.addLineSegment(idealizedLineSegment)
                return true
            }

        }
        return false
    }

    //TODO!~ Move the maths functions to a separate class.
    // Then things like the mean values can be calculated in an instance of that class, and re-used by "drawIfCircle" and "drawIfStraightLine".
    // This saves pointless re-calculation.
    /**
     * Add a list of points to the ViewModel, optionally replacing it with an "idealized" form.
     * If idealization is on, then this method will check if the points list resembles a circle, a line segment, or other basic shape.
     * It will then replace the points list with this "perfect" version of the shape the user drew.
     * @param pointsList A list of points in the (X,Y) plane.
     * @param idealize When `true`, replace the points list with an idealized shape.
     */
    fun addPointsListToViewModel(pointsList: PointsList, idealize: Boolean = true) {

        //TODO?+ Guard against the situation where we have 0 points? Should not occur in practice...

        if (!idealize) {
            viewModel.addPointsList(pointsList)
        } else if (drawIfCircle(pointsList)) {
            return
        } else if (drawIfStraightLine(pointsList)) {
            return
        } else {
            // If it's neither a circle nor a line, let's draw the points that the user REALLY drew:
            viewModel.addPointsList(pointsList)
        }
    }


    fun commandIterator(): Iterator<Command> = viewModel.commandIterator()

    //TODO!~ Set this in a ViewModel...
    //var drawingColor = viewModel.selectedColor
    var drawingColor = Color.RED

    fun colorSelected(view: android.view.View) {
        if (view is RadioButton) {
            val checked = view.isChecked
            when (view.id) {
                R.id.red -> if (checked) { drawingColor = 0xFFFF0000.toInt() /* R.color.red */ }
                R.id.orange -> if (checked) { drawingColor = 0xFFFF8000.toInt() /* R.color.orange */ }
                R.id.yellow -> if (checked) { drawingColor = 0xFFFFFF36.toInt() /* R.color.yellow */ }
                R.id.green -> if (checked) { drawingColor = 0xFF00FF00.toInt() /* R.color.green */ }
                R.id.blue -> if (checked) { drawingColor = 0xFF0000FF.toInt() /* R.color.blue */ }
                R.id.indigo -> if (checked) { drawingColor = 0xFF4B4382.toInt() /* R.color.indigo */ }
                R.id.violet -> if (checked) { drawingColor = 0xFF5601AF.toInt() /* R.color.violet */ }
            }
            //viewModel.selectedColor = drawingColor
            // Apparently the View should NOT get information from the ViewModel.
            // The Activity should get information from the ViewModel, then pass it to the View.
            // (Source: https://sapandiwakar.in/accessing-viewmodel-inside-views-on-android-2/)
            Log.i(tag, "drawingColor==$drawingColor")
            paintingView.selectedColor = drawingColor
            Log.i(tag, "Selected color in paintingView==${paintingView.selectedColor}")
        }
        Log.i(tag, "Selected color = $drawingColor")
    }

    fun clear(view: android.view.View) {
        //TODO!+
    }

    fun load(view: android.view.View) {
        //TODO!+
    }

    fun save(view: android.view.View) {
        //TODO!+
    }

    fun undo(view: android.view.View) {
        viewModel.undo()
        val paintingView = findViewById<PaintingView>(R.id.paintingView)
        paintingView.invalidate()
    }

    fun redo(view: android.view.View) {
        viewModel.redo()
        val paintingView = findViewById<PaintingView>(R.id.paintingView)
        paintingView.invalidate()
    }
}
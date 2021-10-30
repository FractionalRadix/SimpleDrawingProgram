package com.cormontia.android.simpledrawingprogram

import android.graphics.Color
import android.graphics.PointF
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import androidx.activity.viewModels

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

    fun square(x: Double) = x * x

    //fun addLineSegmentToViewModel(lineSegment: LineSegment) {
    fun addPointsListToViewModel(pointsList: PointsList) {
        Log.i(tag, "MainActivity.addLineSegmentToViewModel(...): adding line segment...")

        // The line that the user ACTUALLY drew. Only adding it later...
        //viewModel.addPointsList(pointsList)

        val addIdealizedShape = true
        if (!addIdealizedShape) {
            viewModel.addPointsList(pointsList)
        } else {
            // Second (under development): an idealized version of the line or circle that the user drew.
            //TODO?+ Guard against the situation where we have 0 points? Should not occur in practice...
            val meanX = pointsList.points.map { p -> p.x }.average()
            val meanY = pointsList.points.map { p -> p.y }.average()

            Log.i(tag, "mean x == $meanX, mean y == $meanY")

            val squaredDistances    = pointsList.points.map { p -> square(p.x - meanX) + square(p.y - meanY) }
            val meanSquaredDistance = squaredDistances.average()

            // Determining if our points approximate a circle.
            // (Standard deviation is not the way to go. Standard deviation in a straight line seems way smaller than in a circle...)
            // Naive algorithm (to be replaced by a least squares approximation later):
            // Take the circle with its center at (meanX, meanY), and radius equal to the average distance.
            val distancesToCenter = pointsList.points.map { p -> Math.sqrt( square(p.x - meanX) + square(p.y - meanY )) }
            val radius = distancesToCenter.average()
            // Then determine, for each point, how far away it is from this perfect circle.
            val differences = distancesToCenter.map { it / radius }
            // If all points are within 80%-120% of the radius, we have us a circle.
            // ... note that small straight lines might also satisfy this criterion!
            // ....We need to find a way to determine if the points are spread AROUND our circle.
            if (differences.all { it in 0.8 .. 1.2 }) {
                val newCircle = Circle(
                    PointF(meanX.toFloat(), meanY.toFloat()),
                    Math.sqrt(meanSquaredDistance),
                    drawingColor
                )
                viewModel.addCircle(newCircle)

            } else {

                //TODO!~ Add criteria for when the result simply ISN'T a straight line segment.

                val numerator = pointsList.points.sumOf { p -> (p.x - meanX) * (p.y - meanY) }
                val denominator = pointsList.points.sumOf { p -> square(p.x - meanX) }
                //TODO?+ Guard against denominator == 0.0 ? Note that this can only happen if p.x == meanX all over the line.
                val slope = numerator / denominator
                val yIntercept = meanY - slope * meanX

                // Now the idealized line is described by y = slope * x + yIntercept.
                // But where does the idealized line SEGMENT start, and where does it end?

                // Also, what happens if the input is a perfect vertical line?

                //TEMPORARY, for testing:
                val startIdealizedLineSegment = pointsList.points[0]
                val endpoint = pointsList.points.last()
                //val endIdealizedLineSegment = PointF(endpoint.x, (slope * endpoint.x + yIntercept).toFloat())

                val idealizedLineSegment = LineSegment(startIdealizedLineSegment, endpoint, pointsList.color)

                viewModel.addLineSegment(idealizedLineSegment)

                // AND the line that the user REALLY drew:
                viewModel.addPointsList(pointsList)
            }
        }
    }

    fun pointsListsIterator(): Iterator<PointsList> = viewModel.pointsListsIterator()
    fun lineSegmentIterator(): Iterator<LineSegment> = viewModel.lineSegmentIterator()
    fun circleIterator(): Iterator<Circle> = viewModel.circleIterator()


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
}
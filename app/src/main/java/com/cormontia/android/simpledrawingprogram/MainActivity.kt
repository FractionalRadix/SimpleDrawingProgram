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

    fun addLineSegmentToViewModel(lineSegment: LineSegment) {
        Log.i("SimpleDrawingProgram", "MainActivity.addLineSegmentToViewModel(...): adding line segment...")

        // First: add the line that the user ACTUALLY drew
        viewModel.addLineSegment(lineSegment)

        val addIdealizedShape = false
        if (addIdealizedShape) {
            // Second (under development): an idealized version of the line that the use drew.
            //TODO?+ Guard against the situation where we have 0 points? Should not occur in practice...
            val meanX = lineSegment.points.map { p -> p.x }.average()
            val meanY = lineSegment.points.map { p -> p.y }.average()


            val squaredDistances =
                lineSegment.points.map { p -> square(p.x - meanX) + square(p.y - meanY) }
            val meanSquaredDistance = squaredDistances.average()
            //TODO!~ Make sure squaredDistances.size > 1. If squaredDistances.size <= 3 there's definitely not a circle here anyway...
            val varianceInSquaredDistances =
                squaredDistances.sumOf { square(it - meanSquaredDistance) } / (squaredDistances.size - 1)
            // If all squared distances are within 1 time standard deviation from the center, then let's assume it's a circle.
            // ("1 time within SD" is an experimental value... may need 0.5 SD, or 2.0 SD...)
            val isCircle = squaredDistances.all { it < Math.sqrt(varianceInSquaredDistances) }
            if (false) {
                //TODO?~ Is this correct? Or should the center and radius be calculated using a more complex formula?
                val newCircle = Circle(
                    PointF(meanX.toFloat(), meanY.toFloat()),
                    Math.sqrt(meanSquaredDistance),
                    drawingColor
                )
                viewModel.addCircle(newCircle)
            } else {
                val numerator = lineSegment.points.sumOf { p -> (p.x - meanX) * (p.y - meanY) }
                val denominator = lineSegment.points.sumOf { p -> square(p.x - meanX) }
                //TODO?+ Guard against denominator == 0.0 ? Note that this can only happen if p.x == meanX all over the line.
                val slope = numerator / denominator
                val yIntercept = meanY - slope * meanX

                // Now the idealized line is described by y = slope * x + yIntercept.
                // But where does the idealized line SEGMENT start, and where does it end?

                // Also, what happens if the input is a perfect vertical line?

                //TEMPORARY, for testing:
                val startIdealizedLineSegment = lineSegment.points[0]
                val endpoint = lineSegment.points.last()
                val endIdealizedLineSegment =
                    PointF(endpoint.x, (slope * endpoint.x + yIntercept).toFloat())
                val idealizedLineSegment = LineSegment(
                    mutableListOf(startIdealizedLineSegment, endIdealizedLineSegment),
                    lineSegment.color
                )
                viewModel.addLineSegment(idealizedLineSegment)
            }
        }
    }

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
            Log.i("SimpleDrawingProgram", "drawingColor==$drawingColor")
            paintingView.selectedColor = drawingColor
            Log.i("SimpleDrawingProgram", "Selected color in paintingView==${paintingView.selectedColor}")
        }
        Log.i("SimpleDrawingProgram", "Selected color = $drawingColor")
    }
}
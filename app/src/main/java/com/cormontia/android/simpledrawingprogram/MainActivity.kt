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
            viewModel.addPointsList(pointsList, drawingColor)
        } else {
            // Is it a circle? If so, draw the circle.
            val newCircle = ShapeRecognizer(pointsList).drawIfCircle()
            if (newCircle != null) {
                viewModel.addCircle(newCircle, drawingColor)
                return
            }

            // Is it a line segment? If so, draw the line segment.
            val newLineSegment = ShapeRecognizer(pointsList).drawIfStraightLine()
            if (newLineSegment != null) {
                viewModel.addLineSegment(newLineSegment, drawingColor)
                return
            }

            // If it's neither a circle nor a line, let's draw the points that the user REALLY drew:
            viewModel.addPointsList(pointsList, drawingColor)
        }
    }


    fun commandIterator(): Iterator<Command> = viewModel.commandIterator()

    //TODO!~ Set this in a ViewModel...
    //var drawingColor = viewModel.selectedColor
    private var drawingColor = Color.RED

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
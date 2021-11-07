package com.cormontia.android.simpledrawingprogram

import android.graphics.PointF
import kotlin.math.sqrt

//TODO!~ Instantiate the ShapeRecognizer with a PointsList.
// Then calculate meanX and meanY ahead of time.
class ShapeRecognizer(private val pointsList: PointsList) {

    //TODO?~ Guard against the case that pointsList.points.size == 0 ?

    private val meanX = pointsList.points.map { p -> p.x }.average()
    private val meanY = pointsList.points.map { p -> p.y }.average()

    private fun square(x: Double) = x * x

    /**
     * Determine if our points approximate a circle.
     * If yes, return the circle; if no, return <code>null</code>.
     * @return The circle approximated by the points, or <code>null</code> if the points do not approximate a circle.
     */
    fun drawIfCircle() : Circle? {

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
            return Circle(
                PointF(meanX.toFloat(), meanY.toFloat()),
                sqrt(meanSquaredDistance),
            )
        }
        return null
    }

    /**
     * Determine if our points approximate a segment of a straight line.
     * If yes, return the line segment; if no, return <code>null</code>.
     * @return The line segment approximated by the points, or <code>null</code> if the points do not approximate a line segment.
     */
    fun drawIfStraightLine() : LineSegment? {

        val p0 = pointsList.points[0]
        val p1 = pointsList.points.last()

        val q0: PointF
        val q1: PointF

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
            return LineSegment(q0, q1)
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

                return LineSegment(q0, q1)
            }

        }
        return null
    }

}
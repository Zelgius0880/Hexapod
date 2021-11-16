package com.zelgius.remoteController.hexapod

import kotlin.math.*


abstract class Gait {
    companion object {
        //coxa-to-toe home positions
        val HOME = arrayOf(
            Point(82.0, 82.0, -80.0),
            Point(0.0, 116.0, -80.0),
            Point(-82.0, 82.0, -80.0),
            Point(-82.0, -82.0, -80.0),
            Point(0.0, -116.0, -80.0),
            Point(82.0, -82.0, -80.0)
        )

        val BODY = arrayOf(
            Point(110.4, 58.4, 0.0),
            Point(0.0, 90.8, 0.0),
            Point(-110.4, 58.4, 0.0),
            Point(-110.4, -58.4, 0.0),
            Point(0.0, -90.8, 0.0),
            Point(110.4, -58.4, 0.0)
        )

        const val MAX_AXIS = 1300.0
        const val FRAME_TIME_MS = 20L

        const val SPEED_LOW = 1080

        //const val FRAME_TIME_MS = 100L
        const val SPEED_HIGH = SPEED_LOW / 3
        const val A12DEG = 209440L         //12 degrees in radians x 1,000,000
    }

    abstract val case: IntArray
    abstract val numTicksDivider: Double

    private val stepHeightMultiplier = 1.0

    val current = Array(6) { HOME[it].copy() }
    var tick = 0

    private var duration = SPEED_HIGH
    var sinRotationZ = 0.0
    var cosRotationZ = 0.0
    var isFast = true

    val numTicks get() = (duration / FRAME_TIME_MS / numTicksDivider)

    fun computeMovement(r: Point, l: Point): List<Point> {

        val stride = computeStrides(
            Point(
                if (l.x.absoluteValue > r.y.absoluteValue) l.x else r.x, l.y, r.y
            )
        )
        (0 until 6).forEach { leg ->
            val amplitude = computeAmplitudes(leg, stride)
            computePosition(numTicks, amplitude, leg)
        }

        if (tick < numTicks - 1) ++tick
        else tick = 0


        return current.toList()
    }

    private fun computeStrides(commanded: Point): Point {
        val stride = Point(
            90.0 * commanded.x / MAX_AXIS,
            90.0 * commanded.y / MAX_AXIS,
            35.0 * commanded.z / MAX_AXIS
        )

        sinRotationZ = sin(Math.toRadians(stride.z))
        cosRotationZ = cos(Math.toRadians(stride.z))

        duration = if (!isFast) SPEED_LOW else SPEED_HIGH
        return stride
    }

    private fun computeAmplitudes(leg: Int, stride: Point): Point {
        val total = Point(
            HOME[leg].x + BODY[leg].x,
            HOME[leg].y + BODY[leg].y
        )

        val rotOffsetX = total.y * sinRotationZ + total.x * cosRotationZ - total.x
        val rotOffsetY = total.y * cosRotationZ - total.x * sinRotationZ - total.y

        return Point(
            ((stride.x + rotOffsetX) / 2.0).coerceIn(-50.0, 50.0),
            ((stride.y + rotOffsetY) / 2.0).coerceIn(-50.0, 50.0),
            if ((stride.x + rotOffsetX).absoluteValue > (stride.y + rotOffsetY).absoluteValue)
                stepHeightMultiplier * (stride.x + rotOffsetX) / 4.0
            else
                stepHeightMultiplier * (stride.y + rotOffsetY) / 4.0
        )
    }

    abstract fun computePosition(numTicks: Double, amplitude: Point, leg: Int)

    fun moveAxes(p: Point) {
        //compute rotation sin/cos values using controller inputs
        //compute rotation sin/cos values using controller inputs
        val sinRotX = sin(map(p.x, -20.0, 20.0, A12DEG.toDouble(), -A12DEG.toDouble()) / 1000000.0)
        val cosRotX = cos(map(p.x, -20.0, 20.0, A12DEG.toDouble(), -A12DEG.toDouble()) / 1000000.0)
        val sinRotY = sin(map(p.y, -20.0, 20.0, A12DEG.toDouble(), -A12DEG.toDouble()) / 1000000.0)
        val cosRotY = cos(map(p.y, -20.0, 20.0, A12DEG.toDouble(), -A12DEG.toDouble()) / 1000000.0)

        // these variables are for the yaw, but it is already computed from the right stick
        val sinRotZ = sin(0.0)
        val cosRotZ = cos(0.0)

        for (leg_num in 0..5) {
            //compute total distance from center of body to toe
            val total = Point(
                HOME[leg_num].x + BODY[leg_num].x,
                HOME[leg_num].y + BODY[leg_num].y,
                HOME[leg_num].z + BODY[leg_num].z,
            )

            //perform 3 axis rotations
            val rotOffsetX = (total.x * cosRotY * cosRotZ
                    + total.y * sinRotX * sinRotY * cosRotZ
                    + total.y * cosRotX * sinRotZ
                    - total.z * cosRotX * sinRotY * cosRotZ
                    + total.z * sinRotX * sinRotZ
                    - total.x)

            val rotOffsetY = (-total.x * cosRotY * sinRotZ
                    - total.y * sinRotX * sinRotY * sinRotZ
                    + total.y * cosRotX * cosRotZ
                    + total.z * cosRotX * sinRotY * sinRotZ
                    + total.z * sinRotX * cosRotZ
                    - total.y)

            val rotOffsetZ = (total.x * sinRotY
                    - total.y * sinRotX * cosRotY
                    + total.z * cosRotX * cosRotY
                    - total.z)

            // Calculate foot positions to achieve desired rotation
            current[leg_num].x = HOME[leg_num].x + rotOffsetX
            current[leg_num].y = HOME[leg_num].y + rotOffsetY
            current[leg_num].z = HOME[leg_num].z + rotOffsetZ

            //lock in offsets if commanded
            /*if (capture_offsets === true) {
                offset[leg_num].z = offset[leg_num].z + rotOffsetX
                offset_Y.get(leg_num) = offset_Y.get(leg_num) + rotOffsetY
                offset_Z.get(leg_num) = offset_Z.get(leg_num) + rotOffsetZ + translateZ
               current[leg_num].x = HOME[leg_num].x
               current[leg_num].y = HOME[leg_num].y
               current[leg_num].z = HOME[leg_num].z
            }*/
        }

        //if offsets were commanded, exit current mode
        /*if (capture_offsets === true) {
            capture_offsets = false
            mode = 0
        }*/
    }

    private fun map(x: Double, inMin: Double, inMax: Double, outMin: Double, outMax: Double): Double {
        return (x - inMin) * (outMax - outMin) / (inMax - inMin) + outMin
    }
}

data class Point(var x: Double = 0.0, var y: Double = 0.0, var z: Double = 0.0) {
    companion object {
        fun forLeg(coxa: Double, femur: Double, tibia: Double) = Point(x = coxa, y = femur, z = tibia)
    }

    val isDeadBand: Boolean
        get() = abs(x) <= 15 && abs(y) <= 15 && abs(z) <= 15

}

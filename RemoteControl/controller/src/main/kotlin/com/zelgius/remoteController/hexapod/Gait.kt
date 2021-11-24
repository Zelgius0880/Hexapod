package com.zelgius.remoteController.hexapod

import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin


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
        const val A12DEG = 209440.0         //12 degrees in radians x 1,000,000
        const val A30DEG = 523599.0         //30 degrees in radians x 1,000,000
    }

    abstract val case: IntArray
    abstract val numTicksDivider: Double


    protected val current = Array(6) { HOME[it].copy() }
    var tick = 0

    private var duration = SPEED_HIGH
    private var sinRotationZ = 0.0
    private var cosRotationZ = 0.0
    var isFast = true

    private val offset = Array(6) { Point() }


    private val numTicks get() = (duration / FRAME_TIME_MS / numTicksDivider)

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


        return current.mapIndexed { index, point ->
            point + offset[index]
        }
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
                (stride.x + rotOffsetX) / 4.0
            else
                (stride.y + rotOffsetY) / 4.0
        )
    }

    abstract fun computePosition(numTicks: Double, amplitude: Point, leg: Int)

    fun computeOffsetAxes(p: Point) {
        //compute rotation sin/cos values using controller inputs
        val sinRotX = sin(map(p.x, 20.0, -20.0, A12DEG, -A12DEG) / 1000000.0)
        val cosRotX = cos(map(p.x, 20.0, -20.0, A12DEG, -A12DEG) / 1000000.0)
        val sinRotY = sin(map(p.y, 20.0, -20.0, A12DEG, -A12DEG) / 1000000.0)
        val cosRotY = cos(map(p.y, 20.0, -20.0, A12DEG, -A12DEG) / 1000000.0)
        // these variables are for the yaw, but it is already computed from the right stick
        val sinRotZ = sin(0.0)
        val cosRotZ = cos(0.0)

        val translateZ = if (p.z > 0)
            map(p.z, 1.0, 20.0, 0.0, 30.0)
        else
            map(p.z, -20.0, 00.0, -3 * 30.0, 0.0)


        for (leg in 0..5) {
            //compute total distance from center of body to toe
            val total = Point(
                HOME[leg].x + BODY[leg].x,
                HOME[leg].y + BODY[leg].y,
                HOME[leg].z + BODY[leg].z,
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

            offset[leg] = Point(rotOffsetX, rotOffsetY, rotOffsetZ + translateZ)
        }
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

    operator fun plus(point: Point): Point = Point(
        x = x + point.x,
        y = y + point.y,
        z = z + point.z,
    )

    operator fun div(double: Double) = Point(
        x = x / double,
        y = y / double,
        z = z / double
    )
}

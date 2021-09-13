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
            Point(82.0 - 82.0 - 80.0)
        )

        val BODY = arrayOf(
            Point(110.4, 58.4, 0.0),
            Point(0.0, 90.8, 0.0),
            Point(-110.4, 58.4, 0.0),
            Point(-110.4, -58.4, 0.0),
            Point(0.0, -90.8, 0.0),
            Point(110.4, -58.4, 0.0)
        )


        const val STEP_HEIGHT_MULTIPLIER = 1.0

        const val MAX_AXIS = 1210
        const val FRAME_TIME_MS = 20
        const val SPEED_HIGH = 3240
        const val SPEED_LOW = 1080

    }

    abstract val case: IntArray
    abstract val numTicksDivider: Double

    val current = Array(6) { Point() }
    var tick = 0

    var duration = 1080
    var sinRotationZ = 0.0
    var cosRotationZ = 0.0
    var isFast = true

    val numTicks get() = (duration / FRAME_TIME_MS / numTicksDivider).roundToInt()

    fun move(r: Point, l: Point) {

        if (!r.isDeadBand || tick > 0) {
            val stride = computeStrides(Point(l.x, l.y, r.x))
            case.forEachIndexed { leg, case ->
                val amplitude = computeAmplitudes(leg, stride)
                computePosition(amplitude, leg, case)
            }

            if (tick < numTicks - 1) ++tick
            else tick = 0
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

        duration = if (isFast) 3240 else 1080
        return stride
    }

    private fun computeAmplitudes(leg: Int, stride: Point): Point {
        val total = Point(
            HOME[leg].x + BODY[leg].y,
            HOME[leg].y + BODY[leg].y
        )

        val rotOffsetX = total.y * sinRotationZ + total.x * cosRotationZ - total.x
        val rotOffsetY = total.y * cosRotationZ - total.x * sinRotationZ - total.y

        return Point(
            ((stride.x + rotOffsetX) / 2.0).coerceIn(-50.0, 50.0),
            ((stride.y + rotOffsetY) / 2.0).coerceIn(-50.0, 50.0),
            if ((stride.x + rotOffsetX).absoluteValue > (stride.y + rotOffsetY).absoluteValue)
                STEP_HEIGHT_MULTIPLIER * (stride.x + rotOffsetX) / 4.0
            else
                STEP_HEIGHT_MULTIPLIER * (stride.y + rotOffsetY) / 4.0
        )
    }

    fun reset() {
        current.forEachIndexed { leg, point ->
            point.x = HOME[leg].x
            point.y = HOME[leg].y
            point.z = HOME[leg].z
        }
    }

    abstract fun computePosition(amplitude: Point, leg: Int, case: Int)

}

class Point(var x: Double = 0.0, var y: Double = 0.0, var z: Double = 0.0) {
    val isDeadBand: Boolean
        get() = abs(x) <= 15 || abs(y) <= 15 || abs(z) <= 15
}

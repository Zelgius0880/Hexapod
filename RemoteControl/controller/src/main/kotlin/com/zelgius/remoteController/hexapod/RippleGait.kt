package com.zelgius.remoteController.hexapod

import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

class RippleGait : Gait() {
    override val case: IntArray = intArrayOf(2, 6, 4, 1, 3, 5)
    override val numTicksDivider: Double = 2.0

    override fun computePosition(amplitude: Point, leg: Int, case: Int) {
        when (case) {
            1 -> {                               //move foot forward (raise)
                current[leg].x = HOME[leg].x - amplitude.x * cos(PI * tick / (numTicks * 2))
                current[leg].y = HOME[leg].y - amplitude.y * cos(PI * tick / (numTicks * 2))
                current[leg].z = HOME[leg].z + amplitude.z.absoluteValue * sin(PI * tick / (numTicks * 2))
                if (tick >= numTicks - 1) this.case[leg] = 2

            }
            2 -> {                               //move foot forward (lower)
                current[leg].x =
                    HOME[leg].x - amplitude.x * cos(PI * (numTicks + tick) / (numTicks * 2))
                current[leg].y =
                    HOME[leg].y - amplitude.y * cos(PI * (numTicks + tick) / (numTicks * 2))
                current[leg].z =
                    HOME[leg].z + amplitude.z.absoluteValue * sin(PI * (numTicks + tick) / (numTicks * 2))
                if (tick >= numTicks - 1) this.case[leg] = 3

            }
            3 -> {                               //move foot back one-quarter (on the ground)
                current[leg].x = current[leg].x - amplitude.x / numTicks / 2.0
                current[leg].y = current[leg].y - amplitude.y / numTicks / 2.0
                current[leg].z = HOME[leg].z
                if (tick >= numTicks - 1) this.case[leg] = 4

            }
            4 -> {                               //move foot back one-quarter (on the ground)
                current[leg].x = current[leg].x - amplitude.x / numTicks / 2.0
                current[leg].y = current[leg].y - amplitude.y / numTicks / 2.0
                current[leg].z = HOME[leg].z
                if (tick >= numTicks - 1) this.case[leg] = 5

            }
            5 -> {                               //move foot back one-quarter (on the ground)
                current[leg].x = current[leg].x - amplitude.x / numTicks / 2.0
                current[leg].y = current[leg].y - amplitude.y / numTicks / 2.0
                current[leg].z = HOME[leg].z
                if (tick >= numTicks - 1) this.case[leg] = 6

            }
            6 -> {                               //move foot back one-quarter (on the ground)
                current[leg].x = current[leg].x - amplitude.x / numTicks / 2.0
                current[leg].y = current[leg].y - amplitude.y / numTicks / 2.0
                current[leg].z = HOME[leg].z
                if (tick >= numTicks - 1) this.case[leg] = 1

            }
        }
    }
}
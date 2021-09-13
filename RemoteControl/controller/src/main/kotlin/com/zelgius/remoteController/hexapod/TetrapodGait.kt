package com.zelgius.remoteController.hexapod

import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

class TetrapodGait : Gait() {
    override val case: IntArray = intArrayOf(1, 3, 2, 1, 2, 3)
    override val numTicksDivider: Double = 3.0

    override fun computePosition(amplitude: Point, leg: Int, case: Int) {
        when (case) {
            1 -> {                     //move foot forward (raise and lower)
                current[leg].x = HOME[leg].x - amplitude.x * cos(PI * tick / numTicks)
                current[leg].y = HOME[leg].y - amplitude.y * cos(PI * tick / numTicks)
                current[leg].z = HOME[leg].z + amplitude.z.absoluteValue * sin(PI * tick / numTicks)
                if (tick >= numTicks - 1) this.case[leg] = 2
            }
            2 -> {                     //move foot back one-half (on the ground)
                current[leg].x = current[leg].x - amplitude.x / numTicks
                current[leg].y = current[leg].y - amplitude.y / numTicks
                current[leg].z = HOME[leg].z
                if (tick >= numTicks - 1) this.case[leg] = 3
            }
            3 -> {                     //move foot back one-half (on the ground)
                current[leg].x = current[leg].x - amplitude.x / numTicks
                current[leg].y = current[leg].y - amplitude.y / numTicks
                current[leg].z = HOME[leg].z
                if (tick >= numTicks - 1) this.case[leg] = 1
            }
        }
    }
}
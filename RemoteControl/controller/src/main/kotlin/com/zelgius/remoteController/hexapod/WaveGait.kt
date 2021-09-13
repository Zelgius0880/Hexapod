package com.zelgius.remoteController.hexapod

import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

class WaveGait : Gait() {
    override val case: IntArray = intArrayOf(1, 2, 3, 4, 5, 6)
    override val numTicksDivider: Double = 6.0

    override fun computePosition(amplitude: Point, leg: Int, case: Int) {
        when (case) {
            1 -> {
                current[leg].apply {
                    x = HOME[leg].x - amplitude.x * cos(PI * tick / numTicks.toDouble())
                    y = HOME[leg].y - amplitude.y * cos(PI * tick / numTicks.toDouble())
                    z = HOME[leg].z + amplitude.z.absoluteValue * sin(PI * tick / numTicks.toDouble())

                }
                if (tick >= numTicks - 1) this.case[leg] = 6
            }
            2 -> {
                current[leg].apply {
                    x = current[leg].x - amplitude.x / numTicks / 2.5
                    y = current[leg].y - amplitude.y / numTicks / 2.5
                    z = HOME[leg].z

                }
                if (tick >= numTicks - 1) this.case[leg] = 1
            }
            3 -> {
                current[leg].apply {
                    x = current[leg].x - amplitude.x / numTicks / 2.5
                    y = current[leg].y - amplitude.y / numTicks / 2.5
                    z = HOME[leg].z

                }
                if (tick >= numTicks - 1) this.case[leg] = 2
            }
            4 -> {
                current[leg].apply {
                    x = current[leg].x - amplitude.x / numTicks / 2.5
                    y = current[leg].y - amplitude.y / numTicks / 2.5
                    z = HOME[leg].z

                }
                if (tick >= numTicks - 1) this.case[leg] = 3
            }
            5 -> {
                current[leg].apply {
                    x = current[leg].x - amplitude.x / numTicks / 2.5
                    y = current[leg].y - amplitude.y / numTicks / 2.5
                    z = HOME[leg].z

                }
                if (tick >= numTicks - 1) this.case[leg] = 4
            }
            6 -> {
                current[leg].apply {
                    x = current[leg].x - amplitude.x / numTicks / 2.5
                    y = current[leg].y - amplitude.y / numTicks / 2.5
                    z = HOME[leg].z

                }
                if (tick >= numTicks - 1) this.case[leg] = 5
            }
        }
    }
}
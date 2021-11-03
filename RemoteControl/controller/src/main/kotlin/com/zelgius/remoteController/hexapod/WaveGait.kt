package com.zelgius.remoteController.hexapod

import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

class WaveGait : Gait() {
    override val case: IntArray = intArrayOf(1, 2, 3, 4, 5, 6)
    override val numTicksDivider: Double = 6.0

    override fun computePosition(numTicks: Double, amplitude: Point, leg: Int) {
        when (case[leg]) {
            1 -> {
                current[leg].apply {
                    x = HOME[leg].x - amplitude.x * cos(PI * tick / numTicks)
                    y = HOME[leg].y - amplitude.y * cos(PI * tick / numTicks)
                    z = HOME[leg].z + amplitude.z.absoluteValue * sin(PI * tick / numTicks)

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
package com.zelgius.remoteController.hexapod

import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

class TripodGait : Gait() {
    override val case: IntArray = intArrayOf(1, 2, 1, 2, 1, 2)
    override val numTicksDivider: Double = 2.0

    override fun computePosition(amplitude: Point, leg: Int, case: Int) {
        when(case) {
            1 -> {
                current[leg].apply {
                    x = HOME[leg].x - amplitude.x * cos(PI * tick/numTicks.toDouble())
                    y = HOME[leg].y - amplitude.y * cos(PI * tick/numTicks.toDouble())
                    z = HOME[leg].z + amplitude.z.absoluteValue * sin(PI * tick/numTicks.toDouble())

                }
                if(tick >= numTicks - 1) this.case[leg] = 2
            }
            2 -> {
                current[leg].apply {
                    x = HOME[leg].x + amplitude.x * cos(PI * tick/numTicks.toDouble())
                    y = HOME[leg].y + amplitude.y * cos(PI * tick/numTicks.toDouble())
                    z = HOME[leg].z

                }
                if(tick >= numTicks - 1) this.case[leg] = 1
            }
        }
    }
}
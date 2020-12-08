package com.zelgius.remoteController.arm.arm

import java.nio.ByteBuffer
import kotlin.math.absoluteValue


class Arm private constructor() {
    // Segment order
    // ----------------
    //
    // O -- O -- O -- <
    // 1    2    3    4
    //

    var enabled = false

    var currentIndex = 0
    val currentServo
        get() = driver.servos[currentIndex]

    fun next() {
        if (enabled) {
            currentIndex++
            currentIndex %= 4
        }
    }

    fun previous() {
        if (enabled) {
            currentIndex--
            if (currentIndex < 0) currentIndex = 3
        }
    }

    fun initPosition() {
        driver.servos[0].angle = 45
        driver.servos[1].angle = 160
        driver.servos[2].angle = 160
        driver.servos[3].angle = 0
    }

    enum class Direction { BACKWARD, FORWARD }
    inner class Movement(private val direction: Direction, private val servo: Servo, val reached: () -> Unit = {}) :
        Thread() {
        var stop = false
        override fun run() {
            while (
                !stop
                && ((servo.currentAngle > 0 && direction == Direction.FORWARD)
                        || (servo.currentAngle < 180 && direction == Direction.BACKWARD))
            ) {
                servo.angle = servo.currentAngle + when (direction) {
                    Direction.BACKWARD -> 1
                    Direction.FORWARD -> -1
                }

                sleep(10)
            }

            if (((servo.currentAngle <= 0 && direction == Direction.FORWARD)
                        || (servo.currentAngle >= 180 && direction == Direction.BACKWARD))
            ) {
                reached()
            }
        }
    }

    var currentMovement: Movement? = null

    fun startMoving(direction: Direction, reached: () -> Unit) {
        if (enabled) {

            currentMovement?.stop = true
            currentMovement = Movement(direction, currentServo, reached).apply {
                start()
            }
        }
    }

    fun stopMoving() {
        currentMovement?.stop = true
        currentMovement = null
    }

    lateinit var driver: ServoDriver
    fun test() {
        driver.servos[4].angle = 180
        Thread.sleep(500)
        driver.servos[4].angle = 0
        Thread.sleep(500)
        driver.servos[4].angle = 180
        Thread.sleep(500)
        driver.servos[4].angle = 0
        /*var angle1 = 90
        var angle2 = 90
        var angle3 = 90
        driver.servos[0].angle = angle1
        driver.servos[1].angle = angle2
        driver.servos[2].angle = angle3

        var inc1 = 1
        var inc2 = 0
        var inc3 = 0

        for (i in (0..1000)) {
            angle1.let {
                if (it >= driver.servos[0].actuationRange || it <= 0) {
                    inc1 = -inc1
                }

                if (it == 90) {
                    if (inc2 == 0) inc2 = -1
                    if (inc3 == 0) inc3 = 1
                    driver.servos[3].angle = 90
                    Thread.sleep(200)
                    driver.servos[3].angle = 0
                }

                angle1 = it + inc1
                driver.servos[0].angle = angle1
            }

            angle2.let {
                if (it + inc2 == driver.servos[1].actuationRange || it + inc2 == 0) {
                    inc2 = -inc2

                    //if (inc3 == 0) inc3 = 1
                }

                angle2 = it + inc2
                driver.servos[1].angle = angle2
            }

            angle3.let {
                if (it + inc3 == driver.servos[2].actuationRange || it + inc3 == 0) {
                    inc3 = -inc3
                }

                angle3 = it + inc3
                driver.servos[2].angle = angle3
            }

            Thread.sleep(2)
        }*/
    }

    companion object {
        suspend fun newInstance(): Arm =
            Arm().apply {
                driver = ServoDriver.newInstance()
                initPosition()
            }

    }
}

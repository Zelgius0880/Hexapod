package com.zelgius.remoteController.arm

import com.zelgius.drivers.servo.Servo
import com.zelgius.drivers.servo.ServoDriver

class Arm private constructor() {
    // Segment order
    // ----------------
    //
    // O -- O -- O -- <
    // 9    10   11   7
    //

    var enabled = false

    private val servoIndex = arrayOf(9, 10, 11, 7)

    var currentIndex = 0
    private val currentServo
        get() = driver.servos[servoIndex[currentIndex]]

    operator fun get(index: Int) = driver.servos[servoIndex[index]]

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
        currentIndex = 0
        driver.servos[9].angle = 65
        driver.servos[10].angle = 160
        driver.servos[11].angle = 160
        driver.servos[7].angle = 60
    }

    enum class Direction { BACKWARD, FORWARD }
    inner class Movement(private val direction: Direction, private val servo: Servo, val reached: () -> Unit = {}) :
        Thread() {
        var stop = false
        override fun run() {
            while (
                !stop
                && ((currentIndex == 0 && direction == Direction.FORWARD && servo.currentAngle > 65)
                        || (currentIndex != 0 && servo.currentAngle > 0 && direction == Direction.FORWARD)
                        || (servo.currentAngle < 180 && direction == Direction.BACKWARD))
            ) {
                servo.angle = servo.currentAngle + when (direction) {
                    Direction.BACKWARD -> 1
                    Direction.FORWARD -> -1
                }

                sleep(10)
            }

            if (
                (currentIndex == 0 && direction == Direction.FORWARD && servo.currentAngle <= 65)
                || (servo.currentAngle <= 0 && direction == Direction.FORWARD)
                || (servo.currentAngle >= 180 && direction == Direction.BACKWARD)
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
        driver.servos[11].angle = 180
        Thread.sleep(500)
        driver.servos[11].angle = 0
        Thread.sleep(500)
        driver.servos[11].angle = 180
        Thread.sleep(500)
        driver.servos[11].angle = 0
    }

    companion object {
        fun newInstance(driver: ServoDriver): Arm =
            Arm().apply {
                this.driver = driver
                initPosition()
            }

    }
}

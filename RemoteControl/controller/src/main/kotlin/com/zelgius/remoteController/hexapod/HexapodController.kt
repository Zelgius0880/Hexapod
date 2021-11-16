package com.zelgius.remoteController.hexapod

import com.pi4j.exception.Pi4JException
import com.zelgius.drivers.servo.Servo
import com.zelgius.drivers.servo.ServoConfig
import com.zelgius.drivers.servo.ServoDriver
import com.zelgius.remoteController.controls.CONTROLS
import com.zelgius.remoteController.controls.Control
import com.zelgius.remoteController.hexapod.HexapodController.Leg.Companion.FEMUR_LENGTH
import com.zelgius.remoteController.hexapod.HexapodController.Leg.Companion.TIBIA_LENGTH
import com.zelgius.remoteController.ui.DebugScreen
import kotlin.concurrent.thread
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class HexapodController(
    legDriver1: ServoDriver,
    legDriver2: ServoDriver,
    private val screen: DebugScreen
) {
    companion object {

        private val Point.coxa
            get() = x
        private val Point.femur
            get() = y
        private val Point.tibia
            get() = z

        val CAL = arrayOf(
            Point(2.0, 4.0, 0.0),
            Point(-1.0, -2.0, -3.0),
            Point(-1.0, 0.0, -3.0),
            Point(-3.0, -1.0, -2.0),
            Point(-2.0, 0.0, -3.0),
            Point(-3.0, 0.0, -1.0)
        )
    }

    private var rotationAxes = Point()

    private val legs: Array<Leg> = arrayOf(
        Leg(
            legDriver1.servos.get(0, ServoConfig.MG90D),
            legDriver1.servos.get(1, ServoConfig.MG90D),
            legDriver1.servos.get(2, ServoConfig.MG90D)
        ),
        Leg(
            legDriver1.servos.get(3, ServoConfig.MG90Sv2),
            legDriver1.servos.get(4, ServoConfig.MG90D),
            legDriver1.servos.get(5, ServoConfig.MG90D)
        ),
        Leg(
            legDriver1.servos.get(13, ServoConfig.MG90D),
            legDriver1.servos.get(14, ServoConfig.MG90D),
            legDriver1.servos.get(15, ServoConfig.MG90D)
        ),
        Leg(
            legDriver2.servos.get(13, ServoConfig.MG90D),
            legDriver2.servos.get(14, ServoConfig.MG90D),
            legDriver2.servos.get(15, ServoConfig.MG90D)
        ),
        Leg(
            legDriver2.servos.get(10, ServoConfig.MG90Sv2),
            legDriver2.servos.get(11, ServoConfig.MG90D),
            legDriver2.servos.get(12, ServoConfig.MG90Sv2)
        ),
        Leg(
            legDriver2.servos.get(7, ServoConfig.MG90Sv2),
            legDriver2.servos.get(8, ServoConfig.MG90D),
            legDriver2.servos.get(9, ServoConfig.MG90D)
        ),
    )

    private fun test(servo: Servo, reverse: Boolean = false) {
        servo.angle = if (reverse) servo.actuationRange else 0
        Thread.sleep(500)
        servo.angle = if (reverse) 0 else servo.actuationRange
        Thread.sleep(500)
        servo.angle = servo.actuationRange / 2
        Thread.sleep(500)
    }

    private fun test(leg: Leg, reverse: Boolean = false) {
        test(leg.coxa, reverse)
        Thread.sleep(500)
        test(leg.femur, reverse)
        Thread.sleep(500)
        test(leg.tibia, reverse)

    }

    var stop = false
    var testMode = false
    private var testLegIndex = 0

    private var pointL: Point? = null
    private var pointR: Point? = null

    fun run() {
        thread {
            while (!stop) {
                if (System.currentTimeMillis() - lastStamp > Gait.FRAME_TIME_MS) {
                    val l = pointL
                    val r = pointR

                    if (l != null && r != null)
                        gait?.computeMovement(r, l)?.forEachIndexed { index, p ->
                            try {
                                moveLeg(index, p)
                            } catch (e: Pi4JException) {
                                println(e.message)
                            }
                        }

                    lastStamp = System.currentTimeMillis()
                }
            }


            Thread.sleep(10)
        }
    }

    var gait: Gait? = null
        set(value) {
            if (!testMode || value !is TetrapodGait) {
                field = value
                screen.gait = value
            } else {
                test(legs[testLegIndex])
            }
        }

    private var lastStamp = 0L

    fun reset() {
        gait = null
        legs.forEachIndexed { index, leg ->
            leg.coxa.angle = 90 + CAL[index].coxa.toInt()
            leg.tibia.angle = 90 + CAL[index].tibia.toInt()
            leg.femur.angle = 90 + CAL[index].femur.toInt()
        }

        //test(legs[0])
    }

    fun speedUp() {
        if (testMode) {
            testLegIndex++
            testLegIndex = testLegIndex.coerceAtMost(5)
        }
        gait?.isFast = true
    }

    fun speedDown() {
        if (testMode) {
            testLegIndex--
            testLegIndex = testLegIndex.coerceAtLeast(0)
        }
        gait?.isFast = false
    }

    fun move(control: Control) {

        val (yL, xL, zL) = arrayOf(
            control.data.short,
            control.data.short,
            control.data.short
        ).map { it.toDouble() }

        val (xR, yR, zR) = arrayOf(
            control.data.short,
            control.data.short,
            control.data.short
        ).map { it.toDouble() }

        val l = Point(-xL, yL, zL)

        // on r stick x and y are inverted ¯\_(ツ)_/¯
        val r = Point(-yR, xR, zR)

        if (!l.isDeadBand) pointL = l
        if (!r.isDeadBand) pointR = r

    }

    fun moveAxes(control: Control) {
        when (control.type) {
            CONTROLS.CROSS_LEFT -> rotationAxes.x = (rotationAxes.x - 1).coerceAtLeast(-20.0)
            CONTROLS.CROSS_RIGHT -> rotationAxes.x = (rotationAxes.x + 1).coerceAtMost(20.0)
            CONTROLS.CROSS_UP -> rotationAxes.y = (rotationAxes.y - 1).coerceAtLeast(-20.0)
            CONTROLS.CROSS_DOWN -> rotationAxes.y = (rotationAxes.y + 1).coerceAtMost(20.0)
            else -> return
        }

        gait?.moveAxes(rotationAxes)
    }

    @Synchronized
    private fun moveLeg(legIndex: Int, legPoint: Point) {
        val (x, y, z) = legPoint

        val l0 = sqrt(x.pow(2) + y.pow(2)) - Leg.COXA_LENGTH
        val l3 = sqrt(l0.pow(2) + z.pow(2))

        if (l3 < TIBIA_LENGTH + FEMUR_LENGTH && l3 > TIBIA_LENGTH - FEMUR_LENGTH) {

            //compute tibia angle
            val phiTibia =
                acos((FEMUR_LENGTH.pow(2) + TIBIA_LENGTH.pow(2) - l3.pow(2)) / (2.0 * FEMUR_LENGTH * TIBIA_LENGTH))
            val thetaTibia = (phiTibia.toDegree() - 23.0 + CAL[legIndex].tibia).coerceIn(0.0, 180.0)

            //compute femur angle
            val gammaFemur = atan2(z, l0)
            val phiFemur = acos((FEMUR_LENGTH.pow(2) + l3.pow(2) - TIBIA_LENGTH.pow(2)) / (2 * FEMUR_LENGTH * l3))
            val thetaFemur =
                ((phiFemur + gammaFemur).toDegree() + 14.0 + 90.0 + CAL[legIndex].femur).coerceIn(0.0, 180.0)

            //compute coxa angle
            var thetaCoxa = atan2(x, y).toDegree() + CAL[legIndex].coxa

            val leg = legs[legIndex]

            val point = when (legIndex) {
                0 -> {
                    thetaCoxa = (thetaCoxa + 45.0).coerceIn(0.0, 180.0)     //compensate for leg mounting
                    Point.forLeg(
                        coxa = thetaCoxa,
                        femur = thetaFemur,
                        tibia = thetaTibia
                    )
                }
                1 -> {
                    thetaCoxa = (thetaCoxa + 90.0).coerceIn(0.0, 180.0)     //compensate for leg mounting
                    Point.forLeg(
                        coxa = thetaCoxa,
                        femur = thetaFemur,
                        tibia = thetaTibia
                    )
                }
                2 -> {
                    thetaCoxa = (thetaCoxa + 135.0).coerceIn(0.0, 180.0)    //compensate for leg mounting
                    Point.forLeg(
                        coxa = thetaCoxa,
                        femur = thetaFemur,
                        tibia = thetaTibia
                    )
                }
                3 -> {
                    thetaCoxa = (if (thetaCoxa < 0)                                //compensate for leg mounting
                        thetaCoxa + 225.0             // (need to use different
                    else                                              //  positive and negative offsets
                        thetaCoxa - 135.0).coerceIn(0.0, 180.0)            //  due to atan2 results above!)
                    Point.forLeg(
                        coxa = thetaCoxa,
                        femur = 180 - thetaFemur,
                        tibia = 180 - thetaTibia
                    )
                }
                4 -> {
                    thetaCoxa = (if (thetaCoxa < 0)                                //compensate for leg mounting
                        thetaCoxa + 270.0                // (need to use different
                    else                                              //  positive and negative offsets
                        thetaCoxa - 90.0).coerceIn(0.0, 180.0)                 //  due to atan2 results above!)
                    Point.forLeg(
                        coxa = thetaCoxa,
                        femur = 180 - thetaFemur,
                        tibia = 180 - thetaTibia
                    )
                }
                5 -> {
                    thetaCoxa = (if (thetaCoxa < 0)                              //compensate for leg mounting
                        thetaCoxa + 315.0              // (need to use different
                    else                                            //  positive and negative offsets
                        thetaCoxa - 45.0).coerceIn(0.0, 180.0)                  //  due to atan2 results above!)
                    Point.forLeg(
                        coxa = thetaCoxa,
                        femur = 180 - thetaFemur,
                        tibia = 180 - thetaTibia
                    )
                }
                else -> error("Leg index not known. This should never happen")
            }

            leg.coxa.angle = point.coxa.toInt()
            leg.femur.angle = point.femur.toInt()
            leg.tibia.angle = point.tibia.toInt()


            //<editor-fold desc="Debug">
            val invertIfNeeded: (angle: Double) -> Double = { angle ->
                if (legIndex < 0) angle else (180 - angle)
            }
            screen.legs[legIndex] = Point(
                x = invertIfNeeded(point.coxa),
                y = invertIfNeeded(point.femur),
                z = invertIfNeeded(point.tibia),
            )
            //</editor-fold>
        }
    }

    private fun Double.toDegree() = this * 180 / Math.PI

    class Leg(
        val coxa: Servo,
        val femur: Servo,
        val tibia: Servo,
    ) {
        companion object {
            const val COXA_LENGTH = 51.0
            const val FEMUR_LENGTH = 65.0
            const val TIBIA_LENGTH = 121.0
        }
    }
}


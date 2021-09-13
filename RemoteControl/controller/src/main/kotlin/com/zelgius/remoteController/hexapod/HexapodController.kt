package com.zelgius.remoteController.hexapod

import com.zelgius.remoteController.controls.Control
import com.zelgius.remoteController.hexapod.HexapodController.Leg.Companion.FEMUR_LENGTH
import com.zelgius.remoteController.hexapod.HexapodController.Leg.Companion.TIBIA_LENGTH
import com.zelgius.remoteController.pca9685.Servo
import com.zelgius.remoteController.pca9685.ServoDriver
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

class HexapodController(
    legDriver1: ServoDriver,
    legDriver2: ServoDriver
) {
    companion object {
        val COXA_CAL = arrayOf(2, -1, -1, -3, -2, -3)                       //servo calibration constants
        val FEMUR_CAL = arrayOf(4, -2, 0, -1, 0, 0)
        val TIBIA_CAL = arrayOf(0, -3, -3, -2, -3, -1)

        private const val RAD_TO_DEG = 57.295779513082320876798154814105
    }

    fun test() {
        println("testing ...")
        val servo =legs[0].tibia
        servo.angle = 45
        Thread.sleep(500)
        servo.angle = 0
        Thread.sleep(500)
        servo.angle = 45
        Thread.sleep(500)
        servo.angle = 0
        println("end testing ...")
    }

    private val legs: Array<Leg> = arrayOf(
        Leg(legDriver1.servos[0], legDriver1.servos[1], legDriver1.servos[2]),
        Leg(legDriver1.servos[3], legDriver1.servos[4], legDriver1.servos[5]),
        Leg(legDriver1.servos[13], legDriver1.servos[14], legDriver1.servos[15]),
        
        Leg(legDriver2.servos[0], legDriver2.servos[1], legDriver2.servos[2]),
        Leg(legDriver2.servos[3], legDriver2.servos[4], legDriver2.servos[5]),
        Leg(legDriver2.servos[13], legDriver2.servos[14], legDriver2.servos[15]),
    )

    var gait: Gait? = null

    private var lastStamp = 0L

    fun reset() {
        gait?.reset()
        test()
    }

    fun speedUp(){
        gait?.duration = Gait.SPEED_HIGH
    }

    fun speedDown() {
        gait?.duration = Gait.SPEED_LOW
    }

    fun move(control: Control) {
        if (System.currentTimeMillis() - lastStamp > Gait.FRAME_TIME_MS) {

            val (xL, yL, zL) = arrayOf(
                control.data.short,
                control.data.short,
                control.data.short
            ).map { it.toDouble() }

            val (xR, yR, zR) = arrayOf(
                control.data.short,
                control.data.short,
                control.data.short
            ).map { it.toDouble() }

            moveLegs(Point(xL, -yL, zL))
            gait?.move(Point(xL, -yL, zL), Point(xR, -yR, zR))
        }
    }

    private fun moveLegs(point: Point) {
        val l0 = sqrt(point.x.pow(2) + point.y.pow(2)) - Leg.COXA_LENGTH
        val l3 = sqrt(l0.pow(2) + point.z.pow(2))

        if (l3 < TIBIA_LENGTH + FEMUR_LENGTH && l3 > TIBIA_LENGTH - FEMUR_LENGTH) {
            legs.forEachIndexed { index, leg ->


                //compute tibia angle
                val phiTibia =
                    acos((FEMUR_LENGTH.pow(2) + TIBIA_LENGTH.pow(2) - l3.pow(2)) / (2.0 * FEMUR_LENGTH * TIBIA_LENGTH))
                val thetaTibia = (phiTibia * RAD_TO_DEG - 23.0 + TIBIA_CAL[index]).coerceIn(0.0, 180.0)

                //compute femur angle
                val gammaFemur = atan2(point.z, l0)
                val phiFemur = acos((FEMUR_LENGTH.pow(2) + l3.pow(2) - TIBIA_LENGTH.pow(2)) / (2 * FEMUR_LENGTH * l3))
                val thetaFemur =
                    ((phiFemur + gammaFemur) * RAD_TO_DEG + 14.0 + 90.0 + FEMUR_CAL[index]).coerceIn(0.0, 180.0)

                //compute coxa angle
                var thetaCoxa = atan2(point.x, point.y) * RAD_TO_DEG + COXA_CAL[index]

                when (index) {
                    0 -> {
                        thetaCoxa =
                            (thetaCoxa + 45.0).coerceIn(0.0, 180.0)                 //compensate for leg mounting
                        leg.coxa.angle = thetaCoxa.toInt()
                        leg.femur.angle = thetaFemur.toInt()
                        leg.tibia.angle = thetaTibia.toInt()
                    }
                    1 -> {
                        thetaCoxa =
                            (thetaCoxa + 90.0).coerceIn(0.0, 180.0)                     //compensate for leg mounting
                        leg.coxa.angle = thetaCoxa.toInt()
                        leg.femur.angle = thetaFemur.toInt()
                        leg.tibia.angle = thetaTibia.toInt()
                    }
                    2 -> {
                        thetaCoxa = (thetaCoxa + 135.0).coerceIn(0.0, 180.0)               //compensate for leg mounting
                        leg.coxa.angle = thetaCoxa.toInt()
                        leg.femur.angle = thetaFemur.toInt()
                        leg.tibia.angle = thetaTibia.toInt()
                    }
                    3 -> {
                        thetaCoxa = (if (thetaCoxa < 0)                                //compensate for leg mounting
                            thetaCoxa + 225.0             // (need to use different
                        else                                              //  positive and negative offsets
                            thetaCoxa - 135.0).coerceIn(0.0, 180.0)            //  due to atan2 results above!)
                        leg.coxa.angle = thetaCoxa.toInt()
                        leg.femur.angle = (180 - thetaFemur).toInt()
                        leg.tibia.angle = (180 - thetaTibia).toInt()
                    }
                    4 -> {
                        thetaCoxa = (if (thetaCoxa < 0)                                //compensate for leg mounting
                            thetaCoxa + 270.0                // (need to use different
                        else                                              //  positive and negative offsets
                            thetaCoxa - 90.0).coerceIn(0.0, 180.0)                 //  due to atan2 results above!)
                        leg.coxa.angle = thetaCoxa.toInt()
                        leg.femur.angle = (180 - thetaFemur).toInt()
                        leg.tibia.angle = (180 - thetaTibia).toInt()
                    }
                    5 -> {
                        thetaCoxa = (if (thetaCoxa < 0)                              //compensate for leg mounting
                            thetaCoxa + 315.0              // (need to use different
                        else                                            //  positive and negative offsets
                            thetaCoxa - 45.0).coerceIn(0.0, 180.0)                  //  due to atan2 results above!)
                        leg.coxa.angle = thetaCoxa.toInt()
                        leg.femur.angle = (180 - thetaFemur).toInt()
                        leg.tibia.angle = (180 - thetaTibia).toInt()
                    }

                }
            }
        }
    }

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


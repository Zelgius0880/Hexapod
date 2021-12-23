package com.zelgius.api.model

import com.zelgius.controller.StatusOuterClass

data class Status(
    val level: BatteryLevel,
    val walk: Walk,
    val pitch: Double,
    val roll: Double,
    val offset: Double,
    val isFast: Boolean,
    val isFlashLightOn: Boolean,
    val armSegments: List<ArmSegment>
) {
    enum class Walk {
        RIPPLE, TETRAPOD, WAVE, TRIPOD, NONE,
    }

    enum class BatteryLevel {
        LOW, MEDIUM, HIGH
    }

    data class ArmSegment(val angle: Double, val isSelected: Boolean = false)
}

fun StatusOuterClass.Status.toDataClass() =
    Status(
        when (level) {
            StatusOuterClass.Status.BatteryLevel.LOW -> Status.BatteryLevel.LOW
            StatusOuterClass.Status.BatteryLevel.MEDIUM -> Status.BatteryLevel.MEDIUM
            StatusOuterClass.Status.BatteryLevel.HIGH -> Status.BatteryLevel.HIGH
            else -> error("Unknown level $level")
        },
        when (walkMode) {
            StatusOuterClass.Status.Walk.RIPPLE -> Status.Walk.RIPPLE
            StatusOuterClass.Status.Walk.TETRAPOD -> Status.Walk.TETRAPOD
            StatusOuterClass.Status.Walk.WAVE -> Status.Walk.WAVE
            StatusOuterClass.Status.Walk.TRIPOD -> Status.Walk.TRIPOD
            StatusOuterClass.Status.Walk.NONE -> Status.Walk.NONE
            else -> error("Unknown walk $walkMode")
        },
        pitch,
        roll,
        offset,
        isFast,
        isFlashLightOn,
        armSegmentsList.map { Status.ArmSegment(it.angle, it.isSelected) })

fun Status.toProtobuf() = StatusOuterClass.Status.newBuilder()
    .setPitch(pitch)
    .setLevel(
        when (level) {
            Status.BatteryLevel.LOW -> StatusOuterClass.Status.BatteryLevel.LOW
            Status.BatteryLevel.MEDIUM -> StatusOuterClass.Status.BatteryLevel.MEDIUM
            Status.BatteryLevel.HIGH -> StatusOuterClass.Status.BatteryLevel.HIGH
        }
    )
    .setRoll(roll)
    .setIsFlashLightOn(isFlashLightOn)
    .setWalkMode(
        when (walk) {
            Status.Walk.RIPPLE -> StatusOuterClass.Status.Walk.RIPPLE
            Status.Walk.TETRAPOD -> StatusOuterClass.Status.Walk.TETRAPOD
            Status.Walk.WAVE -> StatusOuterClass.Status.Walk.WAVE
            Status.Walk.TRIPOD -> StatusOuterClass.Status.Walk.TRIPOD
            Status.Walk.NONE -> StatusOuterClass.Status.Walk.NONE
        }
    )
    .setOffset(offset)
    .setIsFast(isFast)
    .addAllArmSegments(armSegments.map {
        StatusOuterClass.ArmSegment.newBuilder()
            .setAngle(it.angle)
            .setIsSelected(it.isSelected)
            .build()
    })
    .build()
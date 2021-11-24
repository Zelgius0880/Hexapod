package com.zelgius.remoteController.ui

import com.github.ajalt.mordant.rendering.BorderStyle.Companion.SQUARE_DOUBLE_SECTION_SEPARATOR
import com.github.ajalt.mordant.rendering.TextAlign
import com.github.ajalt.mordant.rendering.TextColors
import com.github.ajalt.mordant.table.Borders
import com.github.ajalt.mordant.table.ColumnWidth
import com.github.ajalt.mordant.table.table
import com.github.ajalt.mordant.terminal.Terminal
import com.zelgius.remoteController.hexapod.Gait
import com.zelgius.remoteController.hexapod.Point

object DebugScreen {

    // x: coax; y: femur, z: tibia
    val legs = Array(6) { Point() }

    var gait: Gait? = null

    var voltage: Double = 0.0
    var temp: Any = Unit

    private var alreadyRendered = false

    private val terminal = Terminal().apply {
        cursor.hide()
        cursor.move {
            //clearScreen()
        }
    }

    private fun buildLegsTable(range: IntRange) = table {
        borderStyle = SQUARE_DOUBLE_SECTION_SEPARATOR
        align = TextAlign.RIGHT
        outerBorder = true

        (1..6).forEach {
            column(it) {
                width = ColumnWidth.Fixed(10)
            }
        }


        header {

            row {
                cell("") {
                    borders = Borders.BOTTOM_RIGHT
                }
                cellsFrom(range.map { "Leg ${it + 1}" }) {
                    borders = Borders.TOP_RIGHT_BOTTOM
                }
            }
        }
        body {
            borders = Borders.TOM_BOTTOM
            row("COAX", *legs.sliceArray(range).map { String.format("%.2f", it.x) }.toTypedArray())
            row("FEMUR", *legs.sliceArray(range).map { String.format("%.2f", it.y) }.toTypedArray())
            row("TIBIA", *legs.sliceArray(range).map { String.format("%.2f", it.z) }.toTypedArray()) {
                borders = Borders.BOTTOM
            }
        }
    }

    private fun buildInfoTable() = table {
        borderStyle = SQUARE_DOUBLE_SECTION_SEPARATOR
        align = TextAlign.RIGHT
        outerBorder = true

        column(1) {
            style = TextColors.black on TextColors.brightBlue
            width = ColumnWidth.Fixed(30)
        }

        body {
            borders = Borders.TOM_BOTTOM
            row {
                cell("GAIT") {}
                cell(gait?.let { it::class.simpleName } ?: "None")
            }
            row {
                cell("VOLTAGE") {}
                cell(voltage)
            }
            row {
                cell("TEMP") {}
                cell(temp)
            }
        }
    }

    fun render() {
        alreadyRendered = true
        val legs = buildLegsTable(0..5)
        val info = buildInfoTable()

        if (alreadyRendered) {
            val lines = legs.render(terminal).height + info.render(terminal).height
            terminal.cursor.move {
                up(lines)
                startOfLine()
                clearLineAfterCursor()
            }
        }
        terminal.println(legs)
        terminal.println(info)
    }
}
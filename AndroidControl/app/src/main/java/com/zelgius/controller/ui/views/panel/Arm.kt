package com.zelgius.controller.ui.views.panel

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zelgius.api.model.Status
import com.zelgius.controller.ui.theme.AppTheme


@Composable
fun Arm(modifier: Modifier = Modifier, segments: List<Status.ArmSegment>) {
    val segmentColorSelected = MaterialTheme.colorScheme.secondaryContainer
    val segmentColorUnselected = MaterialTheme.colorScheme.secondary
    val clawColorSelected = MaterialTheme.colorScheme.tertiaryContainer
    val clawColorUnselected = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = modifier) {

        segments.slice(0..2).forEachIndexed { index, arm ->
            drawArmSegment(
                index,
                color = if (arm.isSelected) segmentColorSelected else segmentColorUnselected,
            )
        }

        with(segments.last()) {
            drawArmClaw(
                if (isSelected) clawColorSelected else clawColorUnselected
            )
        }
    }

}

private val DrawScope.segmentWidth: Float get() = size.width.coerceAtLeast(size.height) / 16
private val DrawScope.segmentHeight: Float get() = size.width.coerceAtLeast(size.height) / 4
private val DrawScope.clawHeight: Float get() =  size.width.coerceAtLeast(size.height) / 12
private val DrawScope.clawWidth: Float get() = size.width.coerceAtLeast(size.height) / 24

private val DrawScope.padding: Float get() = segmentWidth

private fun DrawScope.drawArmSegment(
    index: Int,
    color: Color
) {

    drawLine(
        color = color,
        start = Offset(size.width / 2, size.height - (index) * segmentHeight - padding),
        end = Offset(size.width / 2, size.height - (index + 1) * segmentHeight + padding/2),
        strokeWidth = segmentWidth,
        cap = StrokeCap.Round,
        pathEffect = PathEffect.cornerPathEffect(4.dp.toPx())
    )
}

private fun DrawScope.drawArmClaw(
    color: Color
) {
    drawLine(
        color = color,
        start = Offset(size.width / 2 - clawWidth /2, size.height - 3 * segmentHeight - padding / 2),
        end = Offset(size.width / 2 - clawHeight - clawWidth /2, size.height - 3 * segmentHeight - segmentHeight / 2),
        strokeWidth = clawWidth,
        cap = StrokeCap.Round,
        pathEffect = PathEffect.cornerPathEffect(2.dp.toPx())
    )

    drawLine(
        color = color,
        start = Offset(size.width / 2 - clawHeight - clawWidth /2, size.height - 3 * segmentHeight - segmentHeight / 2),
        end = Offset(size.width / 2 - clawWidth /2 - padding / 2, 0f + padding / 2 ),
        strokeWidth = clawWidth,
        cap = StrokeCap.Round,
        pathEffect = PathEffect.cornerPathEffect(2.dp.toPx())
    )

    drawLine(
        color = color,
        start = Offset(size.width / 2 + clawWidth /2, size.height - 3 * segmentHeight - padding / 2),
        end = Offset(size.width / 2 + clawHeight + clawWidth /2, size.height - 3 * segmentHeight - segmentHeight / 2),
        strokeWidth = clawWidth,
        cap = StrokeCap.Round,
        pathEffect = PathEffect.cornerPathEffect(2.dp.toPx())
    )

    drawLine(
        color = color,
        start = Offset(size.width / 2 + clawHeight + clawWidth /2, size.height - 3 * segmentHeight - segmentHeight / 2),
        end = Offset(size.width / 2 + clawWidth /2 + padding / 2, 0f + padding / 2 ),
        strokeWidth = clawWidth,
        cap = StrokeCap.Round,
        pathEffect = PathEffect.cornerPathEffect(2.dp.toPx())
    )
}

@Preview(apiLevel = 27)
@Composable
fun PreviewArm() {
    AppTheme() {
        Arm(segments = previewSegments, modifier = Modifier.size(height = 150.dp, width = 50.dp))
    }
}

val previewSegments = listOf(
    Status.ArmSegment(-90.0, true),
    Status.ArmSegment(-90.0, false),
    Status.ArmSegment(-90.0, false),
    Status.ArmSegment(-90.0, true)
)
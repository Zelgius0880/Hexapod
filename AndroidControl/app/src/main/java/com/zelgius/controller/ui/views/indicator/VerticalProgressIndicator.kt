/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zelgius.controller.ui.views.indicator

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.zelgius.controller.ui.views.indicator.ProgressIndicatorDefaults.IndicatorBackgroundOpacity

/**
 * Determinate <a href="https://material.io/components/progress-indicators#linear-progress-indicators" class="external" target="_blank">Material Design linear progress indicator</a>.
 *
 * Progress indicators express an unspecified wait time or display the length of a process.
 *
 * ![Linear progress indicator image](https://developer.android.com/images/reference/androidx/compose/material/linear-progress-indicator.png)
 *
 * By default there is no animation between [progress] values. You can use
 * [ProgressIndicatorDefaults.ProgressAnimationSpec] as the default recommended
 * [AnimationSpec] when animating progress, such as in the following example:
 *
 *
 * @param progress The progress of this progress indicator, where 0.0 represents no progress and 1.0
 * represents full progress. Values outside of this range are coerced into the range.
 * @param color The color of the progress indicator.
 * @param backgroundColor The color of the background behind the indicator, visible when the
 * progress has not reached that area of the overall indicator yet.
 */
@Composable
fun VerticalLinearProgressIndicator(
    /*@FloatRange(from = 0.0, to = 1.0)*/
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = color.copy(alpha = IndicatorBackgroundOpacity)
) {
    Canvas(
        modifier
            .progressSemantics(progress)
            .size(LinearIndicatorWidth, LinearIndicatorHeight)
    ) {
        val strokeWidth = size.width
        drawLinearIndicatorBackground(backgroundColor, strokeWidth)
        drawLinearIndicator(0f, progress, color, strokeWidth)
    }
}

/**
 * Indeterminate <a href="https://material.io/components/progress-indicators#linear-progress-indicators" class="external" target="_blank">Material Design linear progress indicator</a>.
 *
 * Progress indicators express an unspecified wait time or display the length of a process.
 *
 * ![Linear progress indicator image](https://developer.android.com/images/reference/androidx/compose/material/linear-progress-indicator.png)
 *
 * @param color The color of the progress indicator.
 * @param backgroundColor The color of the background behind the indicator, visible when the
 * progress has not reached that area of the overall indicator yet.
 */
@Composable
fun VerticalLinearProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = color.copy(alpha = IndicatorBackgroundOpacity)
) {
    val infiniteTransition = rememberInfiniteTransition()
    // Fractional position of the 'head' and 'tail' of the two lines drawn. I.e if the head is 0.8
    // and the tail is 0.2, there is a line drawn from between 20% along to 80% along the total
    // width.
    val firstLineHead by infiniteTransition.animateFloat(
        0f,
        1f,
        infiniteRepeatable(
            animation = keyframes {
                durationMillis = LinearAnimationDuration
                0f at FirstLineHeadDelay with FirstLineHeadEasing
                1f at FirstLineHeadDuration + FirstLineHeadDelay
            }
        )
    )
    val firstLineTail by infiniteTransition.animateFloat(
        0f,
        1f,
        infiniteRepeatable(
            animation = keyframes {
                durationMillis = LinearAnimationDuration
                0f at FirstLineTailDelay with FirstLineTailEasing
                1f at FirstLineTailDuration + FirstLineTailDelay
            }
        )
    )
    val secondLineHead by infiniteTransition.animateFloat(
        0f,
        1f,
        infiniteRepeatable(
            animation = keyframes {
                durationMillis = LinearAnimationDuration
                0f at SecondLineHeadDelay with SecondLineHeadEasing
                1f at SecondLineHeadDuration + SecondLineHeadDelay
            }
        )
    )
    val secondLineTail by infiniteTransition.animateFloat(
        0f,
        1f,
        infiniteRepeatable(
            animation = keyframes {
                durationMillis = LinearAnimationDuration
                0f at SecondLineTailDelay with SecondLineTailEasing
                1f at SecondLineTailDuration + SecondLineTailDelay
            }
        )
    )
    Canvas(
        modifier
            .progressSemantics()
            .size(LinearIndicatorWidth, LinearIndicatorHeight)
    ) {
        val strokeWidth = size.height
        drawLinearIndicatorBackground(backgroundColor, strokeWidth)
        if (firstLineHead - firstLineTail > 0) {
            drawLinearIndicator(
                firstLineHead,
                firstLineTail,
                color,
                strokeWidth
            )
        }
        if ((secondLineHead - secondLineTail) > 0) {
            drawLinearIndicator(
                secondLineHead,
                secondLineTail,
                color,
                strokeWidth
            )
        }
    }
}

private fun DrawScope.drawLinearIndicator(
    startFraction: Float,
    endFraction: Float,
    color: Color,
    strokeWidth: Float
) {
    val width = size.width
    val height = size.height
    // Start drawing from the vertical center of the stroke
    val xOffset = width / 2

    val isLtr = layoutDirection == LayoutDirection.Ltr
    val barStart = (if (!isLtr) startFraction else 1f - endFraction) * height
    val barEnd = (if (!isLtr) endFraction else 1f - startFraction) * height

    // Progress line
    drawLine(color, Offset(xOffset, barStart), Offset(xOffset, barEnd), strokeWidth)
}

private fun DrawScope.drawLinearIndicatorBackground(
    color: Color,
    strokeWidth: Float
) = drawLinearIndicator(0f, 1f, color, strokeWidth)


object ProgressIndicatorDefaults {
    val StrokeWidth = 4.dp

    /**
     * The default opacity applied to the indicator color to create the background color in a
     * [VerticalLinearProgressIndicator].
     */
    const val IndicatorBackgroundOpacity = 0.24f

    /**
     * The default [AnimationSpec] that should be used when animating between progress in a
     * determinate progress indicator.
     */
    val ProgressAnimationSpec = SpringSpec(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessVeryLow,
        // The default threshold is 0.01, or 1% of the overall progress range, which is quite
        // large and noticeable.
        visibilityThreshold = 1 / 1000f
    )
}

// VerticalLinearProgressIndicator Material specs
// TODO: there are currently 3 fixed widths in Android, should this be flexible? Material says
// the width should be 240dp here.
private val LinearIndicatorWidth = ProgressIndicatorDefaults.StrokeWidth
private val LinearIndicatorHeight = 240.dp

// Indeterminate linear indicator transition specs
// Total duration for one cycle
private const val LinearAnimationDuration = 1800

// Duration of the head and tail animations for both lines
private const val FirstLineHeadDuration = 750
private const val FirstLineTailDuration = 850
private const val SecondLineHeadDuration = 567
private const val SecondLineTailDuration = 533

// Delay before the start of the head and tail animations for both lines
private const val FirstLineHeadDelay = 0
private const val FirstLineTailDelay = 333
private const val SecondLineHeadDelay = 1000
private const val SecondLineTailDelay = 1267

private val FirstLineHeadEasing = CubicBezierEasing(0.2f, 0f, 0.8f, 1f)
private val FirstLineTailEasing = CubicBezierEasing(0.4f, 0f, 1f, 1f)
private val SecondLineHeadEasing = CubicBezierEasing(0f, 0f, 0.65f, 1f)
private val SecondLineTailEasing = CubicBezierEasing(0.1f, 0f, 0.45f, 1f)


@Preview
@Composable
fun PreviewVerticalLinearIndicator() {
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        VerticalLinearProgressIndicator(progress = 0.1f)
    }
}
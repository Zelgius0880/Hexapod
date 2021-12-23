package com.zelgius.controller.ui.views.panel

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun HorizontalSlidePanel(
    modifier: Modifier = Modifier,
    remainingWidth: Dp,
    targetWidth: Dp,
    content: @Composable BoxScope.() -> Unit
) {
    if (remainingWidth >= targetWidth) {
        Box(modifier = modifier) {
            content()
        }
    } else {
        var isOpened by remember {
            mutableStateOf(false)
        }
        val fabSize = 40.dp
        val offset by animateFloatAsState(targetValue = if (isOpened) 0f else (targetWidth - fabSize / 2 - remainingWidth).value)
        val rotation by animateFloatAsState(targetValue = if (isOpened) 0f else 180f)

        Box(
            modifier = modifier
                .offset(offset.dp, 0.dp)
                .width(targetWidth)
        ) {
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                    .padding(start = fabSize / 2)
            ) {
                content()
            }
            FloatingActionButton(
                shape = CircleShape,
                onClick = { isOpened = !isOpened }, modifier = Modifier
                    .size(fabSize)
                    .align(
                        Alignment.CenterStart
                    )
            ) {
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "",
                    Modifier.rotate(rotation)
                )
            }
        }
    }
}

@Composable
private fun Dp.toPx() = with(LocalDensity.current) { toPx() }

@Preview(widthDp = 500, heightDp = 250)
@Composable
fun PreviewHorizontalSlidePanel() {
    Surface {
        Box(
            Modifier
                .background(Color.Black)
                .size(500.dp, 250.dp)
        ) {
            HorizontalSlidePanel(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterEnd),
                remainingWidth = 100.dp,
                targetWidth = 250.dp
            ) {
                Surface(
                    Modifier
                        .background(MaterialTheme.colorScheme.background)
                        .border(width = 2.dp, color = MaterialTheme.colorScheme.primary)
                        .align(Alignment.CenterEnd)
                        .fillMaxSize()
                ) {
                    Text(text = "Test Panel", modifier = Modifier.align(Alignment.CenterEnd))
                }
            }
        }
    }
}


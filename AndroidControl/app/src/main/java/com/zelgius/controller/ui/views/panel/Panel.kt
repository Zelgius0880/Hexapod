package com.zelgius.controller.ui.views.panel

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zelgius.api.model.Status
import com.zelgius.controller.R
import com.zelgius.controller.ui.theme.AppTheme
import com.zelgius.controller.ui.theme.errorColor
import com.zelgius.controller.ui.theme.okColor
import com.zelgius.controller.ui.theme.warningColor
import com.zelgius.controller.ui.views.indicator.VerticalLinearProgressIndicator
import kotlin.math.absoluteValue

const val PANEL_REQUIRED_WIDTH = 250

@Composable
fun Panel(modifier: Modifier = Modifier, viewModel: PanelViewModel = viewModel()) {
    val status by remember {
        viewModel.status
    }

    Box(modifier = modifier) {
        Row {
            Column(Modifier) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    //modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painterResource(R.drawable.ic_baseline_battery_std_24),
                        contentDescription = "",
                        tint = when (status.level) {
                            Status.BatteryLevel.HIGH -> okColor
                            Status.BatteryLevel.MEDIUM -> warningColor
                            Status.BatteryLevel.LOW -> errorColor
                        },
                        modifier = Modifier
                            .align(CenterVertically)
                            .padding(8.dp)
                    )

                    FlashLightButton(isFlashLightOn = status.isFlashLightOn, onClick = {
                        viewModel.toggleFlashLight()
                    }, modifier = Modifier.padding(8.dp))

                    SpeedButton(isFast = status.isFast, onClick = {
                        viewModel.setSpeed(status.isFast)
                    }, modifier = Modifier.padding(8.dp))
                }

                Row {
                    Column {
                        HexapodShape(
                            title = stringResource(id = R.string.pitch),
                            percent = status.pitch / 20,
                            orientation = Orientation.Horizontal,
                            modifier = Modifier.padding(8.dp)
                        )
                        HexapodShape(
                            title = stringResource(id = R.string.roll),
                            percent = status.roll / 20,
                            orientation = Orientation.Vertical,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    OffsetIndicator(
                        progress = -status.offset / 20.0,
                        modifier = Modifier
                            .height(190.dp)
                            .padding(vertical = 8.dp)
                            .align(Bottom)
                    )
                }
            }

            Arm(
                segments = status.armSegments, modifier = Modifier
                    .fillMaxHeight()
                    .width(150.dp)
                    .padding(bottom = 64.dp, top = 8.dp)
            )
        }
    }
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun SpeedButton(isFast: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {

    val colorOn = MaterialTheme.colorScheme.primary
    val colorOff = MaterialTheme.colorScheme.primaryContainer
    val buttonColor = remember {
        Animatable(if (isFast) colorOn else colorOff)
    }

    LaunchedEffect(key1 = isFast) {
        buttonColor.animateTo(if (isFast) colorOn else colorOff)
    }

    Button(
        onClick = {
            onClick()
        }, modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor.value)

    ) {
        AnimatedContent(targetState = isFast,
            transitionSpec = {
                if (targetState) {
                    slideInHorizontally { width -> -width } + fadeIn() with
                            slideOutHorizontally { width -> width } + fadeOut()
                } else {
                    slideInHorizontally { width -> width } + fadeIn() with
                            slideOutHorizontally { width -> -width } + fadeOut()
                }.using(SizeTransform(clip = false))
            }
        ) { isFast ->
            if (isFast)
                Icon(
                    painterResource(R.drawable.ic_baseline_fast_forward_24),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            else
                Icon(
                    painterResource(R.drawable.ic_twotone_fast_forward_24),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
        }
    }
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun FlashLightButton(
    isFlashLightOn: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {

    val colorOn = MaterialTheme.colorScheme.primary
    val colorOff = MaterialTheme.colorScheme.primaryContainer
    val buttonColor = remember {
        Animatable(if (isFlashLightOn) colorOn else colorOff)
    }

    LaunchedEffect(key1 = isFlashLightOn) {
        buttonColor.animateTo(if (isFlashLightOn) colorOn else colorOff)
    }

    Button(
        onClick = {
            //isFlashLightOn = !isFlashLightOn
            onClick()
        },
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor.value)
    ) {
        AnimatedContent(targetState = isFlashLightOn,
            transitionSpec = {
                if (targetState) {
                    slideInVertically { height -> -height } + fadeIn() with
                            slideOutVertically { height -> height } + fadeOut()
                } else {
                    slideInVertically { height -> height } + fadeIn() with
                            slideOutVertically { height -> -height } + fadeOut()
                }.using(SizeTransform(clip = false))
            }
        ) { isOn ->
            if (isOn)
                Icon(
                    painterResource(R.drawable.ic_twotone_flashlight_on_24),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            else
                Icon(
                    painterResource(R.drawable.ic_twotone_flashlight_off_24),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
        }
    }
}

@Composable
fun OffsetIndicator(progress: Double, modifier: Modifier = Modifier) {
    Row(modifier) {
        Column(Modifier.height(IntrinsicSize.Min)) {
            val progressPositive = if (progress <= 0f) 0f else progress.toFloat()
            val progressNegative = if (progress >= 0f) 0f else -progress.toFloat()
            Surface(
                Modifier
                    .weight(1f, false)
                    .padding(bottom = 1.dp),
                color = MaterialTheme.colorScheme.background
            ) {
                VerticalLinearProgressIndicator(
                    progress = progressPositive,
                )
            }


            Surface(
                Modifier
                    .weight(1f, false)
                    .padding(top = 1.dp)
                    .rotate(180f),
                color = MaterialTheme.colorScheme.background
            ) {
                VerticalLinearProgressIndicator(
                    progress = progressNegative,
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .padding(start = 8.dp)
        ) {
            Text(text = "100%", modifier = Modifier.align(Alignment.TopStart))
            Text(text = "0%", modifier = Modifier.align(Alignment.CenterStart))
            Text(text = "-100%", modifier = Modifier.align(Alignment.BottomStart))
        }
    }
}

@Composable
fun HexapodShape(
    title: String,
    percent: Double,
    orientation: Orientation,
    modifier: Modifier = Modifier
) {

    val height = 72.dp
    val width = 144.dp

    Column(modifier = modifier) {
        Text(title, modifier = Modifier.align(Alignment.CenterHorizontally))
        Surface(
            shape = CutCornerShape(20.dp),
            modifier = Modifier.size(width, height)
        ) {
            Box(
                modifier = Modifier
                    .rotate(if (percent > 0) 180f else 0f)
                    .background(
                        brush = when (orientation) {
                            Orientation.Horizontal -> Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.tertiary,
                                    MaterialTheme.colorScheme.tertiaryContainer
                                ),
                                startX = with(LocalDensity.current) { width.toPx() } * (1 - percent.absoluteValue.toFloat()),
                                endX = with(LocalDensity.current) { width.toPx() }
                            )
                            Orientation.Vertical -> Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.tertiary,
                                    MaterialTheme.colorScheme.tertiaryContainer
                                ),
                                startY = with(LocalDensity.current) { height.toPx() } * (1 - percent.absoluteValue.toFloat()),
                                endY = with(LocalDensity.current) { height.toPx() }
                            )
                        }
                    )
            )
        }
    }
}

enum class Orientation { Vertical, Horizontal }

@Preview
@Composable
fun PreviewPanel() {
    Panel(modifier = Modifier.size(400.dp))
}

@Preview
@Composable
fun PreviewHexapodShape() {
    Column() {
        AppTheme(useDarkTheme = true) {
            HexapodShape(title = "Pi", percent = 0.0, Orientation.Vertical)
        }
        AppTheme(useDarkTheme = true) {
            HexapodShape(title = "Pi", percent = -.0, Orientation.Horizontal)
        }
    }

}

val previewStatus = Status(
    Status.BatteryLevel.MEDIUM, Status.Walk.NONE, -15.0, 5.0, -15.0, false,
    isFlashLightOn = false,
    armSegments = previewSegments
)
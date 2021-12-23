package com.zelgius.controller.ui.views.panel

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstrainedLayoutReference
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope
import com.zelgius.api.model.Status
import com.zelgius.controller.ui.theme.*

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun ModeMenu(mode: Status.Walk, modifier: Modifier = Modifier, onClick: (Status.Walk) -> Unit) {
    var isMenuVisible by remember {
        mutableStateOf(false)
    }

    // TODO actualMode = mode should probably be used just here

    val defaultColor = MaterialTheme.colorScheme.primaryContainer
    val buttonColor = remember {
        Animatable(getColor(mode = mode, defaultColor))
    }

    val onClickListener: (Status.Walk) -> Unit = {
        onClick(it)
        isMenuVisible = false
    }

    LaunchedEffect(key1 = mode) {
        buttonColor.animateTo(getColor(mode, defaultColor))
    }

    ConstraintLayout(modifier) {
        val buttonSelected = createRef()
        val label = createRef()
        FloatingActionButton(modifier = Modifier
            .constrainAs(buttonSelected) {
                bottom.linkTo(parent.bottom)
                end.linkTo(parent.end)
            }
            .padding(16.dp),
            onClick = {
                isMenuVisible = !isMenuVisible
            },
            containerColor = buttonColor.value
        ) {
            AnimatedContent(targetState = mode,
                transitionSpec = {
                    (slideInVertically { height -> -height } + fadeIn() with
                            slideOutVertically { height -> height } + fadeOut()).using(
                        SizeTransform(clip = false)
                    )
                }) { mode ->
                when (mode) {
                    Status.Walk.RIPPLE -> Text(text = "X")
                    Status.Walk.TETRAPOD -> Text(text = "A")
                    Status.Walk.WAVE -> Text(text = "Y")
                    Status.Walk.TRIPOD -> Text(text = "B")
                    Status.Walk.NONE -> Icon(Icons.TwoTone.Home, contentDescription = "")
                }
            }
        }

        var angle = -START_ANGLE
        if (mode != Status.Walk.TRIPOD) {
            CircularMenuButton(
                isMenuVisible,
                centerRef = buttonSelected,
                angle = angle,
                mode = Status.Walk.TRIPOD,
                onClick = onClickListener
            )
            angle += INC_ANGLE
        }

        if (mode != Status.Walk.TETRAPOD) {
            CircularMenuButton(
                isMenuVisible,
                centerRef = buttonSelected,
                angle = angle,
                mode = Status.Walk.TETRAPOD,
                onClick = onClickListener,
            )
            angle += INC_ANGLE
        }

        if (mode != Status.Walk.WAVE) {
            CircularMenuButton(
                isMenuVisible,
                centerRef = buttonSelected,
                angle = angle,
                mode = Status.Walk.WAVE,
                onClick = onClickListener
            )
            angle += INC_ANGLE
        }

        if (mode != Status.Walk.RIPPLE) {
            CircularMenuButton(
                isMenuVisible,
                centerRef = buttonSelected,
                angle = angle,
                mode = Status.Walk.RIPPLE,
                onClick = onClickListener
            )
            angle += INC_ANGLE
        }

        if (mode != Status.Walk.NONE) {
            CircularMenuButton(
                isMenuVisible,
                centerRef = buttonSelected,
                angle = angle,
                mode = Status.Walk.NONE,
                onClick = onClickListener
            )
            angle += INC_ANGLE
        }

        ModeLabel(mode = mode, isVisible = !isMenuVisible, modifier = Modifier.constrainAs(label) {
            end.linkTo(buttonSelected.start)
            top.linkTo(buttonSelected.top)
            bottom.linkTo(buttonSelected.bottom)
        })
    }
}

private fun getColor(mode: Status.Walk, default: Color) = when (mode) {
    Status.Walk.RIPPLE -> xButtonColor
    Status.Walk.TETRAPOD -> aButtonColor
    Status.Walk.WAVE -> yButtonColor
    Status.Walk.TRIPOD -> bButtonColor
    Status.Walk.NONE -> default
}

@Composable
fun ModeLabel(mode: Status.Walk, isVisible: Boolean, modifier: Modifier = Modifier) {
    Label(
        when (mode) {
            Status.Walk.RIPPLE -> "Ripple"
            Status.Walk.TETRAPOD -> "Tetrapod"
            Status.Walk.WAVE -> "Wave"
            Status.Walk.TRIPOD -> "Tripod"
            Status.Walk.NONE -> "Home"
        },
        isVisible, modifier
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Suppress("ExperimentalAnimationApi")
@Composable
fun ConstraintLayoutScope.CircularMenuButton(
    isVisible: Boolean,
    centerRef: ConstrainedLayoutReference,
    angle: Float,
    mode: Status.Walk,
    onClick: (Status.Walk) -> Unit
) {
    val ref = createRef()
    val label = createRef()
    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(),
        exit = scaleOut(),
        modifier = Modifier
            .size(40.dp)
            .constrainAs(ref) {
                circular(centerRef, angle, 80.dp)
            },
    ) {
        FloatingActionButton(
            onClick = { onClick(mode) },
            shape = RoundedCornerShape(20.dp),
            containerColor = getColor(mode, MaterialTheme.colorScheme.primaryContainer)
        ) {
            when (mode) {
                Status.Walk.RIPPLE -> Text(text = "X")
                Status.Walk.TETRAPOD -> Text(text = "A")
                Status.Walk.WAVE -> Text(text = "Y")
                Status.Walk.TRIPOD -> Text(text = "B")
                Status.Walk.NONE -> Icon(
                    Icons.TwoTone.Home,
                    contentDescription = "",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(),
        exit = scaleOut(),
        modifier = Modifier
            .constrainAs(label) {
                if (angle < 0) {
                    end.linkTo(ref.start)
                    top.linkTo(ref.top)
                    bottom.linkTo(ref.bottom)
                } else {
                    end.linkTo(ref.end)
                    start.linkTo(ref.start)
                    bottom.linkTo(ref.top)
                }
            }.let {
                if (angle < 0) {
                    it.padding(end = 4.dp)
                } else {
                    it.padding(bottom = 4.dp)
                }
            },
    ) {
        ModeLabel(mode = mode, isVisible = isVisible)
    }

}

@OptIn(ExperimentalAnimationApi::class)
@Suppress("ExperimentalAnimationApi")
@Composable
fun Label(text: String, isVisible: Boolean, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = isVisible,
        enter = scaleIn(),
        exit = scaleOut(),
        modifier = modifier,
    ) {
        Text(
            text,
            style = TextStyle(color = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    RoundedCornerShape(4.dp)
                )
                .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
        )
    }
}

private const val START_ANGLE = 100f
private const val INC_ANGLE = 37f

@Preview(showBackground = true)
@Composable
fun PreviewModeMenu() {
    AppTheme(useDarkTheme = false) {
        var mode by remember {
            mutableStateOf(Status.Walk.RIPPLE)
        }
        Surface {
            ModeMenu(mode, Modifier.fillMaxSize()) { mode = it }
        }
    }
}


@Preview
@Composable
fun PreviewLabel() {
    AppTheme(useDarkTheme = true) {
        Label(text = "Test", isVisible = true)
    }
}


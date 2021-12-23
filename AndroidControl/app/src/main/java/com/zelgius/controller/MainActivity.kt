package com.zelgius.controller

import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zelgius.api.model.ConnectionStatus
import com.zelgius.controller.ui.theme.AppTheme
import com.zelgius.controller.ui.views.panel.*
import com.zelgius.controller.ui.views.stream.StreamPlayer
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                // A surface container using the "background" color from the theme
                MainContent()
            }
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

@Composable
fun MainContent(viewModel: PanelViewModel = viewModel()) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val status by viewModel.status
        val connectionStatus by viewModel.connectionStatus.collectAsState(ConnectionStatus.CONNECTING)
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.aspectRatio(16f / 9f)) {
                StreamPlayer(modifier = Modifier.fillMaxSize(), rotate = 180f)

                when (connectionStatus) {
                    ConnectionStatus.CONNECTING -> Text(
                        text = stringResource(id = R.string.connecting),
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.Black.copy(alpha = 0.80f))
                            .align(Center)
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    ConnectionStatus.ERROR -> {
                        Column(modifier = Modifier.align(Center)) {
                            Text(
                                text = stringResource(id = R.string.connection_failed),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color.Black.copy(alpha = 0.80f))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            Button(
                                onClick = { viewModel.retry() },
                                modifier = Modifier.align(CenterHorizontally)
                            ) {
                                Text(text = stringResource(id = R.string.retry))
                            }
                        }

                    }
                    ConnectionStatus.CONNECTED -> {}
                }
            }

            HorizontalSlidePanel(
                remainingWidth = maxWidth - maxHeight * 16f / 9f,
                targetWidth = 400.dp,
                modifier = Modifier.align(
                    Alignment.CenterEnd
                )
            ) {
                Panel(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    viewModel = viewModel
                )
            }

            ModeMenu(
                mode = status.walk,
                onClick = { mode ->
                    viewModel.setMode(mode)
                },
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val configuration = LocalConfiguration.current
            when (configuration.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> {
                    Row {
                        Box(
                            Modifier
                                .aspectRatio(1f)
                                .background(color = MaterialTheme.colorScheme.inverseSurface)
                        ) {}
                        Panel(

                            modifier = Modifier
                                .fillMaxWidth()
                                .background(color = MaterialTheme.colorScheme.surface)
                        )
                    }
                }
                else -> {
                    Column {
                        Box(
                            Modifier
                                .aspectRatio(1f)
                                .background(color = MaterialTheme.colorScheme.inverseSurface)
                        ) {}
                        Panel(
                            modifier = Modifier
                                .fillMaxHeight()
                                .background(color = MaterialTheme.colorScheme.surface)
                        )
                    }
                }
            }
        }
    }
}
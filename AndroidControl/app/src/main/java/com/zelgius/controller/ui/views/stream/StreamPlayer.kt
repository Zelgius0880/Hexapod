package com.zelgius.controller.ui.views.stream

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.net.Uri
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import com.zelgius.api.Result
import com.zelgius.mjpg_streamer.MjpegInputStream
import com.zelgius.mjpg_streamer.MjpegView
import com.zelgius.mjpg_streamer.State
import kotlinx.coroutines.launch


@Composable
fun StreamPlayer(modifier: Modifier = Modifier, rotate: Float = 0f) {
    PlayerMjpegView(modifier, rotate)
}

@Composable
fun PlayerMjpegView(
    modifier: Modifier = Modifier,
    rotate: Float = 0f,
    viewModel: StreamViewModel = viewModel()
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(modifier = modifier, factory = { context ->
        MjpegView(context, rotate).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

        }
    }, update = { view ->
        view.setOnClickListener {
            listenBitmap(viewModel, lifecycleOwner, view)
        }
        listenBitmap(viewModel, lifecycleOwner, view)
    })
}

private fun listenBitmap(
    viewModel: StreamViewModel,
    lifecycleOwner: LifecycleOwner,
    view: MjpegView
) {
    if(!viewModel.isConnected) {
        viewModel.startStream().asLiveData()
            .observe(lifecycleOwner) { result: Result<MjpegInputStream> ->
                when (result) {
                    is Result.Error -> {
                        view.drawErrorFrame()
                    }
                    is Result.Success -> {
                        viewModel.bitmap.observe(lifecycleOwner) {
                            view.drawFrame(it)
                        }
                        viewModel.state.observe(lifecycleOwner) {
                            if (it == State.ERROR) view.drawErrorFrame()
                        }
                    }
                }
            }
    }
}

@SuppressLint("UnsafeOptInUsageError")
private fun buildMediaSource(context: Context, uri: Uri): MediaSource {
    val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(context)
    return HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
}

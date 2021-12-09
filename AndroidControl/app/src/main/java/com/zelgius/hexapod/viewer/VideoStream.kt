package com.zelgius.hexapod.viewer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.util.AttributeSet
import android.view.Surface
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import androidx.core.net.toUri
import java.io.IOException


class VideoStream @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyle: Int = 0) :
        TextureView(context!!, attrs, defStyle), SurfaceTextureListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnErrorListener {
    private var mediaPlayer: MediaPlayer? = null
    private var source: Uri? = "http://192.168.1.30:8080/stream/video.jpeg".toUri()
    private var completionListener: OnCompletionListener? = null
    private var isLooping = false

    var listener : (Bitmap) -> Unit = {}

    fun setOnCompletionListener(listener: OnCompletionListener?) {
        completionListener = listener
    }

    fun setLooping(looping: Boolean) {
        isLooping = looping
    }

    override fun onDetachedFromWindow() {
        // release resources on detach
        if (mediaPlayer != null) {
            mediaPlayer!!.release()
            mediaPlayer = null
        }
        super.onDetachedFromWindow()
    }

    /*
    * TextureView.SurfaceTextureListener
    */
    override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
        val surface = Surface(surfaceTexture)
        try {
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setOnCompletionListener(completionListener)
            mediaPlayer!!.setOnBufferingUpdateListener(this)
            mediaPlayer!!.setOnErrorListener(this)
            mediaPlayer!!.isLooping = isLooping
            mediaPlayer!!.setDataSource(context, source!!)
            mediaPlayer!!.setSurface(surface)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            mediaPlayer!!.reset()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        surface.release()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        listener(bitmap!!)
    }

    init {
        surfaceTextureListener = this
    }

    override fun onBufferingUpdate(mp: MediaPlayer?, percent: Int) {
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        return false
    }
}
package com.zelgius.androidviewer

import android.app.Activity
import android.content.res.AssetManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import com.zelgius.androidviewer.MainActivity.Renderer
import com.zelgius.androidviewer.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.coroutines.CoroutineContext


class MainActivity : Activity(), CoroutineScope {
    companion object {
        init {
            System.loadLibrary("cardboard_jni")
        }
    }

    // Opaque native pointer to the native CardboardApp instance.
    // This object is owned by the VrActivity instance and passed to the native methods.
    private var nativeApp: Long = 0

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main


    override fun onDestroy() {
        super.onDestroy()
        //job.cancel()
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nativeApp = nativeOnCreate(assets)
        job = Job()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playerView.apply {
            setEGLContextClientVersion(2)
            val renderer = Renderer()
            setRenderer(renderer)
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }

        /*launch {
            binding.playerView.setSource(MjpegInputStream.read("http://192.168.1.38:8080/stream/video.mjpeg")!!)
            binding.playerView.setDisplayMode(MjpegView.SIZE_BEST_FIT)
            binding.playerView.showFps(false)
        }*/

        // start play automatically when player is ready.

        // start play automatically when player is ready.
    }

    override fun onPause() {
        super.onPause()
        //binding.playerView.stopPlayback()
    }


    override fun onResume() {
        super.onResume()
        //binding.playerView.startPlayback()
        nativeOnPause(nativeApp)
        binding.playerView.onPause()

    }


    inner class Renderer : GLSurfaceView.Renderer {
        override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {
            nativeOnSurfaceCreated(nativeApp)
        }

        override fun onSurfaceChanged(gl10: GL10, width: Int, height: Int) {
            nativeSetScreenParams(nativeApp, width, height)
        }

        override fun onDrawFrame(gl10: GL10) {
            nativeOnDrawFrame(nativeApp)
        }
    }

    private external fun nativeOnCreate(assetManager: AssetManager): Long

    private external fun nativeOnDestroy(nativeApp: Long)

    private external fun nativeOnSurfaceCreated(nativeApp: Long)

    private external fun nativeOnDrawFrame(nativeApp: Long)

    private external fun nativeOnTriggerEvent(nativeApp: Long)

    private external fun nativeOnPause(nativeApp: Long)

    private external fun nativeOnResume(nativeApp: Long)

    private external fun nativeSetScreenParams(nativeApp: Long, width: Int, height: Int)

    private external fun nativeSwitchViewer(nativeApp: Long)
}
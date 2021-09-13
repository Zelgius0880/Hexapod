/*
 * Copyright 2019 Google LLC
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
package com.zelgius.hexapod.viewer

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.*
import android.media.MediaPlayer
import android.net.Uri
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.withRotation
import androidx.core.graphics.withSave
import com.zelgius.androidviewer.MjpegInputStream
import com.zelgius.androidviewer.MjpegView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.nio.ByteBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.random.Random
import android.net.wifi.WifiManager
import android.net.wifi.WifiManager.MulticastLock
import com.zelgius.hexapod.viewer.overlay.OverlayInfo
import com.zelgius.hexapod.viewer.overlay.WalkMode
import com.zelgius.hexapod.viewer.overlay.drawOverlay


/**
 * A Google Cardboard VR NDK sample application.
 *
 *
 * This is the main Activity for the sample application. It initializes a GLSurfaceView to allow
 * rendering.
 */
class VrActivity : AppCompatActivity(), PopupMenu.OnMenuItemClickListener {
    companion object {
        private val TAG = VrActivity::class.java.simpleName

        // Permission request codes
        private const val PERMISSIONS_REQUEST_CODE = 2

        init {
            System.loadLibrary("cardboard_jni")
        }
    }


    var leftSurfaceTexture: SurfaceTexture? = null

    // Opaque native pointer to the native CardboardApp instance.
    // This object is owned by the VrActivity instance and passed to the native methods.
    private var nativeApp: Long = 0
    private lateinit var glView: GLSurfaceView
    private var textureHandlers: IntArray? = null

    private var left: MjpegInputStream? = null
    private var right: MjpegInputStream? = null

    @SuppressLint("ClickableViewAccessibility")
    public override fun onCreate(savedInstance: Bundle?) {
        super.onCreate(savedInstance)
        nativeApp = nativeOnCreate(assets)
        setContentView(R.layout.activity_vr)
        glView = findViewById(R.id.surface_view)
        glView.setEGLContextClientVersion(2)
        val renderer: Renderer = Renderer()
        glView.setRenderer(renderer)
        glView.renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        glView.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Signal a trigger event.
                glView.queueEvent(
                    Runnable { nativeOnTriggerEvent(nativeApp) })
                true
            } else
                false
        }

        /*val wifi = getSystemService(Context.WIFI_SERVICE) as WifiManager
        if (wifi != null) {
            val lock = wifi.createMulticastLock("mylock")
            lock.acquire()
        }*/

        CoroutineScope(Dispatchers.IO).launch {
            left = MjpegInputStream.read("http://192.168.1.30:8080?action=stream")!!
            right = MjpegInputStream.read("http://192.168.1.62:8080?action=stream")!!
        }

        // TODO(b/139010241): Avoid that action and status bar are displayed when pressing settings
        // button.
        setImmersiveSticky()
        val decorView = window.decorView
        decorView.setOnSystemUiVisibilityChangeListener { visibility: Int ->
            if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                setImmersiveSticky()
            }
        }

        // Forces screen to max brightness.
        val layout = window.attributes
        layout.screenBrightness = 1f
        window.attributes = layout

        // Prevents screen from dimming/locking.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        super.onPause()
        nativeOnPause(nativeApp)
        glView!!.onPause()
    }

    override fun onResume() {
        super.onResume()

        // On Android P and below, checks for activity to READ_EXTERNAL_STORAGE. When it is not granted,
        // the application will request them. For Android Q and above, READ_EXTERNAL_STORAGE is optional
        // and scoped storage will be used instead. If it is provided (but not checked) and there are
        // device parameters saved in external storage those will be migrated to scoped storage.
        glView.onResume()
        nativeOnResume(nativeApp)
    }

    override fun onDestroy() {
        super.onDestroy()
        nativeOnDestroy(nativeApp)
        nativeApp = 0
        leftSurfaceTexture?.release()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            setImmersiveSticky()
        }
    }


    private inner class Renderer : GLSurfaceView.Renderer {
        lateinit var bitmap: Bitmap
        override fun onSurfaceCreated(gl10: GL10, eglConfig: EGLConfig) {
            nativeOnSurfaceCreated(nativeApp)
        }

        override fun onSurfaceChanged(gl10: GL10, width: Int, height: Int) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            //drawBitmaps()
            textureHandlers = null
            nativeSetScreenParams(nativeApp, width, height)
        }

        override fun onDrawFrame(gl10: GL10) {
            drawBitmaps()
            nativeOnDrawFrame(nativeApp, bitmap.toByteArray(), bitmap.toByteArray())
        }

        fun drawBitmaps() {
            left?.lastFrame?.let {
                synchronized(it) {
                    if (!it.isRecycled) {
                        val overlay = it.drawOverlay(
                            this@VrActivity,
                            OverlayInfo(50, WalkMode.WalkMode.TRIPOD, 0)
                        )
                        Canvas(bitmap).drawBitmap(
                            overlay, null, Rect(
                                0, 0, bitmap.width / 2, bitmap.height,
                            ), Paint()
                        )
                    }
                }
            } ?: drawSample(0, bitmap.width / 2, bitmap)

            right?.lastFrame?.let {
                synchronized(it) {
                    if (!it.isRecycled) {
                        val overlay = it.drawOverlay(
                            this@VrActivity,
                            OverlayInfo(50, WalkMode.WalkMode.TRIPOD, 0)
                        )
                        Canvas(bitmap).drawBitmap(
                            overlay,
                            null,
                            Rect(bitmap.width / 2, 0, bitmap.width, bitmap.height),
                            Paint()
                        )

                        overlay.recycle()
                    }
                }
            } ?: drawSample(bitmap.width / 2, bitmap.width, bitmap)
        }

        fun drawSample(from: Int, to: Int, bitmap: Bitmap) {
            with(Canvas(bitmap)) {
                val paint = Paint()
                paint.color = Color.WHITE
                drawRect(Rect(from, 0, to, bitmap.height), paint)

                paint.color = Color.BLUE
                paint.strokeWidth = 20f

                var top = Random.nextInt(10, bitmap.height - 110)
                var left = Random.nextInt(from, to - 110)

                drawRect(
                    left.toFloat(),
                    top.toFloat(),
                    left + 100f,
                    top + 100f,
                    paint
                )

                top = Random.nextInt(10, bitmap.height - 110)
                left = Random.nextInt(from, to - 110)

                drawOval(
                    left.toFloat(),
                    top.toFloat(),
                    left + 100f,
                    top + 100f,
                    paint
                )
            }
        }
    }

    /** Callback for when close button is pressed.  */
    fun closeSample(view: View?) {
        Log.d(TAG, "Leaving VR sample")
        finish()
    }

    /** Callback for when settings_menu button is pressed.  */
    fun showSettings(view: View?) {
        val popup = PopupMenu(this, view)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.settings_menu, popup.menu)
        popup.setOnMenuItemClickListener(this)
        popup.show()
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        if (item.itemId == R.id.switch_viewer) {
            nativeSwitchViewer(nativeApp)
            return true
        }
        return false
    }

    /**
     * Checks for READ_EXTERNAL_STORAGE permission.
     *
     * @return whether the READ_EXTERNAL_STORAGE is already granted.
     */
    private val isReadExternalStorageEnabled: Boolean
        private get() = (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
                == PackageManager.PERMISSION_GRANTED)

    /** Handles the requests for activity permission to READ_EXTERNAL_STORAGE.  */
    private fun requestPermissions() {
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE)
    }

    /**
     * Callback for the result from requesting permissions.
     *
     *
     * When READ_EXTERNAL_STORAGE permission is not granted, the settings view will be launched
     * with a toast explaining why it is required.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (!isReadExternalStorageEnabled) {
            Toast.makeText(this, R.string.read_storage_permission, Toast.LENGTH_LONG).show()
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                // Permission denied with checking "Do not ask again". Note that in Android R "Do not ask
                // again" is not available anymore.
                launchPermissionsSettings()
            }
            finish()
        }
    }

    private fun launchPermissionsSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", packageName, null)
        startActivity(intent)
    }

    private fun setImmersiveSticky() {
        window.setDecorFitsSystemWindows(false)
        val controller = window.insetsController
        if (controller != null) {
            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

    }

    fun Bitmap.toByteArray(): ByteArray {
        val size: Int = rowBytes * height
        val byteBuffer = ByteBuffer.allocate(size)
        copyPixelsToBuffer(byteBuffer)
        return byteBuffer.array()
    }

    private external fun nativeOnCreate(assetManager: AssetManager): Long
    private external fun nativeOnDestroy(nativeApp: Long)
    private external fun nativeOnSurfaceCreated(nativeApp: Long)
    private external fun nativeOnDrawFrame(nativeApp: Long, left: ByteArray, right: ByteArray)
    private external fun nativeOnTriggerEvent(nativeApp: Long)
    private external fun nativeOnPause(nativeApp: Long)
    private external fun nativeOnResume(nativeApp: Long)
    private external fun nativeSetScreenParams(nativeApp: Long, width: Int, height: Int)
    private external fun nativeSwitchViewer(nativeApp: Long)
    private external fun nativeGetTextureHandlers(nativeApp: Long): IntArray

}
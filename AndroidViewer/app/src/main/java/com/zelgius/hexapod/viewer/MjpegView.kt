package com.zelgius.androidviewer

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.IOException

class MjpegView : SurfaceView, SurfaceHolder.Callback {
    private var thread: MjpegViewThread? = null
    private var mIn: MjpegInputStream? = null
    private var showFps = false
    private var mRun = false
    private var surfaceDone = false
    private var overlayPaint: Paint? = null
    private var overlayTextColor = 0
    private var overlayBackgroundColor = 0
    private var ovlPos = 0
    private var dispWidth = 0
    private var dispHeight = 0
    private var displayMode = 0

    inner class MjpegViewThread(private val mSurfaceHolder: SurfaceHolder) :
        Thread() {
        private var frameCounter = 0
        private var start: Long = 0
        private var ovl: Bitmap? = null
        private fun destRect(_bmw: Int, _bmh: Int): Rect? {
            var bmw = _bmw
            var bmh = _bmh
            val tempX: Int
            val tempY: Int
            if (displayMode == SIZE_STANDARD) {
                tempX = dispWidth / 2 - bmw / 2
                tempY = dispHeight / 2 - bmh / 2
                return Rect(tempX, tempY, bmw + tempX, bmh + tempY)
            }
            if (displayMode == SIZE_BEST_FIT) {
                val bmasp = bmw.toFloat() / bmh.toFloat()
                bmw = dispWidth
                bmh = (dispWidth / bmasp).toInt()
                if (bmh > dispHeight) {
                    bmh = dispHeight
                    bmw = (dispHeight * bmasp).toInt()
                }
                tempX = dispWidth / 2 - bmw / 2
                tempY = dispHeight / 2 - bmh / 2
                return Rect(tempX, tempY, bmw + tempX, bmh + tempY)
            }
            return if (displayMode == SIZE_FULLSCREEN) Rect(0, 0, dispWidth, dispHeight) else null
        }

        fun setSurfaceSize(width: Int, height: Int) {
            synchronized(mSurfaceHolder) {
                dispWidth = width
                dispHeight = height
            }
        }

        private fun makeFpsOverlay(p: Paint?, text: String): Bitmap {
            val b = Rect()
            p!!.getTextBounds(text, 0, text.length, b)
            val bWidth = b.width() + 2
            val bHeight = b.height() + 2
            val bm = Bitmap.createBitmap(bWidth, bHeight, Bitmap.Config.ARGB_8888)
            val c = Canvas(bm)
            p.color = overlayBackgroundColor
            c.drawRect(0f, 0f, bWidth.toFloat(), bHeight.toFloat(), p)
            p.color = overlayTextColor
            c.drawText(
                text,
                (-b.left + 1).toFloat(),
                bHeight / 2 - (p.ascent() + p.descent()) / 2 + 1,
                p
            )

            return bm
        }

        override fun run() {
            start = System.currentTimeMillis()
            val mode = PorterDuffXfermode(PorterDuff.Mode.DST_OVER)
            var bm: Bitmap
            var width: Int
            var height: Int
            var destRect: Rect?
            var c: Canvas? = null
            val p = Paint()
            var fps: String
            while (mRun) {
                if (surfaceDone) {
                    try {
                        c = mSurfaceHolder.lockCanvas()
                        synchronized(mSurfaceHolder) {
                            try {
                                bm = mIn!!.readMjpegFrame()
                                destRect = destRect(bm.width, bm.height)
                                c.drawColor(Color.BLACK)
                                c.drawBitmap(bm, null, destRect!!, p)
                                if (showFps) {
                                    p.xfermode = mode
                                    if (ovl != null) {
                                        height =
                                            if (ovlPos and 1 == 1) destRect!!.top else destRect!!.bottom - ovl!!.height
                                        width =
                                            if (ovlPos and 8 == 8) destRect!!.left else destRect!!.right - ovl!!.width
                                        c.drawBitmap(ovl!!, width.toFloat(), height.toFloat(), null)
                                    }
                                    p.xfermode = null
                                    frameCounter++
                                    if (System.currentTimeMillis() - start >= 1000) {
                                        fps = frameCounter.toString() + "fps"
                                        frameCounter = 0
                                        start = System.currentTimeMillis()
                                        ovl = makeFpsOverlay(overlayPaint, fps)
                                    }
                                }
                            } catch (e: IOException) {
                            }
                        }
                    } finally {
                        if (c != null) mSurfaceHolder.unlockCanvasAndPost(c)
                    }
                }
            }
        }
    }

    private fun init() {
        val holder = holder
        holder.addCallback(this)
        thread = MjpegViewThread(holder)
        isFocusable = true
        overlayPaint = Paint()
        overlayPaint!!.textAlign = Paint.Align.LEFT
        overlayPaint!!.textSize = 12f
        overlayPaint!!.typeface = Typeface.DEFAULT
        overlayTextColor = Color.WHITE
        overlayBackgroundColor = Color.BLACK
        ovlPos = POSITION_LOWER_RIGHT
        displayMode = SIZE_STANDARD
        dispWidth = width
        dispHeight = height
    }

    fun startPlayback() {
        if (mIn != null) {
            mRun = true
            thread!!.start()
        }
    }

    fun stopPlayback() {
        mRun = false
        var retry = true
        while (retry) {
            try {
                thread!!.join()
                retry = false
            } catch (e: InterruptedException) {
            }
        }
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    override fun surfaceChanged(holder: SurfaceHolder, f: Int, w: Int, h: Int) {
        thread!!.setSurfaceSize(w, h)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        surfaceDone = false
        stopPlayback()
    }

    constructor(context: Context) : super(context) {
        init()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        surfaceDone = true
    }

    fun showFps(b: Boolean) {
        showFps = b
    }

    fun setSource(source: MjpegInputStream) {
        mIn = source
        startPlayback()
    }

    fun setOverlayPaint(p: Paint?) {
        overlayPaint = p
    }

    fun setOverlayTextColor(c: Int) {
        overlayTextColor = c
    }

    fun setOverlayBackgroundColor(c: Int) {
        overlayBackgroundColor = c
    }

    fun setOverlayPosition(p: Int) {
        ovlPos = p
    }

    fun setDisplayMode(s: Int) {
        displayMode = s
    }

    companion object {
        const val POSITION_UPPER_LEFT = 9
        const val POSITION_UPPER_RIGHT = 3
        const val POSITION_LOWER_LEFT = 12
        const val POSITION_LOWER_RIGHT = 6
        const val SIZE_STANDARD = 1
        const val SIZE_BEST_FIT = 4
        const val SIZE_FULLSCREEN = 8
    }
}
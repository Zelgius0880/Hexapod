package com.zelgius.androidviewer

import android.graphics.*
import android.view.SurfaceHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.net.URL
import java.util.*

class MjpegInputStream(`in`: InputStream, private val onBitmapReady: (Bitmap) -> Unit) :
        DataInputStream(BufferedInputStream(`in`, FRAME_MAX_LENGTH)) {
    val thread = MjpegViewThread()
    private var _lastFrame: Bitmap? = null
    val lastFrame: Bitmap?
        get() = _lastFrame

    var run: Boolean = true

    private val SOI_MARKER = byteArrayOf(0xFF.toByte(), 0xD8.toByte())
    private val EOF_MARKER = byteArrayOf(0xFF.toByte(), 0xD9.toByte())
    private val CONTENT_LENGTH = "Content-Length"
    private var mContentLength = -1

    @Throws(IOException::class)
    private fun getEndOfSeqeunce(`in`: DataInputStream, sequence: ByteArray): Int {
        var seqIndex = 0
        var c: Byte
        for (i in 0 until FRAME_MAX_LENGTH) {
            c = `in`.readUnsignedByte().toByte()
            if (c == sequence[seqIndex]) {
                seqIndex++
                if (seqIndex == sequence.size) return i + 1
            } else seqIndex = 0
        }
        return -1
    }

    @Throws(IOException::class)
    private fun getStartOfSequence(`in`: DataInputStream, sequence: ByteArray): Int {
        val end = getEndOfSeqeunce(`in`, sequence)
        return if (end < 0) -1 else end - sequence.size
    }

    @Throws(IOException::class, NumberFormatException::class)
    private fun parseContentLength(headerBytes: ByteArray): Int {
        val headerIn = ByteArrayInputStream(headerBytes)
        val props = Properties()
        props.load(headerIn)
        return props.getProperty(CONTENT_LENGTH).toInt()
    }

    @Throws(IOException::class)
    fun readMjpegFrame(): Bitmap {
        mark(FRAME_MAX_LENGTH)
        val headerLen = getStartOfSequence(this, SOI_MARKER)
        reset()
        val header = ByteArray(headerLen)
        readFully(header)
        mContentLength = try {
            parseContentLength(header)
        } catch (nfe: NumberFormatException) {
            getEndOfSeqeunce(this, EOF_MARKER)
        }
        reset()
        val frameData = ByteArray(mContentLength)
        skipBytes(headerLen)
        readFully(frameData)
        return BitmapFactory.decodeStream(ByteArrayInputStream(frameData))
    }

    fun start() {
        thread.start()
    }

    fun stop() {
        run = false
        close()
    }

    companion object {
        private const val HEADER_MAX_LENGTH = 100
        private const val FRAME_MAX_LENGTH = 40000 + HEADER_MAX_LENGTH

        suspend fun read(url: String?, onBitmapReady: (Bitmap) -> Unit = {}, autoStart: Boolean = true): MjpegInputStream? = withContext(Dispatchers.IO) {

            val urlConnection = URL(url).openConnection()
            try {
                return@withContext MjpegInputStream(urlConnection.getInputStream(), onBitmapReady).apply {
                    if (autoStart) start()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return@withContext null
        }
    }

    inner class MjpegViewThread :
            Thread() {
        private var start: Long = 0

        override fun run() {
            start = System.currentTimeMillis()
            while (run) {
                try {
                    readMjpegFrame().apply {
                        onBitmapReady(this)

                        if (lastFrame != null)
                            lastFrame?.let {
                                synchronized(it) {
                                    it.recycle()
                                    _lastFrame = this
                                }
                            }
                        else
                            _lastFrame = this

                    }
                } catch (e: IOException) {
                }
            }
        }
    }
}
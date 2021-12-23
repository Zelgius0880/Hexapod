package com.zelgius.mjpg_streamer

import android.graphics.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.*
import java.net.URL
import java.util.*
import kotlin.random.Random

class MjpegInputStream(`in`: InputStream) :
    DataInputStream(BufferedInputStream(`in`, FRAME_MAX_LENGTH)) {
    private val _flow: MutableStateFlow<Bitmap?> =
        MutableStateFlow(
            Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888).apply {
                MjpegView.drawPlaceholder(Canvas(this))
            })

    private val _status: MutableStateFlow<State> =
        MutableStateFlow(State.SUCCESS)
    val status: Flow<State> get() = _status

    val flow: Flow<Bitmap?> get() = _flow
    private val lastFrame: Bitmap?
        get() = _flow.value

    private var run: Boolean = true
    private var start: Long = 0

    private var mContentLength = -1

    private var job: Job? = null

    @Throws(IOException::class)
    private fun getEndOfSequence(`in`: DataInputStream, sequence: ByteArray): Int {
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
        val end = getEndOfSequence(`in`, sequence)
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
            getEndOfSequence(this, EOF_MARKER)
        }
        reset()
        val frameData = ByteArray(mContentLength)
        skipBytes(headerLen)
        readFully(frameData)
        return BitmapFactory.decodeStream(ByteArrayInputStream(frameData))
    }

    fun start() {
        job = CoroutineScope(Dispatchers.IO).launch {
            start = System.currentTimeMillis()

            _status.value = State.SUCCESS
            while (run) {
                kotlin.runCatching {
                    readMjpegFrame()
                }
                    .onSuccess { result ->
                        lastFrame?.let {
                            synchronized(it) {
                                if (!it.isRecycled) it.recycle()
                            }
                        }
                        _flow.value = result
                    }
                    .onFailure { stop() }

            }
        }
    }


    fun stop() {
        run = false
        job?.cancel()
        close()
        _status.value = State.ERROR
    }

    companion object {
        private const val HEADER_MAX_LENGTH = 100
        private const val FRAME_MAX_LENGTH = 40000 + HEADER_MAX_LENGTH
        private val SOI_MARKER = byteArrayOf(0xFF.toByte(), 0xD8.toByte())
        private val EOF_MARKER = byteArrayOf(0xFF.toByte(), 0xD9.toByte())
        private val CONTENT_LENGTH = "Content-Length"

        suspend fun read(
            url: String?,
            autoStart: Boolean = true
        ) = coroutineScope {
            kotlin.runCatching {
                val job = async(Dispatchers.IO) {
                    URL(url).openConnection()
                }
                val urlConnection = job.await()

                val stream = withContext(Dispatchers.IO) {
                    MjpegInputStream(urlConnection.getInputStream()).apply {
                        if (autoStart) start()
                    }
                }

                stream
            }
        }
    }
}

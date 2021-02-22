package com.zelgius.hexapod.viewer.overlay

import android.Manifest
import android.app.Application
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.graphics.*
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(InstantExecutorExtension::class)
internal class HudDrawingTest {
    @field:Rule
    public val readStorageRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE);
    @field:Rule
    public val writeStorageRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    @field:Rule
    public val mediaLocationRule = GrantPermissionRule.grant(Manifest.permission.ACCESS_MEDIA_LOCATION );

    @Test
    fun drawAndSaveBitmap() {
        Bitmap.createBitmap(1200, 600, Bitmap.Config.ARGB_8888).apply {

            Canvas(this).apply {
                drawSample(0, width / 2, this)
                drawSample(width / 2, width, this)

                BatteryLevel(this).draw(70)
            }
            saveFileToDownloads("result.png", this)
        }

    }

    private fun saveFileToDownloads(name: String, bitmap: Bitmap) {
        val context: Application = ApplicationProvider.getApplicationContext()!!
        try {
            val fos: OutputStream? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver: ContentResolver = context.contentResolver
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)

                resolver.openOutputStream(
                        resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.MediaColumns._ID),
                                "${MediaStore.MediaColumns.DISPLAY_NAME} = ?", arrayOf(name), null)!!.let {
                            if (!it.moveToFirst())
                                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)!!
                            else
                                ContentUris
                                        .withAppendedId(MediaStore.Images.Media.getContentUri("external"),
                                                it.getLong(it.getColumnIndex(MediaStore.MediaColumns._ID)))
                        }, "rwt")
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString()
                val image = File(imagesDir, name)
                FileOutputStream(image)
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos?.close()
        } catch (e: IOException) {
            // Log Message
        }
    }

    fun drawSample(from: Int, to: Int, canvas: Canvas) {
        canvas.apply {
            val paint = Paint()
            paint.color = Color.LTGRAY
            drawRect(Rect(from, 0, to, height), paint)

            paint.color = Color.BLUE
            paint.strokeWidth = 20f

            var top = Random.nextInt(10, height - 110)
            var left = Random.nextInt(from, to - 110)

            drawRect(
                    left.toFloat(),
                    top.toFloat(),
                    left + 100f,
                    top + 100f,
                    paint
            )

            top = Random.nextInt(10, height - 110)
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
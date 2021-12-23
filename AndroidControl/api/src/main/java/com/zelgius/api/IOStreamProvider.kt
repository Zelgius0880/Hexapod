package com.zelgius.api

import java.io.InputStream
import java.io.OutputStream

interface IOStreamProvider {
    suspend fun getInputStream(): InputStream
    suspend fun getOutputStream(): OutputStream
    fun close()
}
package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject


private fun loadImageBytes(imagePath: String): ByteArray {
    return try {
        val file = File(imagePath)
        val bitmap: Bitmap = BitmapFactory.decodeFile(file.path)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.toByteArray()
    } catch (e: IOException) {
        e.printStackTrace()
        ByteArray(0)
    }
}

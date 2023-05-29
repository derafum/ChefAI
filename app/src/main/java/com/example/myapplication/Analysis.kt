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

class Analysis(private val apiKey: String, private val id: String, private val version: Int) {

    fun analyzeImage(imgBytes: ByteArray): List<String> {
        val prediction = getPredict(imgBytes)
        val classes = mutableListOf<String>()

        val predictionsArray = prediction.getJSONArray("predictions")
        for (i in 0 until predictionsArray.length()) {
            val result = predictionsArray.getJSONObject(i)
            val className = result.getString("class")
            if (!classes.contains(className)) {
                classes.add(className)
            }
        }

        return classes
    }

    private fun getPredict(imgBytes: ByteArray): JSONObject {
        val client = OkHttpClient()
        val requestBody = RequestBody.create("application/octet-stream".toMediaType(), imgBytes)
        val request = Request.Builder()
            .url("https://api.roboflow.com/object-detection/predict/$id")
            .post(requestBody)
            .addHeader("Content-Type", "application/octet-stream")
            .addHeader("Authorization", apiKey)
            .addHeader("modelVersion", version.toString())
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw IOException("Отсутствует тело ответа")

        return JSONObject(responseBody)
    }
}



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

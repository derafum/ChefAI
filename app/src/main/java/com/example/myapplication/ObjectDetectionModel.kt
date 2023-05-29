package com.example.myapplication

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException

private const val API_KEY = "hzA1SfCPcpXoK4L5LAKe"
private const val ID = "mortuus-stellaris-raxs6/-object-detection-pukbl/3"
private const val VERSION = 3

class ObjectDetectionModel(private val apiKey: String, private val id: String, private val version: Int) {

    fun getPredict(imageBitmap: Bitmap): List<String> {
        val requestBody = imageBitmap.toRequestBody()
        val request = createRequest(requestBody)
        val response = sendRequest(request)
        return parseResponse(response)
    }

    private fun Bitmap.toRequestBody(): RequestBody {
        val byteArrayOutputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return byteArray.toRequestBody("image/jpeg".toMediaTypeOrNull())
    }

    private fun createRequest(requestBody: RequestBody): Request {
        val url = "https://detect.roboflow.com/$id/$version"
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "image.jpg", requestBody)
            .build()
        return Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()
    }

    private fun sendRequest(request: Request): String {
        val client = OkHttpClient()
        val response = client.newCall(request).execute()
        return response.body?.string() ?: ""
    }

    private fun parseResponse(response: String): List<String> {
        val classes = mutableListOf<String>()
        try {
            val jsonObject = JSONObject(response)
            val predictions = jsonObject.getJSONArray("predictions")
            for (i in 0 until predictions.length()) {
                val result = predictions.getJSONObject(i)
                val className = result.getString("class")
                if (!classes.contains(className)) {
                    classes.add(className)
                }
            }
        } catch (e: Exception) {
            Log.e("ObjectDetectionModel", "Failed to parse response: $response", e)
        }
        return classes
    }
}

fun main() {
    val apiKey = API_KEY
    val id = ID
    val version = VERSION

    val imageBitmap = BitmapFactory.decodeFile("test_img.jpg") // Replace with your image source
    val model = ObjectDetectionModel(apiKey, id, version)
    val prediction = model.getPredict(imageBitmap)
    Log.d("ObjectDetectionModel", prediction.toString())
}

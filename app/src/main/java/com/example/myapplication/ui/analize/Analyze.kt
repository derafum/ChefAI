package com.example.myapplication.ui.analize

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.DatabaseHelper
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentAnalizeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class Analyze : Fragment() {
    private lateinit var viewModel: AnalizeViewModel
    private lateinit var binding: FragmentAnalizeBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var capturedImageView: ImageView

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Используйте переданные параметры container и inflater для создания представления фрагмента
        binding = FragmentAnalizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[AnalizeViewModel::class.java]
        cameraExecutor = Executors.newSingleThreadExecutor()
        // recyclerView = view.findViewById(R.id.reView)

        capturedImageView = view.findViewById(R.id.capturedImageView)


        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        binding.imageCaptureButton.setOnClickListener { takePhoto() }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: return
                    val msg = "Saved: $savedUri"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

                    Log.d(TAG, msg)

                    displayCapturedPhoto(savedUri)

                    analysis(savedUri)

                }
            }
        )
    }

    private fun displayCapturedPhoto(imageUri: Uri) {
        val inputStream = requireContext().contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        capturedImageView.setImageBitmap(bitmap)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    requireContext(),
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startCamera()
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    fun analysis(imageUri: Uri) {
        runBlocking {
            val jsonObject = recognizeQrCode(imageUri)
            if (jsonObject != null) {
                Log.d(TAG, "namesArray $jsonObject")
            } else {
                Log.d(TAG, "namesArray null")

            }

            check_products(imageUri)
         //   handleAsJustPhoto()

        }
    }

    private suspend fun recognizeQrCode(imageUri: Uri): JSONObject? = withContext(Dispatchers.IO) {
        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(imageUri)
        val imageBytes = inputStream?.readBytes()

        val qrFile = File(requireContext().cacheDir, "qrCode.jpg")
        qrFile.writeBytes(imageBytes ?: return@withContext null)

        val token = "20269.DDqUwXE3jHFbumFYw"
        val url = "https://proverkacheka.com/api/v1/check/get"

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("token", token)
            .addFormDataPart(
                "qrfile",
                qrFile.name,
                qrFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        val client = OkHttpClient()

        var jsonObject: JSONObject? = null

        val response = client.newCall(request).execute()
        val jsonResponse = response.body?.string()
        jsonObject = jsonResponse?.let { JSONObject(it) }

        when (jsonObject?.optInt("code", -1)) {
            1 -> {
                // Обработка кода 1
                // ...
                Log.d(TAG, "успешное сканирование чека: $jsonObject")
            }

            else -> {
                // Обработка неизвестного кода
                Log.d(TAG, "неуспешное сканирование чека")
            }
        }
        Log.d(TAG, "qr: $jsonObject")

        return@withContext jsonObject
    }


    fun extractNamesFromJSONObject(jsonString: JSONObject?): Array<String> {

        val itemsArray =
            jsonString?.getJSONObject("data")?.getJSONObject("json")?.getJSONArray("items")

        val namesList = mutableListOf<String>()

        if (itemsArray != null) {
            for (i in 0 until itemsArray.length()) {
                val itemObject = itemsArray.getJSONObject(i)
                val name = itemObject.getString("name")
                namesList.add(name)
            }
        }

        return namesList.toTypedArray()
    }

    private suspend fun handleAsJustPhoto(imageUri: Uri): JSONObject? =
        withContext(Dispatchers.IO) {

            val token = "hzA1SfCPcpXoK4L5LAKe"
            val apiUrl = "https://detect.roboflow.com/-object-detection-pukbl/3?api_key=$token"

            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            val imageBytes = inputStream?.readBytes()
            val encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)

            val requestBody = encodedImage.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(apiUrl)
                .post(requestBody)
                .build()
            val client = OkHttpClient()

            val response = client.newCall(request).execute()
            val jsonResponse = response.body?.string()
            var jsonObject: JSONObject?
            jsonObject = jsonResponse?.let { JSONObject(it) }


            Log.d(TAG, "успешное сканирование продукта: $jsonObject")

            return@withContext jsonObject

        }

    @SuppressLint("Recycle")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun check_products(imageUri: Uri) {
        val apiUrl =
            "https://detect.roboflow.com/-object-detection-pukbl/3?api_key=hzA1SfCPcpXoK4L5LAKe"

        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(imageUri)
        val imageBytes = inputStream?.readBytes()

        val encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)


        val requestBody = encodedImage.toRequestBody("application/json".toMediaType())

        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Обработка ошибок при выполнении запроса
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                Log.d(TAG, "Запрос выполнен успешно responseBody. Ответ сервера: $responseBody")
                // Обработка ответа от сервера
                // responseBody содержит ответ от сервера в виде строки
                activity?.runOnUiThread {
                    // Найти TextView по его ID
                    val responseTextView =
                        requireView().findViewById<TextView>(R.id.responseTextView)
                    // Установить значение responseBody в текстовое поле
                    val predict_product = responseBody?.let { parseClasses(it) }

                    val dbHelper = DatabaseHelper(requireActivity())

                    if (predict_product != null) {
                        for (number in predict_product) {
                            val recipeNumbers = dbHelper.getRecipeNumbersByIngredients(number)
                            Log.d(TAG, "answer: $recipeNumbers")
                            responseTextView.text =
                                "Response Body: $predict_product, $recipeNumbers"
                        }
                    }
                }
            }
        }


        val client = OkHttpClient()

        val request = Request.Builder()
            .url(apiUrl)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(callback)


    }

    private fun parseClasses(jsonText: String): MutableList<String> {
        val classes = mutableListOf<String>()

        try {
            val json = JSONObject(jsonText)
            val predictionsArray = json.getJSONArray("predictions")

            val translations = mapOf(
                "milk" to "молоко",
                "peach" to "персик",
                "orange" to "апельсин",
                "carrot" to "морковь",
                "mandarin" to "мандарин",
                "tomato" to "помидор",
                "bell pepper" to "перец",
                "eggplant" to "баклажан",
                "potato" to "картофель",
                "chili" to "перец чили",
                "peppers" to "перец",
                "egg" to "яйцо",
                "apple" to "яблоко",
                "garlic" to "чеснок",
                "cucumber" to "огурец",
                "lemon" to "лимон",
                "bulb onion" to "репчатый лук",
                "banana" to "банан",
                "pear" to "груша",
                "zucchini" to "цукини",
                "cabbage" to "капуста",
                "strawberry" to "клубника",
                "chicken meat" to "куриц",
                "pork" to "свинина",
                "cherry" to "вишня",
                "grape" to "виноград",
                "green onion" to "зеленый лук",
                "mushrooms" to "грибы",
                "beef" to "говядина"

            )
            val uniqueClasses = mutableSetOf<String>()

            for (i in 0 until predictionsArray.length()) {
                val prediction = predictionsArray.getJSONObject(i)
                val className = prediction.getString("class")
                val translatedClassName = translations[className]
                translatedClassName?.let {
                    if (!uniqueClasses.contains(it)) {
                        uniqueClasses.add(it)
                        classes.add(it)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


        return classes
    }

}



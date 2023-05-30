package com.example.myapplication.ui.analize

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentAnalizeBinding
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

import android.util.Base64
import android.widget.TextView
import com.example.myapplication.DatabaseHelper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import org.json.JSONObject


class Analize : Fragment() {
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
        binding = FragmentAnalizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[AnalizeViewModel::class.java]
        cameraExecutor = Executors.newSingleThreadExecutor()

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
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()

                    Log.d(TAG, msg)

                    displayCapturedPhoto(savedUri)
                    scanQRCode(savedUri)
                    analysis(savedUri, requireContext().contentResolver)
                }
            }
        )
    }

    private fun displayCapturedPhoto(imageUri: Uri) {
        val inputStream = requireContext().contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        capturedImageView.setImageBitmap(bitmap)
    }

    private fun scanQRCode(imageUri: Uri) {
        val image = InputImage.fromFilePath(requireContext(), imageUri)

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val scanner = BarcodeScanning.getClient(options)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    val rawValue = barcode.rawValue
                    val valueType = barcode.valueType
                    Log.d(TAG, "QR Code Value: $rawValue, Value Type: $valueType")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "QR Code scanning failed: ${e.message}", e)
            }
    }

    fun uriToBitmap(uri: Uri): Bitmap {
        var inputStream: InputStream? = null
        return try {
            inputStream = requireContext().contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        } finally {
            inputStream?.close()
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        return outputStream.toByteArray()
    }


    /*
          private fun analysis(imageUri: Uri) {
              try {
                  Log.d(TAG, "imageUri00: $imageUri")
                  if (!Python.isStarted()) {
                      Python.start(AndroidPlatform(requireActivity()))
                  }
                  Log.d(TAG, "imageUri0: $imageUri")
                  val python = Python.getInstance()
                  Log.d(TAG, "imageUri1: $imageUri")
                  val module = python.getModule("analysis_img")
                  Log.d(TAG, "imageUri2: $imageUri")
                  val predict = module.callAttr("main", imageUri)
                  Log.d(TAG, "predict: $predict")
              } catch (e: Exception) {
                  Log.e(TAG, "Python script execution failed: ${e.message}", e)
              }
          }
      */



    fun performPostRequest(url: String, requestBody: RequestBody, callback: Callback) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(callback)
    }

    @SuppressLint("Recycle")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun analysis(imageUri: Uri, contentResolver: ContentResolver) {
        val apiUrl = "https://detect.roboflow.com/-object-detection-pukbl/3?api_key=hzA1SfCPcpXoK4L5LAKe"

        val inputStream = contentResolver.openInputStream(imageUri)
        val imageBytes = inputStream?.readBytes()

        val encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)

        val client = OkHttpClient()
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
                    val responseTextView = requireView().findViewById<TextView>(R.id.responseTextView)
                    // Установить значение responseBody в текстовое поле
                    val predict_product = responseBody?.let { parseClasses(it) }

                    val dbHelper = DatabaseHelper(requireActivity())

                    if (predict_product != null) {
                        for (number in predict_product) {
                            val recipeNumbers = dbHelper.getRecipeNumbersByIngredients(number)
                            Log.d(TAG, "answer: $recipeNumbers")
                            responseTextView.text = "Response Body: $predict_product, $recipeNumbers"
                        }
                    }
                }
            }
        }



        performPostRequest(apiUrl, requestBody, callback)


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

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
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
}

package com.example.myapplication.ui.analize

import CheckQR
import FoodAdapter
import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.myapplication.DatabaseHelper
import okhttp3.Call
import okhttp3.Callback
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

import java.io.IOException
import org.json.JSONObject

import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.myapplication.Food
import com.example.myapplication.Recipe
import com.example.myapplication.RecipeAdapter
import com.example.myapplication.databinding.FragmentHomeBinding

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Analize : Fragment() {
    private lateinit var viewModel: AnalizeViewModel
    private lateinit var binding: FragmentAnalizeBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var capturedImageView: ImageView


    private lateinit var recyclerView: RecyclerView
    private lateinit var foodList: ArrayList<Food>
    private lateinit var foodAdapter: FoodAdapter




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
    ): View? {
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



        val dbHelper = DatabaseHelper(requireActivity())
        var offset = 0







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
                  //  var scannedNames = mutableListOf<String>()
                    //scannedNames = scanQRCode(savedUri)
                //    Log.d(TAG, "QR scannedNames: $scannedNames")
                    analysis_check(savedUri, requireContext().contentResolver) { product_names ->
                        // Здесь ты можешь использовать полученные product_names
                        Log.d(TAG, "Product names: $product_names")
                    }
                   // analysis(savedUri, requireContext().contentResolver)
                }
            }
        )
    }

    private fun displayCapturedPhoto(imageUri: Uri) {
        val inputStream = requireContext().contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        capturedImageView.setImageBitmap(bitmap)
    }

    private fun scanQRCode(imageUri: Uri): MutableList<String> {
        val image = InputImage.fromFilePath(requireContext(), imageUri)
        val names = mutableListOf<String>()

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

                    val token = "20253.Umk1V1xWs9M87DoWY"
                    val url = "https://proverkacheka.com/api/v1/check/get"
                    val qrraw = "20230531T1757&s=559.89&fn=9960440302156794&i=73600&fp=385665697&n=1"

                    performPostRequest(token, url, qrraw) { responseBody ->
                        // Обработка ответа от сервера
                        if (responseBody != null) {
                            Log.d(TAG, "Response: $responseBody")


                                val jsonObject = JSONObject(responseBody)
                                val jsonData = jsonObject.optJSONObject("data")
                                val jsonItems = jsonData?.optJSONObject("json")?.optJSONArray("items")

                                if (jsonItems != null) {
                                    for (i in 0 until jsonItems.length()) {
                                        val item = jsonItems.optJSONObject(i)
                                        val name = item?.optString("name")?.toLowerCase()
                                        if (!name.isNullOrEmpty()) {
                                            names.add(name)
                                            Log.d(TAG, "Product name: $name")
                                        }
                                    }
                                }

                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "QR Code scanning failed: ${e.message}", e)
            }

        return names
    }




    fun extractNames(jsonString: String): List<String> {
        val names = mutableListOf<String>()

        val jsonObject = JSONObject(jsonString)
        val dataObject = jsonObject.getJSONObject("data")
        val itemsArray = dataObject.getJSONArray("items")

        for (i in 0 until itemsArray.length()) {
            val item = itemsArray.getJSONObject(i)
            val name = item.getString("name")
            names.add(name)
        }

        return names
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






    fun performPostRequest(url: String, requestBody: RequestBody, callback: Callback) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(callback)
    }


    private fun performPostRequest_func(responseBody: String?): MutableList<String> {
        val product_names = mutableListOf<String>()
        if (responseBody != null) {
            Log.d(TAG, "Response: $responseBody")


            val jsonObject = JSONObject(responseBody)
            val jsonData = jsonObject.optJSONObject("data")
            val jsonItems = jsonData?.optJSONObject("json")?.optJSONArray("items")
            if (jsonItems != null) {
                for (i in 0 until jsonItems.length()) {
                    val item = jsonItems.optJSONObject(i)
                    val name = item?.optString("name")?.toLowerCase()
                    if (!name.isNullOrEmpty()) {

                        product_names.add(name)
                        Log.d(TAG, "Product name: $name")
                    }
                }
            }

        }
        return product_names
    }
    @SuppressLint("Recycle")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun analysis_check(
        imageUri: Uri,
        contentResolver: ContentResolver,
        callback: (List<String>) -> Unit
    ) {
        val image = InputImage.fromFilePath(requireContext(), imageUri)
        val product_names = mutableListOf<String>()

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        val scanner = BarcodeScanning.getClient(options)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                // Вызываем колбэк после завершения сканирования
                processBarcodes(barcodes, product_names) {
                    // Вызываем колбэк после завершения обработки QR-кодов
                    callback(product_names)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "QR Code scanning failed: ${e.message}", e)
                // Вызываем колбэк в случае ошибки
                callback(emptyList())
            }
    }

    private fun processBarcodes(
        barcodes: List<Barcode>,
        product_names: MutableList<String>,
        callback: () -> Unit
    ) {
        for (barcode in barcodes) {
            val rawValue = barcode.rawValue
            val valueType = barcode.valueType
            Log.d(TAG, "QR Code Value: $rawValue, Value Type: $valueType")

            val token = "20253.Umk1V1xWs9M87DoWY"
            val url = "https://proverkacheka.com/api/v1/check/get"
            val qrraw = "20230531T1757&s=559.89&fn=9960440302156794&i=73600&fp=385665697&n=1"

            performPostRequest(token, url, qrraw) { responseBody ->
                // Обработка ответа от сервера
                if (responseBody != null) {
                    Log.d(TAG, "Response: $responseBody")

                    val jsonObject = JSONObject(responseBody)
                    val jsonData = jsonObject.optJSONObject("data")
                    val jsonItems = jsonData?.optJSONObject("json")?.optJSONArray("items")
                    if (jsonItems != null) {
                        for (i in 0 until jsonItems.length()) {
                            val item = jsonItems.optJSONObject(i)
                            val name = item?.optString("name")?.toLowerCase()
                            if (!name.isNullOrEmpty()) {
                                product_names.add(name)
                                Log.d(TAG, "Product name: $name")
                            }
                        }
                    }
                }
            }
        }
        // Вызываем колбэк после обработки всех QR-кодов
        callback()
    }



    @SuppressLint("Recycle")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun analysis(imageUri: Uri, contentResolver: ContentResolver) {



        var flag = false


        val image = InputImage.fromFilePath(requireContext(), imageUri)
        val product_names = mutableListOf<String>()

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

                    val token = "20253.Umk1V1xWs9M87DoWY"
                    val url = "https://proverkacheka.com/api/v1/check/get"
                    val qrraw = "20230531T1757&s=559.89&fn=9960440302156794&i=73600&fp=385665697&n=1"

                    performPostRequest(token, url, qrraw) { responseBody ->
                        // Обработка ответа от сервера
                        if (responseBody != null) {
                            Log.d(TAG, "Response: $responseBody")


                            val jsonObject = JSONObject(responseBody)
                            val jsonData = jsonObject.optJSONObject("data")
                            val jsonItems = jsonData?.optJSONObject("json")?.optJSONArray("items")
                            flag = true
                            if (jsonItems != null) {
                                for (i in 0 until jsonItems.length()) {
                                    val item = jsonItems.optJSONObject(i)
                                    val name = item?.optString("name")?.toLowerCase()
                                    if (!name.isNullOrEmpty()) {

                                        product_names.add(name)
                                        Log.d(TAG, "Product name: $name")
                                    }
                                }
                            }

                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "QR Code scanning failed: ${e.message}", e)
            }









        if (!flag) {

            val apiUrl =
                "https://detect.roboflow.com/-object-detection-pukbl/3?api_key=hzA1SfCPcpXoK4L5LAKe"

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

                @SuppressLint("SetTextI18n", "SuspiciousIndentation")
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
                                    "Response Body: $predict_product"
                                binding.reView.layoutManager = LinearLayoutManager(context)
                                val adapter = RecipeAdapter()
                                binding.reView.adapter = adapter


                                    GlobalScope.launch(Dispatchers.Main) {
                                        for (product in predict_product){
                                        val number = withContext(Dispatchers.IO) { dbHelper.getRecipeNumbersByIngredients(product) }
                                        Log.d("CameraXApp", "Name, $number")
                                        val recipes = withContext(Dispatchers.IO) { dbHelper.getRecipesByNumber(number.elementAt(0)) }

                                        for (recipe in recipes) {
                                            val name = recipe.name
                                            val img = recipe.img
                                            val time = recipe.time
                                            val recipeItem = Recipe(img, name, "Time: $time")
                                            adapter.addRecipe(recipeItem)
                                            Log.d("CameraXApp", "Name, $name, img, $img, time, $time")
                                        }

                                        withContext(Dispatchers.IO) {


                                        }
                                }




                                }


                            }
                        }
                    }
                }
            }



            performPostRequest(apiUrl, requestBody, callback)

        }
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

    fun performPostRequest(token: String, url: String, qrraw: String, callback: (String?) -> Unit) {
        val client = OkHttpClient()

        val requestBody = FormBody.Builder()
            .add("token", token)
            .add("qrraw", qrraw)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Обработка ошибок при выполнении запроса
                e.printStackTrace()
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                callback(responseBody)
            }
        })
    }



}



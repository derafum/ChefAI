package com.example.myapplication.ui.analize

import DatabaseHelper
import RecipeAdapter
import android.Manifest
import android.annotation.SuppressLint
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.Recipe

import com.example.myapplication.databinding.FragmentAnalizeBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
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
                    // Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    val msgForUser = "Фото сохранено успешно"
                    Toast.makeText(requireContext(), msgForUser, Toast.LENGTH_SHORT).show()
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

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    fun analysis(imageUri: Uri) {
        runBlocking {
            val responseTextView = requireView().findViewById<TextView>(R.id.responseTextView)
            val jsonObject = recognizeQrCode(imageUri)
            if (jsonObject != null) {
                Log.d(TAG, "namesArray... $jsonObject")

                val names = getNamesFromJSONObject(jsonObject)
                val matchingProducts = findMatchingProducts(names)

                responseTextView.text = "Я вижу здесь: чек =). Продукты: ${matchingProducts.joinToString(", ")}"
                val dbHelper = DatabaseHelper(requireActivity())
                for (product in matchingProducts){

                    val recipeNumbers = dbHelper.getRecipeNumbersByIngredients(product)

                    binding.reView.layoutManager = LinearLayoutManager(context)
                    val adapter = RecipeAdapter()
                    binding.reView.adapter = adapter

                    for (number in recipeNumbers) {
                        GlobalScope.launch(Dispatchers.Main) {
                            val recipes = withContext(Dispatchers.IO) {
                                dbHelper.getRecipeDataByNumber(number)
                            }


                            val name = recipes!!.name
                            val img = recipes.img
                            val time = recipes.time
                            val recipeItem = Recipe(img, name, "Time: $time")
                            adapter.addRecipe(recipeItem)
                            Log.d("CameraXApp", "Name, $name, img, $img, time, $time")

                        }
                    }
                }
            } else {
                Log.d(TAG, "namesArray null")
                checkProducts(imageUri)
            }
            //   handleAsJustPhoto()

        }
    }

    @SuppressLint("Recycle")
    private suspend fun recognizeQrCode(imageUri: Uri): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? =
                requireContext().contentResolver.openInputStream(imageUri)
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

            val jsonObject: JSONObject?

            val response = client.newCall(request).execute()
            val jsonResponse = response.body?.string()
            jsonObject = jsonResponse?.let { JSONObject(it) }

            when (jsonObject?.optInt("code", -1)) {
                1 -> {
                    // Обработка кода 1
                    // ...
                    Log.d(TAG, "успешное сканирование чека")
                }
                else -> {
                    // Обработка неизвестного кода
                    Log.d(TAG, "неуспешное сканирование чека")
                    return@withContext null
                }
            }
            Log.d(TAG, "qr: $jsonObject")

            return@withContext jsonObject
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка при распознавании QR-кода: ${e.message}", e)
            return@withContext null
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("Recycle")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun checkProducts(imageUri: Uri) {
        val responseTextView =
            requireView().findViewById<TextView>(R.id.responseTextView)
        responseTextView.text = "Я вижу здесь: ожидайте "
        val apiUrl =
            "https://detect.roboflow.com/-object-detection-pukbl/3?api_key=hzA1SfCPcpXoK4L5LAKe"

        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(imageUri)
        val imageBytes = inputStream?.readBytes()

        val encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT)


        val requestBody = encodedImage.toRequestBody("application/json".toMediaType())

        val callback = object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Обработка ошибок при выполнении запроса
                responseTextView.text = "Ошибка при отправке запроса"
                e.printStackTrace()
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                Log.d(TAG, "Запрос выполнен успешно responseBody. Ответ сервера: $responseBody")

                // Обработка ответа от сервера
                // responseBody содержит ответ от сервера в виде строки
                activity?.runOnUiThread {
                    // Найти TextView по его ID

                    // Установить значение responseBody в текстовое поле
                    val predict_product = responseBody?.let { parseClasses(it) }

                    val dbHelper = DatabaseHelper(requireActivity())

                    if (!predict_product.isNullOrEmpty()) {
                        for (product in predict_product) {
                            val recipeNumbers = dbHelper.getRecipeNumbersByIngredients(product)
                            Log.d(TAG, "answer: $recipeNumbers")
                            val result = predict_product.joinToString(", ")
                            responseTextView.text =
                                "Я вижу здесь: $result"



                            binding.reView.layoutManager = LinearLayoutManager(context)
                            val adapter = RecipeAdapter()
                            binding.reView.adapter = adapter

                            for (number in recipeNumbers) {
                                GlobalScope.launch(Dispatchers.Main) {
                                    val recipes = withContext(Dispatchers.IO) {
                                        dbHelper.getRecipeDataByNumber(number)
                                    }


                                    val name = recipes!!.name
                                    val img = recipes.img
                                    val time = recipes.time
                                    val recipeItem = Recipe(img, name, "Time: $time")
                                    adapter.addRecipe(recipeItem)
                                    Log.d("CameraXApp", "Name, $name, img, $img, time, $time")

                                }
                            }
                        }
                    } else {
                        responseTextView.text = "Ничего не удалось найти"
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


    fun getNamesFromJSONObject(jsonObject: JSONObject?): String {
        val names = mutableListOf<String>()
        jsonObject?.optJSONObject("data")?.optJSONObject("json")?.optJSONArray("items")?.let { items ->
            for (i in 0 until items.length()) {
                val item = items.optJSONObject(i)
                val name = item?.optString("name")
                name?.let { names.add(it) }
            }
        }
        return names.joinToString()
    }


    fun findMatchingProducts(names: String): Array<String> {
        val products = arrayOf(
            "томат", "огурц", "помидор", "молоко", "персик", "апельсин", "морковь", "мандарин", "перец",
            "баклажан", "картофель", "картошк", "перец", "яйц", "яблок", "чеснок", "лимон", "репчатый лук",
            "банан", "груш", "цукин", "капуст", "клубника", "куриц", "курин", "свинин", "вишн", "виноград",
            "лук", "гриб", "говядин"
        )

        val foundProducts = mutableListOf<String>()

        for (product in products) {
            if (isProductMatch(names, product)) {
                foundProducts.add(product)
            }
        }

        return foundProducts.toTypedArray()
    }

    fun isProductMatch(names: String, product: String): Boolean {
        val words = names.split("\\s+".toRegex())
        for (word in words) {
            val normalizedWord = word.toLowerCase().replace("[^а-яa-z]".toRegex(), "")
            val normalizedProduct = product.toLowerCase().replace("[^а-яa-z]".toRegex(), "")
            if (normalizedWord == normalizedProduct) {
                return true
            }
            if (levenshteinDistance(normalizedWord, normalizedProduct) <= 2) {
                return true
            }
        }
        return false
    }

    fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        val d = Array(m + 1) { IntArray(n + 1) }

        for (i in 0..m) {
            d[i][0] = i
        }

        for (j in 0..n) {
            d[0][j] = j
        }

        for (j in 1..n) {
            for (i in 1..m) {
                val substitutionCost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                d[i][j] = minOf(
                    d[i - 1][j] + 1,
                    d[i][j - 1] + 1,
                    d[i - 1][j - 1] + substitutionCost
                )
            }
        }

        return d[m][n]
    }
}



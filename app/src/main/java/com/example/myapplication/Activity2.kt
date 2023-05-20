package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.example.myapplication.ui.home.Home
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random

class Activity2 : AppCompatActivity() {
    private var lastClickTime: Long = 0
    private var isButtonSelected = false // Flag to track button selection
    private var checkClick = false
    private var count = 0
    private var count_need = 0




    val numbers = arrayOf(17094, 16311, 14043, 26160, 10172, 25646, 7438, 28691, 29637, 4694, 14155, 8247, 12383, 799, 304, 22393, 28175, 27144, 28077, 4013, 25912, 12860, 29054, 12750, 25957, 10840, 3242, 27811, 874, 29450, 12884, 24518, 7586, 22579, 28491, 20364, 28214, 24002, 17142, 20162)

    fun getRandomNumbers(numbers: Array<Int>): List<Int> {
        val randomNumbers = mutableListOf<Int>()
        val random = Random.Default

        // Выбираем 5 случайных чисел из массива
        for (i in 0 until 5) {
            val randomNumber = numbers[random.nextInt(numbers.size)]
            randomNumbers.add(randomNumber)
        }

        return randomNumbers
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // массив с выбранными рецептами, которые передаем рек системе
        val count_recommend = mutableListOf<Int>()

        getSupportActionBar()?.hide()

        val dbHelper = DatabaseHelper(this@Activity2)


        val randomNumbers = getRandomNumbers(numbers)


        val recipeMap = mutableMapOf<Int, Pair<String, String>>()

        val imageList: MutableList<SlideModel> = ArrayList() // Create image list

        for (i in randomNumbers){
            val result = dbHelper.getRecipeUrlAndImgByNumber(i)
            if (result != null) {
                val (title, img) = result
                imageList.add(SlideModel(img, title, ScaleTypes.FIT))

            }


        }

        val result = dbHelper.getRecipeUrlAndImgByNumber(42)
        if (result != null) {
            val (recipeUrl, recipeImg) = result
            Log.d("Recipes", "URL: $recipeUrl, IMG: $recipeImg")
        } else {
            Log.d("Recipes", "No recipe found with number 42")
        }

        dbHelper.close()

        setContentView(R.layout.activity_2)


        val imageSlider = findViewById<ImageSlider>(R.id.slider)
        imageSlider.setImageList(imageList)


        count = imageList.size


        val length = count_recommend.size
        Log.d("MyLogMAct", "Length count_recommend $count_recommend")


        val textView = findViewById<TextView>(R.id.textsize)
        textView.text = "Count $count_need / $count, Длина $length"


        val str = "test"
        val fragment = Home()
        val bundle = Bundle()
        bundle.putIntegerArrayList("countRecommendList", ArrayList(count_recommend))
        fragment.arguments = bundle


        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val stre = "test"
        editor.putString("count_recommend", stre)
        editor.apply()



        val textViewString = textView.text.toString()

        val selectedImages: MutableList<Int> = mutableListOf()

        imageSlider.setItemClickListener(object : ItemClickListener {

            override fun onItemSelected(position: Int) {
                val currentTime = System.currentTimeMillis()
                if (selectedImages.contains(position)) {
                    count_recommend.remove(randomNumbers[position])
                    Log.d("MyLogPosition", "Remove $position")
                    selectedImages.remove(position)

                } else {
                    count_recommend.add(randomNumbers[position])
                    Log.d("MyLogPosition", "Add $position")
                    selectedImages.add(position)

                }
                val selectedCount = selectedImages.size
                textView.text = "Count $selectedCount / $count"
                Log.d("MyLogMAct", "Count $selectedCount /1 $count, $count_recommend")


            }
        })

    }
    fun goToAnActivity(view: View?) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
fun runPythonScript(number: Int) {
    val processBuilder = ProcessBuilder("D:\\Python\\python.exe", "Business logic\\Rec_system\\Rec_system_new.py", number.toString())
    val process = processBuilder.start()

    val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
    val output = StringBuilder()

    var line: String?
    while (bufferedReader.readLine().also { line = it } != null) {
        output.append(line)
    }

    val result = output.toString()

    // Делайте что-то с полученным результатом из скрипта Python (например, обработка или отображение)
}

fun runPythonScript2(): Int {
    val processBuilder = ProcessBuilder("D:\\Python\\python.exe", "Business logic\\Rec_system\\Rec_system_new.py")
    val process = processBuilder.start()

    val bufferedReader = BufferedReader(InputStreamReader(process.inputStream))
    val output = bufferedReader.readLine()

    return output?.toIntOrNull() ?: 0
}




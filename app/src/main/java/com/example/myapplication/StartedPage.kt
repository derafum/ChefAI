package com.example.myapplication


import DatabaseHelper
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.example.myapplication.databinding.ActivitySplashBinding
import kotlin.random.Random


class StartedPage : AppCompatActivity() {
    private var count = 0
    private var count_need = 0



    private val numbers = arrayOf(
        17094,
        16311,
        14043,
        26160,
        10172,
        25646,
        7438,
        28691,
        29637,
        4694,
        14155,
        8247,
        12383,
        799,
        304,
        22393,
        28175,
        27144,
        28077,
        4013,
        25912,
        12860,
        29054,
        12750,
        25957,
        10840,
        3242,
        27811,
        874,
        29450,
        12884,
        24518,
        7586,
        22579,
        28491,
        20364,
        28214,
        24002,
        17142,
        20162
    )

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


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // массив с выбранными рецептами, которые передаем рек системе
        val count_recommend = mutableListOf<Int>()

        getSupportActionBar()?.hide()

        val dbHelper = DatabaseHelper(this@StartedPage)


        val randomNumbers = getRandomNumbers(numbers)


        val imageList: MutableList<SlideModel> = ArrayList() // Create image list

        for (i in randomNumbers) {
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

        setContentView(R.layout.started_page)


        val imageSlider = findViewById<ImageSlider>(R.id.slider)
        imageSlider.setImageList(imageList)


        count = imageList.size


        val length = count_recommend.size
        Log.d("MyLogMAct", "Length count_recommend $count_recommend")


        val textView = findViewById<TextView>(R.id.textsize)
        textView.text = "Count $count_need / $count"


        val sharedPreferences = getSharedPreferences("len_count_recommend", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("count_recommend", length.toString())
        editor.apply()

        val selectedImages: MutableList<Int> = mutableListOf()

        imageSlider.setItemClickListener(object : ItemClickListener {

            override fun onItemSelected(position: Int) {
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


                val sharedPreferences2 = getSharedPreferences("length", Context.MODE_PRIVATE)
                val editor2 = sharedPreferences2.edit()
                editor2.putInt("len", selectedCount)
                editor2.apply()


                for (i in 0 until selectedCount) {
                    // Действия, которые нужно выполнить с каждым элементом массива

                    val sharedPreferences_elements =
                        getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    val editors = sharedPreferences_elements.edit()
                    editors.putInt("count_recommend$i", count_recommend[i])
                    editors.apply()
                }
            }
        })
    }

    fun goToAnActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}

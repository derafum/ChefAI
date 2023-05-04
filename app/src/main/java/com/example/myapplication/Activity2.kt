package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel


class Activity2 : AppCompatActivity() {
    private var lastClickTime: Long = 0
    private var isButtonSelected = false // Flag to track button selection
    private var checkClick = false
    private var count = 0
    private var count_need = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2)

        val imageList: MutableList<SlideModel> = ArrayList() // Create image list
        imageList.add(SlideModel("https://eda.ru/img/eda/c180x180/s1.eda.ru/StaticContent/Photos/120131082957/160116111633/p_O.jpg", "Итальянская курица по-охотничьи", ScaleTypes.FIT))
        imageList.add(SlideModel("https://eda.ru/img/eda/c180x180/s1.eda.ru/StaticContent/Photos/160321003649/210623123415/p_O.jpeg", "Спагетти с трюфельным соусом", ScaleTypes.FIT))
        imageList.add(SlideModel("https://eda.ru/img/eda/c180x180/s1.eda.ru/StaticContent/Photos/120213175727/120213175945/p_O.jpg", "Картофельные лепешки", ScaleTypes.FIT))
        imageList.add(SlideModel("https://eda.ru/img/eda/c180x180/s1.eda.ru/StaticContent/Photos/120131082151/120620211337/p_O.jpg", "Бараньи котлетки с зеленым чесноком на гриле", ScaleTypes.FIT))
        imageList.add(SlideModel("https://eda.ru/img/eda/c180x180/s1.eda.ru/StaticContent/Photos/120131083458/130806175411/p_O.jpg", "Рагу с баклажанами а-ля итальяно", ScaleTypes.FIT))
        imageList.add(SlideModel("https://eda.ru/img/eda/c180x180/s1.eda.ru/StaticContent/Photos/120213181923/120213182254/p_O.jpg", "Говядина в соусе из красной фасоли", ScaleTypes.FIT))

        val imageSlider = findViewById<ImageSlider>(R.id.slider)
        imageSlider.setImageList(imageList)


        count = imageList.size

        val textView = findViewById<TextView>(R.id.textsize)
        textView.text = "Count $count_need / $count"


        Log.d("MyLogMAct", "Count $count_need / $count")

        val textViewString = textView.text.toString()


        imageSlider.setItemClickListener(object : ItemClickListener {
            override fun onItemSelected(position: Int) {
                val currentTime = System.currentTimeMillis()
                if ((currentTime - lastClickTime < 500) and (checkClick)) {
                    Log.d("MyLogMAct", "Image $position not selected")
                    Toast.makeText(this@Activity2, "Image $position not selected", Toast.LENGTH_SHORT).show()
                    checkClick = true
                    isButtonSelected = false
                    count_need -= 2
                    if (count_need <=0) {
                        count_need = 0
                    }
                }
                else if (lastClickTime > 0) {
                    Log.d("MyLogMAct", "Image $position selected")
                    Toast.makeText(this@Activity2, "Image $position selected", Toast.LENGTH_SHORT).show()
                    checkClick = true
                    isButtonSelected = true
                    count_need += 1
                    if (count_need >=count) {
                        count_need = count
                    }
                }
// Set text

                textView.text = "Count $count_need / $count"
// Get text
                lastClickTime = currentTime

            }

        })


    }
    fun goToAnActivity(view: View?) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}

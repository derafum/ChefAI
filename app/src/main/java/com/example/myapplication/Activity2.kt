package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.denzcoskun.imageslider.ImageSlider
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel


class Activity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_2)

        //        ImageSlider imageSlider = findViewById(R.id.slider);
        //        List<SlideModel> slideModels = new ArrayList<>();
        //        slideModels.add(new SlideModel(R.drawable.spider, ScaleTypes.FIT));
        //        slideModels.add(new SlideModel(R.drawable.jack, ScaleTypes.FIT));
        //        slideModels.add(new SlideModel(R.drawable.ozark, ScaleTypes.FIT));
        //        slideModels.add(new SlideModel(R.drawable.retro, ScaleTypes.FIT));
        val imageList: MutableList<SlideModel> = ArrayList() // Create image list

        // imageList.add(SlideModel("String Url" or R.drawable)
        // imageList.add(SlideModel("String Url" or R.drawable, "title") You can add title
        imageList.add(SlideModel("https://bit.ly/2YoJ77H", "Title 1", ScaleTypes.FIT))
        imageList.add(SlideModel("https://bit.ly/2BteuF2", "Title 2", ScaleTypes.FIT))
        imageList.add(SlideModel("https://bit.ly/3fLJf72", "Title 3", ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.porshe, "Title 4", ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.porshe2, "Title 5", ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.jaguar, "Title 6", ScaleTypes.FIT))
        val imageSlider = findViewById<ImageSlider>(R.id.slider)
        imageSlider.setImageList(imageList)
    }
}


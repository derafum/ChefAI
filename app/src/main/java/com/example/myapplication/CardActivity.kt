package com.example.myapplication


import DatabaseHelper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.myapplication.Food
import com.google.gson.Gson

class CardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val foodItemJson = intent.getStringExtra("foodItemJson")
        val gson = Gson()
        val foodItem = gson.fromJson(foodItemJson, Food::class.java)
        val title = foodItem.foodName
        val img = foodItem.foodImage
        val dbHelper = DatabaseHelper(this@CardActivity)
        val recipeDataList = dbHelper.getRecipeDataByImg(img)

        setContent {
            Column(modifier = Modifier.fillMaxSize()) {
                for (recipeData in recipeDataList) {
                    RecipeItem(recipeData)
                }
            }
        }
    }

    @Composable
    fun RecipeItem(recipeData: DatabaseHelper.RecipeData) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(recipeData.name)
            Text(recipeData.time)

            // Display image using GlideImage composable
            GlideImage(
                data = recipeData.img,
                contentDescription = null,
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Crop
            )

            // Add other views for displaying recipe details
            Text(recipeData.amountServings)
            Text(recipeData.energy)
            Text(recipeData.ingredients)
            Text(recipeData.instructions)

        }
    }

    @Composable
    fun GlideImage(
        data: Any,
        contentDescription: String?,
        modifier: Modifier = Modifier,
        contentScale: ContentScale = ContentScale.Fit,
        size: Dp? = null
    ) {
        Image(
            painter = rememberImagePainter(data),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            alignment = Alignment.Center,
        )
    }
}

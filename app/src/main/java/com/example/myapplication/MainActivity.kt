package com.example.myapplication


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding


    private var adapter = RecipeAdapter()
    private val imageIdList = listOf(
        R.drawable.recipe1,
        R.drawable.recipe2,
        R.drawable.recipe3,
        R.drawable.recipe4,
        R.drawable.recipe5,
        R.drawable.recipe6,
        R.drawable.recipe6,
        R.drawable.recipe6,
        R.drawable.recipe6,
        R.drawable.recipe6

    )

    private var index = 0


/*
    private fun init2() = with(binding){
        reView.layoutManager = LinearLayoutManager(this@MainActivity)
        reView.adapter = adapter
        for (i in 1..4){
            val recipe = Recipe(imageIdList[i], "Recipe $i", "Time: 10 минут" )
            adapter.addRecipe(recipe)
        }
    }

*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)




        val isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
            .getBoolean("isFirstRun", true)

        if (isFirstRun) {
            //show start activity
            startActivity(Intent(this@MainActivity, Activity2::class.java))
            Toast.makeText(this@MainActivity, "First Run", Toast.LENGTH_LONG)
                .show()
        }


        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
            .putBoolean("isFirstRun", false).commit()


        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bNav.selectedItemId = R.id.item1
        binding.bNav.setOnNavigationItemSelectedListener{
            when(it.itemId){
                R.id.item1 -> {
                    Toast.makeText(this, "Главная", Toast.LENGTH_SHORT).show()
                }
                R.id.item2 -> {
                    Toast.makeText(this, "Поиск", Toast.LENGTH_SHORT).show()
                }
                R.id.item3 -> {
                    Toast.makeText(this, "Анализ", Toast.LENGTH_SHORT).show()
                }
                R.id.item4 -> {
                    Toast.makeText(this, "Избранное", Toast.LENGTH_SHORT).show()
                }
                R.id.item5 -> {
                    Toast.makeText(this, "Профиль", Toast.LENGTH_SHORT).show()
                }
            }
            true
        }

        val dbHelper = DatabaseHelper(this)
        var offset = 0
        var recipes = dbHelper.getTopRecipesByLikes(10, offset)


        binding.reView.layoutManager = LinearLayoutManager(this@MainActivity)
        var adapter = RecipeAdapter()
        binding.reView.adapter = adapter

        var i = 0

        for (recipe in recipes) {
            val name = recipe.name
            val img = recipe.img
            val time = recipe.time
            offset += 10
            val recipe = Recipe(img, name, "Time: $time")
            adapter.addRecipe(recipe)
            i += 1
            Log.d("recipe", "Name, $name, img, $img, time, $time")
        }


        recipes = dbHelper.getTopRecipesByLikes(10, offset)
        for (recipe in recipes) {
            val name = recipe.name
            val img = recipe.img
            val time = recipe.time
            offset += 10
            // Дальнейшая обработка данных рецепта
            Log.d("recipe", "Name2, $name, img2, $img, time2, $time")
        }



        Log.d("MyLogMAct", "OnCreate")
    }

    override fun onStart() {
        super.onStart()
        Log.d("MyLogMAct", "onStart")
    }

    override fun onResume() {
        super.onResume()


        Log.d("MyLogMAct", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("MyLogMAct", "onPause")
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyLogMAct", "onDestroy")
    }


    override fun onStop() {
        super.onStop()
        Log.d("MyLogMAct", "onStop")
    }


    override fun onRestart() {
        super.onRestart()
        Log.d("MyLogMAct", "onRestart")
    }

}

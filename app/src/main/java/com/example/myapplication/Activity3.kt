package com.example.myapplication

import FoodAdapter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Activity3 : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var foodList: ArrayList<Food>
    private lateinit var foodAdapter: FoodAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.started_page)

        init()
    }


    private fun init() {

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        foodList = ArrayList()

        // addDataToList()


        foodAdapter = FoodAdapter(foodList)
        recyclerView.adapter = foodAdapter
    }
    /*
        private fun addDataToList(){

            foodList.add(Food(R.drawable.recipe1, "Paneer Butter"))
            foodList.add(Food(R.drawable.recipe2, "Pizza"))
            foodList.add(Food(R.drawable.recipe3, "Dosa"))
            foodList.add(Food(R.drawable.recipe4, "Veg Biryani"))
            foodList.add(Food(R.drawable.recipe5, "Pasta"))
            foodList.add(Food(R.drawable.recipe6, "Noodles"))
        }*/
}
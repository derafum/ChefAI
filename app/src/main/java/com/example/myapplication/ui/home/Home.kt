package com.example.myapplication.ui.home

import FoodAdapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.myapplication.DatabaseHelper
import com.example.myapplication.Food
import com.example.myapplication.Recipe
import com.example.myapplication.RecipeAdapter
import com.example.myapplication.databinding.FragmentHomeBinding

class Home : Fragment() {

    companion object {
        fun newInstance() = Home()
    }

    private lateinit var viewModel: HomeViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var foodList: ArrayList<Food>
    private lateinit var foodAdapter: FoodAdapter


    private lateinit var binding: FragmentHomeBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)


        val count_recommend = mutableListOf<Int>()
        val dbHelper = DatabaseHelper(requireActivity())
        var offset = 0
        var recipes = dbHelper.getTopRecipesByLikes(10, offset)


        binding.reView.layoutManager = LinearLayoutManager(context)
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


        val sharedPreferences2 = context?.getSharedPreferences("length", Context.MODE_PRIVATE)
        val len_count_recommend = sharedPreferences2?.getInt("len", 0)

        val sharedPreferences = context?.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val countRecommendSet = sharedPreferences?.getString("test", "")

        Log.d("MyLogMAct", "countRecommendLststring, $countRecommendSet")

        for (i in 0 until len_count_recommend!!) {

            val sharedPreferences =
                requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val countRecommendList = sharedPreferences.getInt("count_recommend$i", 0)

            count_recommend.add(countRecommendList)
        }

        Log.d("MyLogMAct", "len_count_recommend, $len_count_recommend")
        Log.d("MyLogMAct", "countRecommendList, $count_recommend")



        recyclerView = binding.RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        foodList = ArrayList()

        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(requireActivity()));
        }

        val python = Python.getInstance()
        val module = python.getModule("Rec_system_new")


// Чтение содержимого скрипта


        for (number in count_recommend) {

            val getNumberFunction = module.callAttr("rec_system", number)
            val rec_receipe = getNumberFunction.asList()

            for (receipeEntry in rec_receipe) {
                val receipe = receipeEntry.toInt()
                val result = dbHelper.getRecipeUrlAndImgByNumber(receipe)
                if (result != null) {
                    val (title, img) = result
                    foodList.add(Food(img, title))
                }
            }








            Log.d("MyLogMAct", "rec_receipe, $rec_receipe")

        }

        val getNumberFunction = module.callAttr("rec_system", 10)
        val number = getNumberFunction






        foodAdapter = FoodAdapter(foodList)
        recyclerView.adapter = foodAdapter


        return binding.root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        // TODO: Use the ViewModel
    }


    private fun init() {
        recyclerView = binding.RecyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        foodList = ArrayList()



        foodAdapter = FoodAdapter(foodList)
        recyclerView.adapter = foodAdapter
    }


}
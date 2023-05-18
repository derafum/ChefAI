package com.example.myapplication.ui.home
import android.annotation.SuppressLint
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.*
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.Food
import com.example.myapplication.FoodAdapter


class Home : Fragment() {

    companion object {
        fun newInstance() = Home()
    }

    private lateinit var viewModel: HomeViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var foodList:ArrayList<Food>
    private lateinit var foodAdapter: FoodAdapter


    private lateinit var binding: FragmentHomeBinding




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)



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

        init()
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
        addDataToList()
        foodAdapter = FoodAdapter(foodList)
        recyclerView.adapter = foodAdapter
    }





    private fun addDataToList(){

        foodList.add(Food(R.drawable.recipe1, "Paneer Butter"))
        foodList.add(Food(R.drawable.recipe2, "Pizza"))
        foodList.add(Food(R.drawable.recipe3, "Dosa"))
        foodList.add(Food(R.drawable.recipe4, "Veg Biryani"))
        foodList.add(Food(R.drawable.recipe5, "Pasta"))
        foodList.add(Food(R.drawable.recipe6, "Noodles"))
    }

}
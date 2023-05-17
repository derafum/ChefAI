package com.example.myapplication.ui.home

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Activity2
import com.example.myapplication.DatabaseHelper
import com.example.myapplication.Recipe
import com.example.myapplication.RecipeAdapter
import com.example.myapplication.databinding.FragmentHomeBinding

class Home : Fragment() {

    companion object {
        fun newInstance() = Home()
    }

    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHomeBinding.inflate(inflater)


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
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
package com.example.myapplication.ui.search

import android.content.ClipData
import android.content.ClipData.Item
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.DatabaseHelper
import com.example.myapplication.R
import com.example.myapplication.RecipeAdapter
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.databinding.FragmentSearchBinding
import com.example.myapplication.ui.analize.Analyze

class Search : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val adapter = RecipeAdapter()
    private val imageIdList = listOf(
        R.drawable.recipe1,
        R.drawable.recipe2,
        R.drawable.recipe3,
        R.drawable.recipe4,
        R.drawable.recipe5,
        R.drawable.recipe6,
    )
    private var index = 0


    companion object {
        fun newInstance() = Search()
        private const val TAG = "Search"
    }

    private lateinit var viewModel: SearchViewModel
    private lateinit var binding: FragmentSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchView.clearFocus()

        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Выполните действия при отправке текста запроса
                query?.let { logSearchText(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Выполните действия при изменении текста запроса
                newText?.let { filterList(it) }
                return true
            }
        })

        binding.reView3.setHasFixedSize(true)
        binding.reView3.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
    }

    private fun logSearchText(query: String) {
        // Выведите текст запроса в лог
        Log.d(TAG, "Search Query: ${query}")
        // Получите данные рецепта по слову запроса
        val dbHelper = DatabaseHelper(requireActivity())
        val recipeDataList = dbHelper.getRecipeDataByWord(query)

        // Выведите данные рецепта в лог
        for (recipeData in recipeDataList) {
            Log.d(TAG, "Recipe Name: ${recipeData.name}")
            Log.d(TAG, "Time: ${recipeData.time}")
            Log.d(TAG, "Image: ${recipeData.img}")
            Log.d(TAG, "Amount of Servings: ${recipeData.amountServings}")
            Log.d(TAG, "Energy: ${recipeData.energy}")
            Log.d(TAG, "Ingredients: ${recipeData.ingredients}")
            Log.d(TAG, "Instructions: ${recipeData.instructions}")
        }
    }

    private fun filterList(text: String) {
        // Выполните фильтрацию списка на основе текста запроса
        // и обновите адаптер списка
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        // TODO: Use the ViewModel
    }
}

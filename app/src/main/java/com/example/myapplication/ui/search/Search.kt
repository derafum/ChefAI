package com.example.myapplication.ui.search
import DatabaseHelper
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Recipe
import com.example.myapplication.RecipeAdapter
import com.example.myapplication.databinding.FragmentSearchBinding


class Search : Fragment() {

    private val adapter = RecipeAdapter()



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

        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { logSearchText(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { filterList(it) }
                return true
            }
        })

        binding.reView3.setHasFixedSize(true)
        binding.reView3.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        binding.reView3.adapter = adapter // Установка адаптера для RecyclerView
    }

    private fun logSearchText(query: String) {
        Log.d(TAG, "Search Query: $query")

        val dbHelper = DatabaseHelper(requireActivity())
        val recipeDataList = dbHelper.getRecipeDataByName(query)

        adapter.recipeList.clear()
        adapter.notifyDataSetChanged()

        for (recipeData in recipeDataList) {
            Log.d(TAG, "Recipe Name: ${recipeData.name}")
            Log.d(TAG, "Time: ${recipeData.time}")
            Log.d(TAG, "Image: ${recipeData.img}")
            Log.d(TAG, "Energy: ${recipeData.energy}")
            Log.d(TAG, "Ingredients: ${recipeData.ingredients}")
            Log.d(TAG, "Instructions: ${recipeData.instructions}")
            Log.d(TAG, "Numbers: ${recipeData.number}")

            val recipe = Recipe(recipeData.img, recipeData.name, recipeData.time)
            adapter.addRecipe(recipe)
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

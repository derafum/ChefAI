package com.example.myapplication.ui.home

import FoodAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.*
import com.example.myapplication.databinding.FragmentHomeBinding
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Home : Fragment(), RecipeClickListener {

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

        val dbHelper = DatabaseHelper(requireActivity())
        var offset = 0

        binding.reView.layoutManager = LinearLayoutManager(context)



        val adapter = RecipeAdapter(this)
        binding.reView.adapter = adapter

        GlobalScope.launch(Dispatchers.Main) {
            val recipes = withContext(Dispatchers.IO) { dbHelper.getTopRecipesByLikes(10, offset) }

            for (recipe in recipes) {
                val name = recipe.name
                val img = recipe.img
                val time = recipe.time
                offset += 10
                val recipeItem = Recipe(img, name, "Time: $time")
                adapter.addRecipe(recipeItem)
                Log.d("recipe", "Name, $name, img, $img, time, $time")
            }

            withContext(Dispatchers.IO) {
                val lenCountRecommend = requireContext().getSharedPreferences("length", Context.MODE_PRIVATE)?.getInt("len", 0)


                val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val countRecommendSet = sharedPreferences?.getString("test", "")

                Log.d("MyLogMAct", "countRecommendLststring, $countRecommendSet")

                val countRecommendList = mutableListOf<Int>()

                for (i in 0 until lenCountRecommend!!) {
                    val countRecommend = sharedPreferences?.getInt("count_recommend$i", 0)
                    if (countRecommend != null) {
                        countRecommendList.add(countRecommend)
                    }
                }

                Log.d("MyLogMAct", "lenCountRecommend, $lenCountRecommend")
                Log.d("MyLogMAct", "countRecommendList, $countRecommendList")


                if (! Python.isStarted()) {
                    Python.start(AndroidPlatform(requireActivity()));
                }

                val python = Python.getInstance()
                val module = python.getModule("Rec_system_new")
                val foodList = ArrayList<Food>()

                for (number in countRecommendList) {
                    val getNumberFunction = module.callAttr("rec_system", number)
                    val recReceipe = getNumberFunction.asList()

                    for (receipeEntry in recReceipe) {
                        val receipe = receipeEntry.toInt()
                        val result = dbHelper.getRecipeUrlAndImgByNumber(receipe)
                        if (result != null) {
                            val (title, img) = result
                            foodList.add(Food(img, title))
                        }
                    }

                    Log.d("MyLogMAct", "recReceipe, $recReceipe")
                }

                launch(Dispatchers.Main) {
                    recyclerView = binding.RecyclerView
                    recyclerView.setHasFixedSize(true)
                    recyclerView.layoutManager = LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
                    foodAdapter = FoodAdapter(foodList)
                    recyclerView.adapter = foodAdapter
                }
            }
        }

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

    override fun onClick(recipe: Recipe) {
        val fragment = CardFragment()
        val bundle = Bundle()
        bundle.putInt(RECIPE_ID_EXTRA, recipe.imageId.toInt())
        val imageId = recipe.imageId.toIntOrNull()
        if (imageId != null) {
            bundle.putInt(RECIPE_ID_EXTRA, imageId)
        } else {
            // Обработка случая, когда значение imageId не может быть преобразовано в целое число
        }
        fragment.arguments = bundle

        val fragmentManager = requireActivity().supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment) // Замените `R.id.fragmentContainer` на идентификатор вашего контейнера для фрагментов
        transaction.addToBackStack(null)
        transaction.commit()
    }
}

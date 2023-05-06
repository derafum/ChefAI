package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.RecipeItemBinding

class RecipeAdapter: RecyclerView.Adapter<RecipeAdapter.RecipeHolder>() {
    val recipeList = ArrayList<Recipe>()
    class RecipeHolder(item: View): RecyclerView.ViewHolder(item) {
        val binding = RecipeItemBinding.bind(item)
        fun bind(recipe: Recipe) = with(binding){
            im.setImageResource(recipe.imageId)
            tvTitle.text = recipe.title
            tvTime.text = recipe.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recipe_item, parent, false)
        return RecipeHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeHolder, position: Int) {
        holder.bind(recipeList[position])
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    fun addRecipe(recipe: Recipe) {
        recipeList.add(recipe)
        notifyDataSetChanged()
    }
}
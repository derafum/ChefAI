package com.example.myapplication

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.databinding.RecipeItemBinding

class RecipeAdapter(private val clickListener: RecipeClickListener) : RecyclerView.Adapter<RecipeAdapter.RecipeHolder>() {
    private val recipeList = ArrayList<Recipe>()


    class RecipeHolder(item: View, private val clickListener: RecipeClickListener) : RecyclerView.ViewHolder(item) {


        private val binding = RecipeItemBinding.bind(item)
        private val TAG = "CameraXApp"


        fun bind(recipe: Recipe) = with(binding) {
            Glide.with(itemView)
                .load(recipe.imageId)
                .into(im)
            tvTitle.text = recipe.title
            tvTime.text = recipe.time

            binding.recipe1card.setOnClickListener {
                clickListener.onClick(recipe)
                Log.e(TAG, "okey")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.recipe_item, parent, false)
        return RecipeHolder(view, clickListener)
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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.Recipe
import com.example.myapplication.databinding.RecipeItemBinding

class RecipeAdapter : RecyclerView.Adapter<RecipeAdapter.RecipeHolder>() {
    val recipeList = mutableListOf<Recipe>()

    class RecipeHolder(item: View) : RecyclerView.ViewHolder(item) {
        private val binding = RecipeItemBinding.bind(item)

        fun bind(recipe: Recipe) {
            Glide.with(itemView)
                .load(recipe.imageId)
                .into(binding.im)
            binding.tvTitle.text = recipe.title
            binding.tvTime.text = recipe.time
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recipe_item, parent, false)
        return RecipeHolder(view)
    }

    override fun onBindViewHolder(holder: RecipeHolder, position: Int) {
        val recipe = recipeList.getOrNull(position)
        recipe?.let {
            holder.bind(it)
        }
    }

    override fun getItemCount(): Int {
        return recipeList.size
    }

    fun addRecipe(recipe: Recipe) {
        recipeList.add(recipe)
        notifyDataSetChanged()
    }
}

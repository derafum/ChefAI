import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.Food
import com.example.myapplication.R
import com.example.myapplication.ui.home.Home

class FoodAdapter(val foodList: List<Food>, private val onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    class FoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val foodImageView: ImageView = itemView.findViewById(R.id.imageView)
        val foodNameTv: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.each_item, parent, false)
        return FoodViewHolder(view)
    }

    override fun getItemCount(): Int {
        return foodList.size
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]
        Glide.with(holder.itemView)
            .load(food.foodImage) // Подставьте строку с URL или путем к файлу
            .into(holder.foodImageView)
        holder.foodNameTv.text = food.foodName
        holder.itemView.setOnClickListener {
            onItemClickListener.onItemClick(position)
        }
    }
}

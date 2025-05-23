package com.example.greenaura

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PlantAdapter(
    private val context: Context,
    private var plantList: List<Plant>,
    private val onClick: (Plant) -> Unit
) : RecyclerView.Adapter<PlantAdapter.PlantViewHolder>() {

    inner class PlantViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val plantImage: ImageView = view.findViewById(R.id.plantImage)
        val plantName: TextView = view.findViewById(R.id.plantName)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onClick(plantList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlantViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_plant, parent, false)
        return PlantViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlantViewHolder, position: Int) {
        val plant = plantList[position]
        holder.plantName.text = plant.common_name
        Glide.with(context)
            .load(plant.Image_Src)
            .placeholder(R.drawable.ic_placeholder) // Optional: Add a placeholder
            .error(R.drawable.ic_error)             // Optional: Add an error fallback
            .into(holder.plantImage)
    }

    override fun getItemCount(): Int = plantList.size

    fun filterList(filtered: List<Plant>) {
        plantList = filtered
        notifyDataSetChanged()
    }
}

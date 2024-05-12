package com.example.music_app.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.music_app.databinding.CategoryItemRecyclerRowBinding
import com.example.music_app.models.CategoryModel

class CategoryAdapter (private val categoryList : List<CategoryModel>) :
    RecyclerView.Adapter<CategoryAdapter.MyViewHolder>() {
    class MyViewHolder(private val binding: CategoryItemRecyclerRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        //bind the data with views
            fun bindData(category:CategoryModel){
                binding.nameTextView.text = category.name


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = CategoryItemRecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bindData(categoryList[position])
    }
}
package com.example.music_app.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.music_app.SongsListActivity
import com.example.music_app.databinding.CategoryItemRecyclerRowBinding
import com.example.music_app.models.CategoryModel
/*
* Author: Ngô Phạm Quang Vinh
* Main fuction:
* - Display a list of categories (genres) in RecyclerView.
* - Handle events when users click on a category*/

class CategoryAdapter(private val categoryList: List<CategoryModel>) :
    RecyclerView.Adapter<CategoryAdapter.MyViewHolder>() {
    // Khởi tạo adapter với danh sách các category

    class MyViewHolder(private val binding: CategoryItemRecyclerRowBinding) :
        RecyclerView.ViewHolder(binding.root) {
        // ViewHolder để hiển thị một category trong RecyclerView

        fun bindData(category: CategoryModel) {
            // Gán dữ liệu cho các view trong ViewHolder và xử lý sự kiện nhấn vào category
            binding.nameTextView.text = category.name
            Glide.with(binding.coverImageView).load(category.coverUrl)
                .apply(
                    RequestOptions().transform(RoundedCorners(32))
                )
                .into(binding.coverImageView)
            // Start SongsList Activity
            val context = binding.root.context
            binding.root.setOnClickListener {
                SongsListActivity.category = category
                context.startActivity(Intent(context, SongsListActivity::class.java))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Tạo một ViewHolder mới từ layout file
        val binding = CategoryItemRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        // Trả về số lượng category trong danh sách
        return categoryList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // Gán dữ liệu cho ViewHolder tại vị trí position trong danh sách
        holder.bindData(categoryList[position])
    }
}
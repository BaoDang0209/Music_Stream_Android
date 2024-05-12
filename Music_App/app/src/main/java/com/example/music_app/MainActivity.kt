package com.example.music_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music_app.adapter.CategoryAdapter
import com.example.music_app.databinding.ActivityMainBinding
import com.example.music_app.databinding.CategoryItemRecyclerRowBinding
import com.example.music_app.models.CategoryModel
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var categoryAdapter: CategoryAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getCategories()
    }

    fun getCategories(){
        FirebaseFirestore.getInstance().collection("category")
            .get().addOnSuccessListener {
                val categoryList = it.toObjects(CategoryModel::class.java)
                setupCategoryRecyclerView(categoryList)
            }
    }
    fun setupCategoryRecyclerView(categoryList:List<CategoryModel>){
        categoryAdapter = CategoryAdapter(categoryList)
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        binding.categoriesRecyclerView.adapter = categoryAdapter
    }
}
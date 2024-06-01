package com.example.music_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.music_app.adapter.CategoryAdapter
import com.example.music_app.adapter.SectionSongListAdapter
import com.example.music_app.databinding.ActivityMainBinding
import com.example.music_app.models.CategoryModel
import com.example.music_app.models.SongModel
import com.example.music_app.utils.MyExoplayer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects

/*
* Author: Đỗ Huynh Bảo Đăng
* Main fuction: Display song and setup recyclerview*/
class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var categoryAdapter: CategoryAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Show categories and sections in main page
        getCategories()
        setupSection("section_1",binding.section1MainLayout,binding.section1Title,binding.section1RecyclerView)
        setupSection("section_2",binding.section2MainLayout,binding.section2Title,binding.section2RecyclerView)
        setupSection("section_3",binding.section3MainLayout,binding.section3Title,binding.section3RecyclerView)
        setupMostlyPlayed("mostly_played",binding.mostlyPlayedMainLayout,binding.mostlyPlayedTitle,binding.mostlyPlayedRecyclerView)

        binding.optionBtn.setOnClickListener {
            showPopupMenu()
        }


    }
    //  Show all menu items when the menu pops up
    fun showPopupMenu(){

        val popupMenu = PopupMenu(this,binding.optionBtn)
        val inflator = popupMenu.menuInflater
        inflator.inflate(R.menu.option_menu,popupMenu.menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.logout -> {
                    logout()
                    true
                }
            }
            when(it.itemId){
                R.id.profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
            }
            when(it.itemId){
                R.id.favories -> {
                    val intent = Intent(this, FavoristListActivity::class.java)
                    startActivity(intent)
                    true
                }
            }
            false
        }
    }
    // Funcion of logout
    fun logout(){
        MyExoplayer.getInstance()?.release()
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this,LoginActivity::class.java))
        finish()
    }
    //Resume music below main page
    override fun onResume() {
        super.onResume()
        showPlayerView()
    }
    // Show music below main page
    fun showPlayerView(){
        binding.playerView.setOnClickListener {
            startActivity(Intent(this,PlayerActivity::class.java))
        }
        MyExoplayer.getCurrentSong()?.let {
            binding.playerView.visibility = View.VISIBLE
            binding.songTitleTextView.text = "Now Playing : " + it.title
            Glide.with(binding.songCoverImageView).load(it.coverUrl)
                .apply(
                    RequestOptions().transform(RoundedCorners(32))
                ).into(binding.songCoverImageView)
        } ?: run{
            binding.playerView.visibility = View.GONE
        }
    }
    //Categories
    fun getCategories(){
        FirebaseFirestore.getInstance().collection("category")
            .get().addOnSuccessListener {
                val categoryList = it.toObjects(CategoryModel::class.java)
                setupCategoryRecyclerView(categoryList)
            }
    }
    // Set up categories
    fun setupCategoryRecyclerView(categoryList:List<CategoryModel>){
        categoryAdapter = CategoryAdapter(categoryList)
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        binding.categoriesRecyclerView.adapter = categoryAdapter
    }
    //Set up all Sections ( K-Pop , V-Pop ,...)
    fun setupSection(id : String,mainLayout : RelativeLayout,titleView : TextView,recyclerView: RecyclerView){
        FirebaseFirestore.getInstance().collection("sections")
            .document(id)
            .get().addOnSuccessListener {
                val section = it.toObject(CategoryModel::class.java)
                section?.apply {
                    mainLayout.visibility = View.VISIBLE
                    titleView.text = name
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity,LinearLayoutManager.HORIZONTAL,false)
                    recyclerView.adapter = SectionSongListAdapter(songs)
                    mainLayout.setOnClickListener {
                        SongsListActivity.category = section
                        startActivity(Intent(this@MainActivity,SongsListActivity::class.java))
                    }
                }
            }

    }
    // Show all music that has been clicked to listen
    fun setupMostlyPlayed(id : String,mainLayout : RelativeLayout,titleView : TextView,recyclerView: RecyclerView){
        FirebaseFirestore.getInstance().collection("sections")
            .document(id)
            .get().addOnSuccessListener {
                //get most played songs
                FirebaseFirestore.getInstance().collection("songs")
                    .orderBy("count",Query.Direction.DESCENDING)
                    .limit(5)
                    .get().addOnSuccessListener {songListSnapshot->
                        val songsModelList = songListSnapshot.toObjects<SongModel>()
                        val songsIdList = songsModelList.map{
                            it.id
                        }.toList()
                        val section = it.toObject(CategoryModel::class.java)
                        section?.apply {
                            section.songs = songsIdList
                            mainLayout.visibility = View.VISIBLE
                            titleView.text = name
                            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity,LinearLayoutManager.HORIZONTAL,false)
                            recyclerView.adapter = SectionSongListAdapter(songs)
                            mainLayout.setOnClickListener {
                                SongsListActivity.category = section
                                startActivity(Intent(this@MainActivity,SongsListActivity::class.java))
                            }
                        }
                    }


            }
    }

}
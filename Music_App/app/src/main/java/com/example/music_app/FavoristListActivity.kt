package com.example.music_app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music_app.adapter.FavoritesListAdapter
import com.example.music_app.databinding.ActivityFavoristSongsBinding
import com.google.firebase.auth.FirebaseAuth

class FavoristListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoristSongsBinding
    private lateinit var favoritesListAdapter: FavoritesListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoristSongsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val userId = user.uid
            setupSongsListRecyclerView(userId)
        } else {
            // Handle the case where the user is not logged in
            finish()
        }

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setupSongsListRecyclerView(userId: String) {
        favoritesListAdapter = FavoritesListAdapter(userId)
        binding.favoritelistRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.favoritelistRecyclerview.adapter = favoritesListAdapter
    }
}

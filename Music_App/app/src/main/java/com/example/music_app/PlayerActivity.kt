package com.example.music_app

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.example.music_app.databinding.ActivityPlayerBinding
import com.example.music_app.models.SongModel
import com.example.music_app.models.UserModel
import com.example.music_app.utils.MyExoplayer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var exoPlayer: ExoPlayer

    private val database = FirebaseDatabase.getInstance("https://music-stream-ef950-default-rtdb.asia-southeast1.firebasedatabase.app")
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            showGif(isPlaying)
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize App Check
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance()
        )

        MyExoplayer.getCurrentSong()?.let { currentSong ->
            binding.songTitleTextView.text = currentSong.title
            binding.songSubtitleTextView.text = currentSong.subtitle
            Glide.with(binding.songCoverImageView).load(currentSong.coverUrl)
                .circleCrop()
                .into(binding.songCoverImageView)
            Glide.with(binding.songGifImageView).load(R.drawable.media_playing)
                .circleCrop()
                .into(binding.songGifImageView)
            exoPlayer = MyExoplayer.getInstance()!!
            binding.playerView.player = exoPlayer
            binding.playerView.showController()
            exoPlayer.addListener(playerListener)
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.favoristBtn.setOnClickListener {
            Log.d("PlayerActivity", "Favorite button clicked")
            fetchSongDataAndAddToFavorites()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.removeListener(playerListener)
    }

    private fun showGif(show: Boolean) {
        binding.songGifImageView.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    private fun fetchSongDataAndAddToFavorites() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            showToast("User not logged in")
            return
        }

        val userId = currentUser.uid
        val userRef = database.getReference("users").child(userId)

        val currentSong = MyExoplayer.getCurrentSong()
        if (currentSong == null) {
            showToast("No song currently playing")
            return
        }

        // Fetch the song URL from Firebase Storage
        storage.getReferenceFromUrl(currentSong.url).downloadUrl.addOnSuccessListener { uri ->
            val favoriteSong = SongModel(
                id = "song_${System.currentTimeMillis()}",
                title = currentSong.title,
                subtitle = currentSong.subtitle,
                url = uri.toString(),
                coverUrl = currentSong.coverUrl
            )

            // Update user's favorite songs in Firebase Realtime Database
            userRef.get().addOnSuccessListener { dataSnapshot ->
                val user = dataSnapshot.getValue(UserModel::class.java)
                val updatedFavorites = user?.favoritesongs?.toMutableList() ?: mutableListOf()
                updatedFavorites.add(favoriteSong)

                userRef.child("favoritesongs").setValue(updatedFavorites)
                    .addOnSuccessListener {
                        showToast("Added to favorites!")
                    }
                    .addOnFailureListener { error ->
                        showToast("Failed to add to favorites: ${error.message}")
                    }
            }.addOnFailureListener { error ->
                showToast("Failed to fetch user data: ${error.message}")
            }
        }.addOnFailureListener { error ->
            showToast("Failed to fetch song data: ${error.message}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

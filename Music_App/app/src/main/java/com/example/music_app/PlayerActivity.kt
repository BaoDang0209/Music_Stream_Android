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
/*
* Author: Đỗ Huynh Bảo Đăng
* Main fuction: Display song and song player*/
class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private lateinit var exoPlayer: ExoPlayer
    private var isFavorite = false

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

            // Check if the current song is already in the favorites
            checkIfFavorite(currentSong)
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.favoriteBtn.setOnClickListener {
            MyExoplayer.getCurrentSong()?.let { currentSong ->
                if (isFavorite) {
                    removeSongFromFavorites(currentSong)
                } else {
                    addSongToFavorites(currentSong)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.removeListener(playerListener)
    }

    private fun showGif(show: Boolean) {
        // Hiển thị hoặc ẩn GIF phát nhạc
        binding.songGifImageView.visibility = if (show) View.VISIBLE else View.INVISIBLE
    }

    /*
* Author:Nguyễn Duy Mạnh
* Main fuction: Remove song from favorite list*/
    private fun checkIfFavorite(song: SongModel) {
        // Kiểm tra xem bài hát hiện tại có nằm trong danh sách yêu thích của người dùng hay không
        val currentUser = auth.currentUser ?: return

        val userId = currentUser.uid
        val userRef = database.getReference("users").child(userId).child("favoritesongs")

        userRef.child(song.id).get().addOnSuccessListener { dataSnapshot ->
            isFavorite = dataSnapshot.exists()
            updateFavoriteIcon()
        }.addOnFailureListener { error ->
            Log.e("PlayerActivity", "Error checking if song is favorite: ${error.message}")
        }
    }

    private fun addSongToFavorites(song: SongModel) {
        // Thêm bài hát vào danh sách yêu thích của người dùng
        val currentUser = auth.currentUser ?: return

        val userId = currentUser.uid
        val userRef = database.getReference("users").child(userId).child("favoritesongs")

        userRef.child(song.id).setValue(song)
            .addOnSuccessListener {
                isFavorite = true
                updateFavoriteIcon()
                showToast("Added to favorites!")
            }
            .addOnFailureListener { error ->
                showToast("Failed to add to favorites: ${error.message}")
            }
    }

    private fun removeSongFromFavorites(song: SongModel) {
        // Xóa bài hát khỏi danh sách yêu thích của người dùng
        val currentUser = auth.currentUser ?: return

        val userId = currentUser.uid
        val userRef = database.getReference("users").child(userId).child("favoritesongs")

        userRef.child(song.id).removeValue()
            .addOnSuccessListener {
                isFavorite = false
                updateFavoriteIcon()
                showToast("Removed from favorites!")
            }
            .addOnFailureListener { error ->
                showToast("Failed to remove from favorites: ${error.message}")
            }
    }

    private fun updateFavoriteIcon() {
        // Cập nhật biểu tượng yêu thích trên nút yêu thích
        binding.favoriteBtn.setImageResource(
            if (isFavorite) R.drawable.ic_favorite_done else R.drawable.ic_favorite
        )
    }

    private fun showToast(message: String) {
        // Hiển thị một thông báo ngắn
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
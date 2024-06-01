package com.example.music_app.utils

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.music_app.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore

/*
* Author: Đỗ Huynh Bảo Đăng
* Main fuction:song player*/
object MyExoplayer {
    // Khai báo ExoPlayer và biến lưu trữ bài hát hiện tại
    private var exoPlayer: ExoPlayer? = null
    private var currentSong: SongModel? = null

    fun getCurrentSong(): SongModel? {
        // Trả về bài hát hiện tại
        return currentSong
    }

    fun getInstance(): ExoPlayer? {
        // Trả về instance của ExoPlayer
        return exoPlayer
    }

    fun startPlaying(context: Context, song: SongModel) {
        // Bắt đầu phát bài hát
        if (exoPlayer == null)
            exoPlayer = ExoPlayer.Builder(context).build() // Tạo ExoPlayer nếu chưa có

        if (currentSong != song) {
            // Nếu bài hát mới khác với bài hát hiện tại, cập nhật bài hát hiện tại và bắt đầu phát
            currentSong = song

            currentSong?.url?.apply {
                val mediaItem = MediaItem.fromUri(this)
                exoPlayer?.setMediaItem(mediaItem)
                exoPlayer?.prepare()
                exoPlayer?.play()
            }
        }
    }

    fun updateCount() {
        // Cập nhật số lượt phát của bài hát hiện tại trên Firestore
        currentSong?.id?.let { id ->
            FirebaseFirestore.getInstance().collection("songs")
                .document(id)
                .get().addOnSuccessListener {
                    var latestCount = it.getLong("count")
                    if (latestCount == null) {
                        latestCount = 1L
                    } else {
                        latestCount = latestCount + 1
                    }

                    FirebaseFirestore.getInstance().collection("songs")
                        .document(id)
                        .update(mapOf("count" to latestCount))
                }
        }
    }
}
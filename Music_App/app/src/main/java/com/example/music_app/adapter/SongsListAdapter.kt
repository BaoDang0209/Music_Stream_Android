package com.example.music_app.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.music_app.utils.MyExoplayer
import com.example.music_app.PlayerActivity
import com.example.music_app.databinding.SongListItemRecyclerRowBinding
import com.example.music_app.models.SongModel
import com.google.firebase.firestore.FirebaseFirestore
/*
* Author: Nguyễn Duy Mạnh
* Main fuction: Display song list in RecyclerView.
* Get data from Firestore by songId.
* Press song: play in PlayerActivity.

* */
class SongsListAdapter(private val songIdList: List<String>) :
    RecyclerView.Adapter<SongsListAdapter.MyViewHolder>() {
    // Khởi tạo adapter với danh sách các songId

    class MyViewHolder(private val binding: SongListItemRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {
        // ViewHolder để hiển thị một bài hát trong RecyclerView

        fun bindData(songId: String) {
            // Lấy dữ liệu bài hát từ Firestore và gán cho các view trong ViewHolder
            // Xử lý sự kiện nhấn vào bài hát để phát nhạc
            FirebaseFirestore.getInstance().collection("songs")
                .document(songId).get()
                .addOnSuccessListener {
                    val song = it.toObject(SongModel::class.java)
                    song?.apply {
                        binding.songTitleTextView.text = title
                        binding.songSubtitleTextView.text = subtitle
                        Glide.with(binding.songCoverImageView).load(coverUrl)
                            .apply(
                                RequestOptions().transform(RoundedCorners(32))
                            )
                            .into(binding.songCoverImageView)
                        binding.root.setOnClickListener {
                            MyExoplayer.startPlaying(binding.root.context, song)
                            it.context.startActivity(Intent(it.context, PlayerActivity::class.java))
                        }
                    }
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        // Tạo một ViewHolder mới từ layout file
        val binding = SongListItemRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        // Trả về số lượng bài hát trong danh sách
        return songIdList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // Gán dữ liệu cho ViewHolder tại vị trí position trong danh sách
        holder.bindData(songIdList[position])
    }
}
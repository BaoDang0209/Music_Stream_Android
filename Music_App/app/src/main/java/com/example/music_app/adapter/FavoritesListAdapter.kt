package com.example.music_app.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.music_app.PlayerActivity
import com.example.music_app.databinding.FavoriteListItemRecyclerRowBinding
import com.example.music_app.models.SongModel
import com.example.music_app.utils.MyExoplayer
import com.google.firebase.database.*

class FavoritesListAdapter(private val userId: String) : RecyclerView.Adapter<FavoritesListAdapter.MyViewHolder>() {

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://music-stream-ef950-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .getReference("users")
        .child(userId)
        .child("favoritesongs")
    private val favoriteSongsList = mutableListOf<SongModel>()

    init {
        // Fetch favorite songs
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                favoriteSongsList.clear()
                Log.d("FavoritesListAdapter", "Data snapshot received: ${snapshot.value}")
                for (childSnapshot in snapshot.children) {
                    val song = childSnapshot.getValue(SongModel::class.java)
                    if (song != null) {
                        favoriteSongsList.add(song)
                        Log.d("FavoritesListAdapter", "Added song: $song")
                    } else {
                        Log.e("FavoritesListAdapter", "Error: SongModel conversion failed for $childSnapshot")
                    }
                }
                Log.d("FavoritesListAdapter", "Fetched ${favoriteSongsList.size} songs")
                notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FavoritesListAdapter", "Database error: ${error.message}")
            }
        })
    }

    class MyViewHolder(private val binding: FavoriteListItemRecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {
        private val removeFavoriteButton: ImageButton = binding.removeFavoriteButton

        fun bindData(song: SongModel, onRemoveClicked: (SongModel) -> Unit) {
            binding.songTitleTextView.text = song.title
            binding.songSubtitleTextView.text = song.subtitle
            Glide.with(binding.songCoverImageView.context).load(song.coverUrl)
                .apply(RequestOptions().transform(RoundedCorners(32)))
                .into(binding.songCoverImageView)
            binding.root.setOnClickListener {
                MyExoplayer.startPlaying(binding.root.context, song)
                it.context.startActivity(Intent(it.context, PlayerActivity::class.java))
            }

            removeFavoriteButton.setOnClickListener {
                onRemoveClicked(song)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = FavoriteListItemRecyclerRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return favoriteSongsList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val song = favoriteSongsList[position]
        holder.bindData(song) { songToRemove ->
            removeSongFromFavorites(songToRemove)
        }
    }

    private fun removeSongFromFavorites(song: SongModel) {
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance("https://music-stream-ef950-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("users")
            .child(userId)
            .child("favoritesongs")

        // Fetch the current list of favorite songs
        databaseReference.get().addOnSuccessListener { dataSnapshot ->
            val favoriteSongsList = dataSnapshot.children.mapNotNull { it.getValue(SongModel::class.java) }.toMutableList()
            val songToRemove = favoriteSongsList.find { it.id == song.id } // Assuming SongModel has an 'id' property

            if (songToRemove != null) {
                favoriteSongsList.remove(songToRemove)

                // Update the list in Firebase
                databaseReference.setValue(favoriteSongsList)
                    .addOnSuccessListener {
                        Log.d("FavoritesListAdapter", "Song removed from favorites: $song")
                        // Optionally update the local list and notify adapter
                        // favoriteSongsList.remove(song)
                        // notifyDataSetChanged()
                    }
                    .addOnFailureListener { error ->
                        Log.e("FavoritesListAdapter", "Error removing song from favorites: $error")
                    }
            } else {
                Log.d("FavoritesListAdapter", "Song not found in favorites: $song")
            }
        }.addOnFailureListener { error ->
            Log.e("FavoritesListAdapter", "Error fetching favorite songs: $error")
        }
    }
}
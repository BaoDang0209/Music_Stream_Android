package com.example.music_app.models

data class UserModel(
    val fullname: String = "",
    val email: String = "",
    val phone: String = "",
    val image: String = "",
    val favoritesongs: List<SongModel> = listOf()
)
{
    constructor() : this("","","","", listOf())
}

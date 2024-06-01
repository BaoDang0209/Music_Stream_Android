package com.example.music_app.models

// Khởi tạo model
data class CategoryModel(
    val name:String,
    val coverUrl:String,
    var songs : List<String>
){
    constructor(): this("","",listOf())
}

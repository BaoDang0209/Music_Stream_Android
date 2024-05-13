package com.example.music_app.models

data class CategoryModel(
    val name:String,
    val coverUrl:String,
    val songs : List<String>
){
    constructor(): this("","",listOf())
}

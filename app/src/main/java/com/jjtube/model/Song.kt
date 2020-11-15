package com.jjtube.model

data class Song(
    val id:Int,
    val displayName:String,
    val path:String,
    val size:Long,
    val duration:Long,
    val modified:Long,
    val album:String? = null,
    val albumArtist:String? = null
)
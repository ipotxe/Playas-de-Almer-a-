package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_beaches")
data class FavoriteBeachEntity(
    @PrimaryKey
    val beachId: String,
    val isFavorite: Boolean = true,
    val isVisited: Boolean = false,
    val userNotes: String = "",
    val userRating: Float = 0f,
    val addedTimestamp: Long = System.currentTimeMillis()
)

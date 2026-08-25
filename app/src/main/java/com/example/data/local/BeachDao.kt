package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BeachDao {
    @Query("SELECT * FROM favorite_beaches WHERE isFavorite = 1 ORDER BY addedTimestamp DESC")
    fun getFavorites(): Flow<List<FavoriteBeachEntity>>

    @Query("SELECT * FROM favorite_beaches")
    fun getAllUserBeachData(): Flow<List<FavoriteBeachEntity>>

    @Query("SELECT * FROM favorite_beaches WHERE beachId = :beachId LIMIT 1")
    fun getFavoriteByBeachId(beachId: String): Flow<FavoriteBeachEntity?>

    @Query("SELECT * FROM favorite_beaches WHERE beachId = :beachId LIMIT 1")
    suspend fun getFavoriteDirect(beachId: String): FavoriteBeachEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: FavoriteBeachEntity)

    @Query("DELETE FROM favorite_beaches WHERE beachId = :beachId")
    suspend fun deleteFavorite(beachId: String)

    @Query("UPDATE favorite_beaches SET isFavorite = :isFavorite WHERE beachId = :beachId")
    suspend fun updateFavoriteStatus(beachId: String, isFavorite: Boolean)

    @Query("UPDATE favorite_beaches SET isVisited = :isVisited WHERE beachId = :beachId")
    suspend fun updateVisitedStatus(beachId: String, isVisited: Boolean)

    @Query("UPDATE favorite_beaches SET userNotes = :notes WHERE beachId = :beachId")
    suspend fun updateUserNotes(beachId: String, notes: String)
}

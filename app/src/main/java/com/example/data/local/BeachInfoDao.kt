package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for retrieving and managing beach information in Room.
 * Provides reactive [Flow] queries and suspend functions for database operations.
 */
@Dao
interface BeachInfoDao {

    /**
     * Retrieve all beaches ordered alphabetically by name.
     */
    @Query("SELECT * FROM beaches ORDER BY name ASC")
    fun getAllBeaches(): Flow<List<BeachEntity>>

    /**
     * Retrieve a specific beach by its unique identifier.
     */
    @Query("SELECT * FROM beaches WHERE id = :beachId LIMIT 1")
    fun getBeachById(beachId: String): Flow<BeachEntity?>

    /**
     * One-shot fetch of a specific beach by ID.
     */
    @Query("SELECT * FROM beaches WHERE id = :beachId LIMIT 1")
    suspend fun getBeachByIdDirect(beachId: String): BeachEntity?

    /**
     * Retrieve only beaches that are suitable for snorkeling,
     * ordered by highest snorkel rating first.
     */
    @Query("SELECT * FROM beaches WHERE is_suitable_for_snorkeling = 1 ORDER BY snorkel_rating DESC")
    fun getBeachesSuitableForSnorkeling(): Flow<List<BeachEntity>>

    /**
     * Retrieve beaches that meet or exceed a specific snorkeling rating threshold.
     */
    @Query("SELECT * FROM beaches WHERE snorkel_rating >= :minRating ORDER BY snorkel_rating DESC")
    fun getBeachesByMinSnorkelRating(minRating: Float): Flow<List<BeachEntity>>

    /**
     * Retrieve beaches by location, matching either location description or municipality.
     */
    @Query("SELECT * FROM beaches WHERE location LIKE '%' || :locationQuery || '%' OR municipality LIKE '%' || :locationQuery || '%' ORDER BY name ASC")
    fun getBeachesByLocation(locationQuery: String): Flow<List<BeachEntity>>

    /**
     * Retrieve beaches within a geographical bounding box defined by coordinates.
     */
    @Query("SELECT * FROM beaches WHERE latitude BETWEEN :minLat AND :maxLat AND longitude BETWEEN :minLng AND :maxLng ORDER BY name ASC")
    fun getBeachesInBoundingBox(
        minLat: Double,
        maxLat: Double,
        minLng: Double,
        maxLng: Double
    ): Flow<List<BeachEntity>>

    /**
     * Search beaches matching a query string in their name, location, or summary.
     */
    @Query("SELECT * FROM beaches WHERE name LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchBeaches(query: String): Flow<List<BeachEntity>>

    /**
     * Insert or update a single beach.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBeach(beach: BeachEntity)

    /**
     * Insert or update multiple beaches.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBeaches(beaches: List<BeachEntity>)

    /**
     * Update an existing beach entity.
     */
    @Update
    suspend fun updateBeach(beach: BeachEntity)

    /**
     * Delete a single beach entity.
     */
    @Delete
    suspend fun deleteBeach(beach: BeachEntity)

    /**
     * Delete all beaches from the database.
     */
    @Query("DELETE FROM beaches")
    suspend fun deleteAllBeaches()

    /**
     * Get the count of beaches currently stored in the database.
     */
    @Query("SELECT COUNT(*) FROM beaches")
    suspend fun getBeachCount(): Int
}

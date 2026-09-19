package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.Beach
import com.example.data.model.WaterSport

/**
 * Room database entity to store beach information including name, location,
 * coordinates, and suitability for snorkeling.
 */
@Entity(tableName = "beaches")
data class BeachEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "location")
    val location: String,

    @ColumnInfo(name = "municipality")
    val municipality: String,

    @ColumnInfo(name = "zone")
    val zone: String,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "is_suitable_for_snorkeling")
    val isSuitableForSnorkeling: Boolean,

    @ColumnInfo(name = "snorkel_rating")
    val snorkelRating: Float,

    @ColumnInfo(name = "snorkel_description")
    val snorkelDescription: String = "",

    @ColumnInfo(name = "summary")
    val summary: String = "",

    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "sand_type")
    val sandType: String = "",

    @ColumnInfo(name = "orientation")
    val orientation: String = "",

    @ColumnInfo(name = "is_virgin_cove")
    val isVirginCove: Boolean = false,

    @ColumnInfo(name = "is_natural_park")
    val isNaturalPark: Boolean = false,

    @ColumnInfo(name = "main_photo_res_id")
    val mainPhotoResId: Int? = null,

    @ColumnInfo(name = "main_photo_url")
    val mainPhotoUrl: String = ""
)

/**
 * Mapper extension to convert domain [Beach] to Room [BeachEntity].
 */
fun Beach.toEntity(): BeachEntity = BeachEntity(
    id = id,
    name = name,
    location = "$municipality, ${zone.displayName}",
    municipality = municipality,
    zone = zone.name,
    latitude = latitude,
    longitude = longitude,
    isSuitableForSnorkeling = snorkelRating >= 4.0f || waterSports.contains(WaterSport.SNORKEL),
    snorkelRating = snorkelRating,
    snorkelDescription = snorkelDescription,
    summary = summary,
    description = description,
    sandType = sandType,
    orientation = orientation.name,
    isVirginCove = services.isVirginCove,
    isNaturalPark = isNaturalPark,
    mainPhotoResId = mainPhotoResId,
    mainPhotoUrl = mainPhotoUrl
)

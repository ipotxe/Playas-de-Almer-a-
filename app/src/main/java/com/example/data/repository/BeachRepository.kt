package com.example.data.repository

import com.example.data.local.BeachDao
import com.example.data.local.BeachDataProvider
import com.example.data.local.FavoriteBeachEntity
import com.example.data.model.*
import com.example.data.remote.MarineWeatherService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BeachRepository(
    private val beachDao: BeachDao,
    private val marineService: MarineWeatherService
) {
    private val allBeaches = BeachDataProvider.getAllBeaches()

    fun getAllBeaches(): List<Beach> = allBeaches

    fun getBeachById(id: String): Beach? = allBeaches.find { it.id == id }

    fun getFavorites(): Flow<List<Beach>> {
        return beachDao.getFavorites().map { entities ->
            val favIds = entities.map { it.beachId }.toSet()
            allBeaches.filter { it.id in favIds }
        }
    }

    fun getAllUserBeachData(): Flow<List<FavoriteBeachEntity>> {
        return beachDao.getAllUserBeachData()
    }

    fun getFavoriteEntity(beachId: String): Flow<FavoriteBeachEntity?> {
        return beachDao.getFavoriteByBeachId(beachId)
    }

    suspend fun toggleFavorite(beachId: String) {
        val existing = beachDao.getFavoriteDirect(beachId)
        if (existing == null) {
            beachDao.insertOrUpdate(
                FavoriteBeachEntity(
                    beachId = beachId,
                    isFavorite = true,
                    isVisited = false
                )
            )
        } else {
            beachDao.updateFavoriteStatus(beachId, !existing.isFavorite)
        }
    }

    suspend fun setVisited(beachId: String, visited: Boolean) {
        val existing = beachDao.getFavoriteDirect(beachId)
        if (existing == null) {
            beachDao.insertOrUpdate(
                FavoriteBeachEntity(
                    beachId = beachId,
                    isFavorite = false,
                    isVisited = visited
                )
            )
        } else {
            beachDao.updateVisitedStatus(beachId, visited)
        }
    }

    suspend fun saveNotes(beachId: String, notes: String) {
        val existing = beachDao.getFavoriteDirect(beachId)
        if (existing == null) {
            beachDao.insertOrUpdate(
                FavoriteBeachEntity(
                    beachId = beachId,
                    isFavorite = false,
                    isVisited = false,
                    userNotes = notes
                )
            )
        } else {
            beachDao.updateUserNotes(beachId, notes)
        }
    }

    suspend fun fetchMarineForecast(zone: Zone): MarineForecast {
        return marineService.fetchMarineForecastForZone(zone)
    }

    fun calculateBathingSafety(beach: Beach, forecast: MarineForecast): BathingSafetyAlert {
        val windType = forecast.currentWindType
        val windSpeed = forecast.windSpeedKmh
        val waveHeight = forecast.waveHeightMeters
        val orientation = beach.orientation

        val isEastFacing = orientation == Orientation.EAST || orientation == Orientation.NORTH_EAST
        val isSouthEastFacing = orientation == Orientation.SOUTH_EAST
        val isWestFacing = orientation == Orientation.WEST || orientation == Orientation.SOUTH_WEST
        val isSouthFacing = orientation == Orientation.SOUTH

        val isProtected: Boolean
        val flag: FlagColor
        val summary: String
        val snorkelQuality: String
        val windAdvice: String

        when (windType) {
            WindType.LEVANTE -> {
                if (isEastFacing) {
                    isProtected = false
                    if (windSpeed >= 28 || waveHeight >= 1.4f) {
                        flag = FlagColor.RED
                        summary = "Oleaje fuerte y corriente de Levante directa. Baño muy peligroso."
                        snorkelQuality = "Desaconsejado (Mucha turbidez y resaca)"
                        windAdvice = "El viento de Levante golpea de frente. Recomendamos cambiar a calas orientadas al Oeste como Mónsul o Cala Rajá."
                    } else if (windSpeed >= 15 || waveHeight >= 0.7f) {
                        flag = FlagColor.YELLOW
                        summary = "Marejadilla / Oleaje moderado por Levante. Precaución en la orilla."
                        snorkelQuality = "Regular / Visibilidad reducida (<5m)"
                        windAdvice = "Sopla Levante en esta playa. El mar está picado."
                    } else {
                        flag = FlagColor.GREEN
                        summary = "Levante suave. Baño permitido con precaución."
                        snorkelQuality = "Buena (>8m)"
                        windAdvice = "Brisa de Levante que apenas levanta ola."
                    }
                } else if (isSouthEastFacing) {
                    isProtected = false
                    if (windSpeed >= 22) {
                        flag = FlagColor.YELLOW
                        summary = "Viento de Levante lateral. Precaución al nadar."
                        snorkelQuality = "Media"
                        windAdvice = "Recibe viento lateral de Levante."
                    } else {
                        flag = FlagColor.GREEN
                        summary = "Condiciones buenas para el baño."
                        snorkelQuality = "Buena"
                        windAdvice = "Viento moderado, baño agradable."
                    }
                } else {
                    // West or South facing beaches are protected from Levante!
                    isProtected = true
                    flag = FlagColor.GREEN
                    summary = "¡Playa protegida del Levante! Mar en calma y baño seguro."
                    snorkelQuality = "Excelente (>15m de visibilidad)"
                    windAdvice = "¡Elección perfecta hoy! El relieve de la sierra frena el viento de Levante."
                }
            }

            WindType.PONIENTE -> {
                if (isEastFacing || isSouthEastFacing) {
                    // East facing beaches are sheltered with Poniente (offshore wind flattens the sea!)
                    isProtected = true
                    flag = FlagColor.GREEN
                    summary = "¡Mar como una piscina! Viento terral de Poniente que allana el agua."
                    snorkelQuality = "¡Increíble! Visibilidad cristalina máxima (>20m)"
                    windAdvice = "Día ideal para visitar esta playa. El Poniente sopla de tierra a mar dejando el agua transparente."
                } else if (isWestFacing) {
                    isProtected = false
                    if (windSpeed >= 25 || waveHeight >= 1.2f) {
                        flag = FlagColor.YELLOW
                        summary = "Oleaje y viento directo de Poniente. Precaución."
                        snorkelQuality = "Media / Arena en suspensión"
                        windAdvice = "Expuesta al Poniente. Para mar en calma, recomendamos calas del Levante o Este de Cabo de Gata."
                    } else {
                        flag = FlagColor.GREEN
                        summary = "Poniente moderado, apta para el baño."
                        snorkelQuality = "Buena"
                        windAdvice = "Viento fresco de Poniente."
                    }
                } else {
                    isProtected = true
                    flag = FlagColor.GREEN
                    summary = "Condiciones estables para el baño."
                    snorkelQuality = "Buena"
                    windAdvice = "Buena protección orográfica."
                }
            }

            WindType.CALM -> {
                isProtected = true
                flag = FlagColor.GREEN
                summary = "Mar en calma total en toda la bahía. Baño y snorkel excelentes."
                snorkelQuality = "Óptima (Transparencia total)"
                windAdvice = "Ausencia de viento significativo. Todas las calas son recomendables hoy."
            }
        }

        return BathingSafetyAlert(
            flag = flag,
            summary = summary,
            isProtectedFromCurrentWind = isProtected,
            waveHeightEstimated = waveHeight,
            snorkelQualityToday = snorkelQuality,
            windAdvice = windAdvice
        )
    }
}

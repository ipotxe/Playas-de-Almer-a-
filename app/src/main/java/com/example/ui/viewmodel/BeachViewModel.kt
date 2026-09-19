package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BeachDatabase
import com.example.data.local.FavoriteBeachEntity
import com.example.data.model.*
import com.example.data.remote.MarineWeatherService
import com.example.data.repository.BeachRepository
import com.example.util.WindAlertSeverity
import com.example.util.WindSafetyAlert
import com.example.util.WindSafetyAlertHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class BeachSortOrder(val label: String) {
    RECOMMENDED_TODAY("Mejor baño hoy"),
    SNORKEL_RATING("Puntuación Snorkel"),
    NAME_AZ("Nombre (A-Z)"),
    LENGTH("Longitud")
}

data class FilterState(
    val searchQuery: String = "",
    val selectedZone: Zone? = null,
    val onlyTopSnorkel: Boolean = false,
    val onlyGreenFlagToday: Boolean = false,
    val onlyWindProtectedToday: Boolean = false,
    val onlyVirginCoves: Boolean = false,
    val onlyWithLifeguard: Boolean = false,
    val onlyNudist: Boolean = false,
    val onlyFamily: Boolean = false,
    val selectedSport: WaterSport? = null,
    val sortOrder: BeachSortOrder = BeachSortOrder.RECOMMENDED_TODAY
)

data class BeachUiState(
    val allBeaches: List<Beach> = emptyList(),
    val filteredBeaches: List<Beach> = emptyList(),
    val favorites: List<Beach> = emptyList(),
    val userBeachData: Map<String, FavoriteBeachEntity> = emptyMap(),
    val selectedForecastDay: ForecastDay = ForecastDay.TODAY,
    val multiDayMarineForecasts: Map<Zone, Map<ForecastDay, MarineForecast>> = emptyMap(),
    val marineForecasts: Map<Zone, MarineForecast> = emptyMap(),
    val isLoadingForecast: Boolean = false,
    val activeWindAlert: WindSafetyAlert? = null,
    val isWindAlertDismissed: Boolean = false,
    val isWindAlertSheetOpen: Boolean = false,
    val filterState: FilterState = FilterState(),
    val selectedBeach: Beach? = null,
    val activeDetailPhotoIndex: Int? = null,
    val activeNavTab: Int = 0 // 0: Explorar, 1: Mapa, 2: Previsión Mar, 3: Favoritos
)

class BeachViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BeachRepository

    private val _uiState = MutableStateFlow(BeachUiState())
    val uiState: StateFlow<BeachUiState> = _uiState.asStateFlow()

    init {
        val db = BeachDatabase.getDatabase(application)
        val marineService = MarineWeatherService()
        repository = BeachRepository(db.beachDao(), marineService, db.beachInfoDao())

        val beaches = repository.getAllBeaches()
        _uiState.update { it.copy(allBeaches = beaches) }

        viewModelScope.launch {
            repository.seedBeachesIfEmpty()
        }

        observeFavorites()
        refreshAllMarineForecasts()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.getFavorites().collect { favList ->
                _uiState.update { it.copy(favorites = favList) }
            }
        }

        viewModelScope.launch {
            repository.getAllUserBeachData().collect { entities ->
                val entityMap = entities.associateBy { it.beachId }
                _uiState.update { it.copy(userBeachData = entityMap) }
                applyFilters()
            }
        }
    }

    fun refreshAllMarineForecasts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingForecast = true) }
            val multiDayMap = mutableMapOf<Zone, Map<ForecastDay, MarineForecast>>()
            for (zone in Zone.values()) {
                multiDayMap[zone] = repository.fetchMultiDayMarineForecast(zone)
            }
            val activeDay = _uiState.value.selectedForecastDay
            val activeDayForecasts = Zone.values().associateWith { zone ->
                multiDayMap[zone]?.get(activeDay)
                    ?: MarineWeatherService().generateFallbackForecast(zone, activeDay)
            }
            val beaches = _uiState.value.allBeaches
            val alert = WindSafetyAlertHelper.analyzeForecastForSwimmingAlert(activeDayForecasts, beaches)

            _uiState.update {
                it.copy(
                    multiDayMarineForecasts = multiDayMap,
                    marineForecasts = activeDayForecasts,
                    isLoadingForecast = false,
                    activeWindAlert = alert,
                    isWindAlertDismissed = false
                )
            }
            applyFilters()
        }
    }

    fun selectForecastDay(day: ForecastDay) {
        val multiDay = _uiState.value.multiDayMarineForecasts
        val updatedDayForecasts = Zone.values().associateWith { zone ->
            multiDay[zone]?.get(day) ?: MarineWeatherService().generateFallbackForecast(zone, day)
        }
        val beaches = _uiState.value.allBeaches
        val alert = WindSafetyAlertHelper.analyzeForecastForSwimmingAlert(updatedDayForecasts, beaches)

        _uiState.update {
            it.copy(
                selectedForecastDay = day,
                marineForecasts = updatedDayForecasts,
                activeWindAlert = alert,
                isWindAlertDismissed = false
            )
        }
        applyFilters()
    }

    fun getForecastForZoneAndDay(zone: Zone, day: ForecastDay): MarineForecast {
        return _uiState.value.multiDayMarineForecasts[zone]?.get(day)
            ?: MarineWeatherService().generateFallbackForecast(zone, day)
    }

    fun dismissWindAlert() {
        _uiState.update { it.copy(isWindAlertDismissed = true) }
    }

    fun openWindAlertSheet() {
        _uiState.update { it.copy(isWindAlertSheetOpen = true) }
    }

    fun closeWindAlertSheet() {
        _uiState.update { it.copy(isWindAlertSheetOpen = false) }
    }

    fun simulateWindScenario(windType: WindType, severity: WindAlertSeverity) {
        val alert = WindSafetyAlertHelper.generateSimulatedAlert(
            windType = windType,
            severity = severity,
            allBeaches = _uiState.value.allBeaches
        )
        _uiState.update {
            it.copy(
                activeWindAlert = alert,
                isWindAlertDismissed = false
            )
        }
        applyFilters()
    }

    fun resetToRealAemetForecast() {
        refreshAllMarineForecasts()
    }

    fun filterOnlyShelteredBeaches() {
        _uiState.update {
            it.copy(
                filterState = it.filterState.copy(onlyWindProtectedToday = true)
            )
        }
        applyFilters()
    }

    fun selectNavTab(tabIndex: Int) {
        _uiState.update { it.copy(activeNavTab = tabIndex) }
    }

    fun selectBeach(beach: Beach?) {
        _uiState.update { it.copy(selectedBeach = beach, activeDetailPhotoIndex = null) }
    }

    fun openPhotoViewer(photoIndex: Int) {
        _uiState.update { it.copy(activeDetailPhotoIndex = photoIndex) }
    }

    fun closePhotoViewer() {
        _uiState.update { it.copy(activeDetailPhotoIndex = null) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(filterState = it.filterState.copy(searchQuery = query)) }
        applyFilters()
    }

    fun selectZone(zone: Zone?) {
        _uiState.update { it.copy(filterState = it.filterState.copy(selectedZone = zone)) }
        applyFilters()
    }

    fun toggleTopSnorkelFilter() {
        _uiState.update {
            it.copy(filterState = it.filterState.copy(onlyTopSnorkel = !it.filterState.onlyTopSnorkel))
        }
        applyFilters()
    }

    fun toggleGreenFlagFilter() {
        _uiState.update {
            it.copy(filterState = it.filterState.copy(onlyGreenFlagToday = !it.filterState.onlyGreenFlagToday))
        }
        applyFilters()
    }

    fun toggleWindProtectedFilter() {
        _uiState.update {
            it.copy(filterState = it.filterState.copy(onlyWindProtectedToday = !it.filterState.onlyWindProtectedToday))
        }
        applyFilters()
    }

    fun toggleVirginCovesFilter() {
        _uiState.update {
            it.copy(filterState = it.filterState.copy(onlyVirginCoves = !it.filterState.onlyVirginCoves))
        }
        applyFilters()
    }

    fun toggleLifeguardFilter() {
        _uiState.update {
            it.copy(filterState = it.filterState.copy(onlyWithLifeguard = !it.filterState.onlyWithLifeguard))
        }
        applyFilters()
    }

    fun toggleNudistFilter() {
        _uiState.update {
            it.copy(filterState = it.filterState.copy(onlyNudist = !it.filterState.onlyNudist))
        }
        applyFilters()
    }

    fun toggleFamilyFilter() {
        _uiState.update {
            it.copy(filterState = it.filterState.copy(onlyFamily = !it.filterState.onlyFamily))
        }
        applyFilters()
    }

    fun selectSportFilter(sport: WaterSport?) {
        val newSport = if (_uiState.value.filterState.selectedSport == sport) null else sport
        _uiState.update {
            it.copy(filterState = it.filterState.copy(selectedSport = newSport))
        }
        applyFilters()
    }

    fun setSortOrder(order: BeachSortOrder) {
        _uiState.update {
            it.copy(filterState = it.filterState.copy(sortOrder = order))
        }
        applyFilters()
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(filterState = FilterState(searchQuery = it.filterState.searchQuery))
        }
        applyFilters()
    }

    fun toggleFavorite(beachId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(beachId)
        }
    }

    fun toggleVisited(beachId: String) {
        viewModelScope.launch {
            val isCurrentlyVisited = _uiState.value.userBeachData[beachId]?.isVisited ?: false
            repository.setVisited(beachId, !isCurrentlyVisited)
        }
    }

    fun saveNotes(beachId: String, notes: String) {
        viewModelScope.launch {
            repository.saveNotes(beachId, notes)
        }
    }

    fun getBathingAlertForBeach(beach: Beach, day: ForecastDay? = null): BathingSafetyAlert {
        val targetDay = day ?: _uiState.value.selectedForecastDay
        val forecast = _uiState.value.multiDayMarineForecasts[beach.zone]?.get(targetDay)
            ?: _uiState.value.marineForecasts[beach.zone]
            ?: MarineWeatherService().generateFallbackForecast(beach.zone, targetDay)
        return repository.calculateBathingSafety(beach, forecast)
    }

    private fun applyFilters() {
        val current = _uiState.value
        val filters = current.filterState
        val query = filters.searchQuery.trim().lowercase()

        var list = current.allBeaches.filter { beach ->
            // Search text
            val matchesQuery = query.isEmpty() ||
                    beach.name.lowercase().contains(query) ||
                    beach.municipality.lowercase().contains(query) ||
                    beach.zone.displayName.lowercase().contains(query) ||
                    beach.summary.lowercase().contains(query) ||
                    beach.sandType.lowercase().contains(query) ||
                    beach.waterSports.any { it.label.lowercase().contains(query) }

            // Zone
            val matchesZone = filters.selectedZone == null || beach.zone == filters.selectedZone

            // Top snorkel
            val matchesSnorkel = !filters.onlyTopSnorkel || beach.snorkelRating >= 4.5f

            // Virgin coves
            val matchesVirgin = !filters.onlyVirginCoves || beach.services.isVirginCove

            // Lifeguard
            val matchesLifeguard = !filters.onlyWithLifeguard || beach.services.hasLifeguard

            // Nudist
            val matchesNudist = !filters.onlyNudist || beach.isNudistFriendly

            // Family
            val matchesFamily = !filters.onlyFamily || beach.isFamilyFriendly

            // Sports
            val matchesSport = filters.selectedSport == null || beach.waterSports.contains(filters.selectedSport)

            // Wind & Flag dynamic filters
            val alert = getBathingAlertForBeach(beach)
            val matchesGreenFlag = !filters.onlyGreenFlagToday || alert.flag == FlagColor.GREEN
            val matchesWindProtected = !filters.onlyWindProtectedToday || alert.isProtectedFromCurrentWind

            matchesQuery && matchesZone && matchesSnorkel && matchesVirgin &&
                    matchesLifeguard && matchesNudist && matchesFamily &&
                    matchesSport && matchesGreenFlag && matchesWindProtected
        }

        // Sorting
        list = when (filters.sortOrder) {
            BeachSortOrder.RECOMMENDED_TODAY -> list.sortedWith(
                compareByDescending<Beach> { getBathingAlertForBeach(it).isProtectedFromCurrentWind }
                    .thenBy { getBathingAlertForBeach(it).flag.ordinal }
                    .thenByDescending { it.snorkelRating }
            )
            BeachSortOrder.SNORKEL_RATING -> list.sortedByDescending { it.snorkelRating }
            BeachSortOrder.NAME_AZ -> list.sortedBy { it.name }
            BeachSortOrder.LENGTH -> list.sortedByDescending { it.lengthMeters }
        }

        _uiState.update { it.copy(filteredBeaches = list) }
    }
}

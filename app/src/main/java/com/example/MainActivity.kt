package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Beach
import com.example.ui.screens.*
import com.example.ui.theme.MarineCyan
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BeachViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BeachViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                BeachApp(viewModel = viewModel)
            }
        }
    }
}

data class NavTabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun BeachApp(viewModel: BeachViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val navItems = listOf(
        NavTabItem("Explorar", Icons.Default.BeachAccess, Icons.Outlined.BeachAccess, "tab_explore"),
        NavTabItem("Mapa", Icons.Default.Map, Icons.Outlined.Map, "tab_map"),
        NavTabItem("Estado del Mar", Icons.Default.Waves, Icons.Outlined.Waves, "tab_marine"),
        NavTabItem("Favoritas", Icons.Default.Favorite, Icons.Outlined.FavoriteBorder, "tab_favorites")
    )

    // Handle back button when beach detail is open
    BackHandler(enabled = uiState.selectedBeach != null) {
        viewModel.selectBeach(null)
    }

    Scaffold(
        bottomBar = {
            if (uiState.selectedBeach == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    windowInsets = WindowInsets.navigationBars,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    navItems.forEachIndexed { index, item ->
                        val isSelected = uiState.activeNavTab == index
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.selectNavTab(index) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        AnimatedContent(
            targetState = uiState.selectedBeach,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "ScreenTransition",
            modifier = Modifier.fillMaxSize()
        ) { selectedBeach ->
            if (selectedBeach != null) {
                val userEntry = uiState.userBeachData[selectedBeach.id]
                BeachDetailScreen(
                    beach = selectedBeach,
                    bathingAlert = viewModel.getBathingAlertForBeach(selectedBeach),
                    isFavorite = uiState.favorites.any { it.id == selectedBeach.id },
                    isVisited = userEntry?.isVisited ?: false,
                    userNotes = userEntry?.userNotes ?: "",
                    onBackClick = { viewModel.selectBeach(null) },
                    onFavoriteToggle = { viewModel.toggleFavorite(selectedBeach.id) },
                    onToggleVisited = { viewModel.toggleVisited(selectedBeach.id) },
                    onSaveNotes = { notes -> viewModel.saveNotes(selectedBeach.id, notes) }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                ) {
                    when (uiState.activeNavTab) {
                        0 -> ExploreScreen(
                            uiState = uiState,
                            getBathingAlert = { viewModel.getBathingAlertForBeach(it) },
                            onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                            onZoneSelected = { viewModel.selectZone(it) },
                            onToggleTopSnorkel = { viewModel.toggleTopSnorkelFilter() },
                            onToggleGreenFlag = { viewModel.toggleGreenFlagFilter() },
                            onToggleWindProtected = { viewModel.toggleWindProtectedFilter() },
                            onToggleVirginCoves = { viewModel.toggleVirginCovesFilter() },
                            onToggleLifeguard = { viewModel.toggleLifeguardFilter() },
                            onToggleNudist = { viewModel.toggleNudistFilter() },
                            onToggleFamily = { viewModel.toggleFamilyFilter() },
                            onSportSelected = { viewModel.selectSportFilter(it) },
                            onSortOrderChanged = { viewModel.setSortOrder(it) },
                            onResetFilters = { viewModel.resetFilters() },
                            onBeachClick = { viewModel.selectBeach(it) },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            onRefreshForecast = { viewModel.refreshAllMarineForecasts() },
                            onDismissWindAlert = { viewModel.dismissWindAlert() },
                            onOpenWindAlertSheet = { viewModel.openWindAlertSheet() },
                            onCloseWindAlertSheet = { viewModel.closeWindAlertSheet() },
                            onFilterShelteredOnly = { viewModel.filterOnlyShelteredBeaches() },
                            onSimulateWindScenario = { type, sev -> viewModel.simulateWindScenario(type, sev) },
                            onResetRealAemet = { viewModel.resetToRealAemetForecast() }
                        )

                        1 -> InteractiveMapScreen(
                            uiState = uiState,
                            getBathingAlert = { viewModel.getBathingAlertForBeach(it) },
                            onBeachSelected = { viewModel.selectBeach(it) },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            onZoneFilterChanged = { viewModel.selectZone(it) }
                        )

                        2 -> MarineForecastScreen(
                            uiState = uiState,
                            getBathingAlert = { viewModel.getBathingAlertForBeach(it) },
                            onBeachClick = { viewModel.selectBeach(it) },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            onRefresh = { viewModel.refreshAllMarineForecasts() },
                            onDismissWindAlert = { viewModel.dismissWindAlert() },
                            onOpenWindAlertSheet = { viewModel.openWindAlertSheet() },
                            onCloseWindAlertSheet = { viewModel.closeWindAlertSheet() },
                            onFilterShelteredOnly = { viewModel.filterOnlyShelteredBeaches() },
                            onSimulateWindScenario = { type, sev -> viewModel.simulateWindScenario(type, sev) },
                            onResetRealAemet = { viewModel.resetToRealAemetForecast() }
                        )

                        3 -> FavoritesScreen(
                            uiState = uiState,
                            getBathingAlert = { viewModel.getBathingAlertForBeach(it) },
                            onBeachClick = { viewModel.selectBeach(it) },
                            onFavoriteToggle = { viewModel.toggleFavorite(it) },
                            onToggleVisited = { viewModel.toggleVisited(it) },
                            onSaveNotes = { id, notes -> viewModel.saveNotes(id, notes) },
                            onExploreClick = { viewModel.selectNavTab(0) }
                        )
                    }
                }
            }
        }
    }
}

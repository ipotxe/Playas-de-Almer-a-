package com.example

import com.example.data.local.BeachDataProvider
import com.example.data.model.*
import com.example.util.WindAlertSeverity
import com.example.util.WindSafetyAlertHelper
import org.junit.Assert.*
import org.junit.Test

class WindSafetyAlertHelperTest {

    private val allBeaches = BeachDataProvider.getAllBeaches()

    @Test
    fun testLevanteWindProtectsWestBeaches() {
        // Levante blows from East -> East facing beaches are exposed, West facing are sheltered
        val isEastSheltered = WindSafetyAlertHelper.isBeachShelteredFromWind(Orientation.EAST, WindType.LEVANTE)
        val isWestSheltered = WindSafetyAlertHelper.isBeachShelteredFromWind(Orientation.WEST, WindType.LEVANTE)
        val isSouthWestSheltered = WindSafetyAlertHelper.isBeachShelteredFromWind(Orientation.SOUTH_WEST, WindType.LEVANTE)

        assertFalse(isEastSheltered)
        assertTrue(isWestSheltered)
        assertTrue(isSouthWestSheltered)
    }

    @Test
    fun testPonienteWindProtectsEastBeaches() {
        // Poniente blows from West -> West facing beaches are exposed, East facing are sheltered
        val isEastSheltered = WindSafetyAlertHelper.isBeachShelteredFromWind(Orientation.EAST, WindType.PONIENTE)
        val isWestSheltered = WindSafetyAlertHelper.isBeachShelteredFromWind(Orientation.WEST, WindType.PONIENTE)
        val isSouthEastSheltered = WindSafetyAlertHelper.isBeachShelteredFromWind(Orientation.SOUTH_EAST, WindType.PONIENTE)

        assertTrue(isEastSheltered)
        assertFalse(isWestSheltered)
        assertTrue(isSouthEastSheltered)
    }

    @Test
    fun testStrongLevanteTriggersDangerAlert() {
        val simulatedAlert = WindSafetyAlertHelper.generateSimulatedAlert(
            windType = WindType.LEVANTE,
            severity = WindAlertSeverity.STRONG_DANGER,
            allBeaches = allBeaches
        )

        assertEquals(WindAlertSeverity.STRONG_DANGER, simulatedAlert.severity)
        assertEquals(WindType.LEVANTE, simulatedAlert.windType)
        assertTrue(simulatedAlert.windSpeedKmh >= WindSafetyAlertHelper.THRESHOLD_STRONG_WIND_KMH)
        assertTrue(simulatedAlert.exposedBeachIds.isNotEmpty())
        assertTrue(simulatedAlert.shelteredBeachIds.isNotEmpty())
        assertTrue(simulatedAlert.title.contains("Levante"))
    }

    @Test
    fun testForecastAnalysisWithMildConditionsReturnsNoAlert() {
        val calmForecast = mapOf(
            Zone.CABO_DE_GATA to MarineForecast(
                zone = Zone.CABO_DE_GATA,
                currentWindType = WindType.CALM,
                windSpeedKmh = 10,
                windDirectionDegrees = 90f,
                windDirectionCardinal = "E",
                waveHeightMeters = 0.3f,
                wavePeriodSeconds = 4f,
                waterTemperatureCelsius = 25f,
                airTemperatureCelsius = 28f,
                uvIndex = 7,
                weatherCondition = "Despejado",
                tideStatus = "Media",
                generalAdvice = "Calma",
                lastUpdatedText = "Hoy"
            )
        )

        val alert = WindSafetyAlertHelper.analyzeForecastForSwimmingAlert(calmForecast, allBeaches)
        assertNull(alert)
    }
}

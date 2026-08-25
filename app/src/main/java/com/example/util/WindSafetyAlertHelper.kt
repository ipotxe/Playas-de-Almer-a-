package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.*

enum class WindAlertSeverity(val displayName: String, val level: Int) {
    SAFE("Condiciones Seguras", 0),
    MODERATE_WARNING("Aviso Amarillo: Precaución para el Baño", 1),
    STRONG_DANGER("Alerta Roja: Baño Peligroso por Fuerte Viento", 2)
}

data class WindSafetyAlert(
    val id: String,
    val title: String,
    val headline: String,
    val severity: WindAlertSeverity,
    val windType: WindType,
    val windSpeedKmh: Int,
    val waveHeightMeters: Float,
    val affectedZone: Zone,
    val cardinalDirection: String,
    val swimmingImpactDescription: String,
    val safetyRecommendations: List<String>,
    val exposedBeachIds: List<String>,
    val shelteredBeachIds: List<String>,
    val isSimulated: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

object WindSafetyAlertHelper {

    const val CHANNEL_ID = "wind_swimming_alerts"
    private const val NOTIFICATION_ID = 1001

    // Thresholds for wind affecting swimming in Almería
    const val THRESHOLD_MODERATE_WIND_KMH = 20
    const val THRESHOLD_STRONG_WIND_KMH = 30
    const val THRESHOLD_MODERATE_WAVE_M = 0.8f
    const val THRESHOLD_HIGH_WAVE_M = 1.4f

    /**
     * Analyzes AEMET / Marine forecast data across all zones and returns an active
     * WindSafetyAlert if wind conditions (Levante or Poniente) are strong enough
     * to compromise bathing safety or generate strong waves/currents.
     */
    fun analyzeForecastForSwimmingAlert(
        forecasts: Map<Zone, MarineForecast>,
        allBeaches: List<Beach>
    ): WindSafetyAlert? {
        if (forecasts.isEmpty()) return null

        // Find the most severe forecast among zones
        val primaryForecast = forecasts[Zone.CABO_DE_GATA] 
            ?: forecasts.values.maxByOrNull { it.windSpeedKmh } 
            ?: return null

        val maxSpeed = forecasts.values.maxOfOrNull { it.windSpeedKmh } ?: primaryForecast.windSpeedKmh
        val maxWave = forecasts.values.maxOfOrNull { it.waveHeightMeters } ?: primaryForecast.waveHeightMeters
        val windType = primaryForecast.currentWindType

        // If wind speed and waves are mild, no critical alert
        if (maxSpeed < THRESHOLD_MODERATE_WIND_KMH && maxWave < THRESHOLD_MODERATE_WAVE_M) {
            return null
        }

        val severity = if (maxSpeed >= THRESHOLD_STRONG_WIND_KMH || maxWave >= THRESHOLD_HIGH_WAVE_M) {
            WindAlertSeverity.STRONG_DANGER
        } else {
            WindAlertSeverity.MODERATE_WARNING
        }

        val (title, headline, swimmingImpact, recommendations) = when (windType) {
            WindType.LEVANTE -> buildLevanteAlertContent(maxSpeed, maxWave, severity)
            WindType.PONIENTE -> buildPonienteAlertContent(maxSpeed, maxWave, severity)
            WindType.CALM -> buildGeneralWindAlertContent(maxSpeed, maxWave, severity)
        }

        // Identify exposed vs sheltered beaches
        val exposedBeaches = mutableListOf<String>()
        val shelteredBeaches = mutableListOf<String>()

        for (beach in allBeaches) {
            val isSheltered = isBeachShelteredFromWind(beach.orientation, windType)
            if (isSheltered) {
                shelteredBeaches.add(beach.id)
            } else {
                exposedBeaches.add(beach.id)
            }
        }

        return WindSafetyAlert(
            id = "alert_${windType.name.lowercase()}_${severity.name.lowercase()}_${System.currentTimeMillis() / 3600000}",
            title = title,
            headline = headline,
            severity = severity,
            windType = windType,
            windSpeedKmh = maxSpeed,
            waveHeightMeters = maxWave,
            affectedZone = primaryForecast.zone,
            cardinalDirection = primaryForecast.windDirectionCardinal,
            swimmingImpactDescription = swimmingImpact,
            safetyRecommendations = recommendations,
            exposedBeachIds = exposedBeaches,
            shelteredBeachIds = shelteredBeaches,
            isSimulated = false
        )
    }

    private fun buildLevanteAlertContent(
        speedKmh: Int,
        wavesM: Float,
        severity: WindAlertSeverity
    ): Tuple4<String, String, String, List<String>> {
        val isDanger = severity == WindAlertSeverity.STRONG_DANGER
        val title = if (isDanger) {
            "🚨 Alerta de Levante Fuerte (${speedKmh} km/h)"
        } else {
            "⚠️ Aviso de Levante Moderado (${speedKmh} km/h)"
        }

        val headline = if (isDanger) {
            "Oleaje violento y resaca peligrosa en calas y playas orientadas al Este."
        } else {
            "Mar picado y corrientes en la costa Este de Cabo de Gata y Levante."
        }

        val swimmingImpact = if (isDanger) {
            "El viento de Levante sopla con fuerza ($speedKmh km/h, olas de hasta ${String.format("%.1f", wavesM)}m) impactando de frente en playas abiertas al Este como Los Muertos, Las Negras, El Playazo y Agua Amarga. Hay alto riesgo de corrientes de resaca y golpes de mar. El baño está totalmente desaconsejado en playas expuestas."
        } else {
            "El viento de Levante ($speedKmh km/h) levanta marejadilla y corrientes en la costa Este. Se recomienda precaución extrema con niños y personas con poca experiencia en el agua. Visibilidad de snorkel reducida por arena y posidonia en suspensión."
        }

        val recommendations = listOf(
            "Acude a playas protegidas orientadas al Oeste/Sur (Mónsul, Genoveses, Cala Rajá, Zapillo).",
            "Evita el baño en Playa de los Muertos y calas abiertas al Este (resaca peligrosa).",
            "No utilices colchonetas inflables o flotadores ligeros (pueden ser arrastrados mar adentro).",
            "Respeta escrupulosamente las banderas de los socorristas (Amarilla / Roja)."
        )

        return Tuple4(title, headline, swimmingImpact, recommendations)
    }

    private fun buildPonienteAlertContent(
        speedKmh: Int,
        wavesM: Float,
        severity: WindAlertSeverity
    ): Tuple4<String, String, String, List<String>> {
        val isDanger = severity == WindAlertSeverity.STRONG_DANGER
        val title = if (isDanger) {
            "🚨 Alerta de Poniente Fuerte (${speedKmh} km/h)"
        } else {
            "⚠️ Aviso de Poniente Moderado (${speedKmh} km/h)"
        }

        val headline = if (isDanger) {
            "Fuerte oleaje en Poniente y Bahía. ¡Mar como un plato en el litoral Este!"
        } else {
            "Viento terral en Calas de Levante. Precaución en costa Oeste y Poniente."
        }

        val swimmingImpact = if (isDanger) {
            "El Poniente sopla a $speedKmh km/h creando mar gruesa en Almerimar, Roquetas y playas abiertas al Oeste. Sin embargo, en el Parque Natural (Los Muertos, Cala de Enmedio, San José oriental) actúa como viento terral que allana el mar como una piscina cristalina, aunque debes tener cuidado de no alejarte de la orilla."
        } else {
            "Viento de Poniente ($speedKmh km/h). Condiciones excelentes para baño y snorkel en las calas del Este. Marejadilla y viento directo en playas orientadas al Oeste."
        }

        val recommendations = listOf(
            "Aprovecha para visitar calas del Este (Los Muertos, Cala de Enmedio, Agua Amarga) donde el agua estará cristalina.",
            "Cuidado con el viento terral en playas protegidas: empuja hacia dentro del mar.",
            "Evita playas abiertas al Oeste/Suroeste si buscas aguas tranquilas sin oleaje.",
            "Condiciones perfectas para snorkel en las calas resguardadas del Parque Natural."
        )

        return Tuple4(title, headline, swimmingImpact, recommendations)
    }

    private fun buildGeneralWindAlertContent(
        speedKmh: Int,
        wavesM: Float,
        severity: WindAlertSeverity
    ): Tuple4<String, String, String, List<String>> {
        val title = "⚠️ Aviso de Viento Costero (${speedKmh} km/h)"
        val headline = "Viento moderado a fuerte afectando las condiciones de baño."
        val swimmingImpact = "Rachas de viento de $speedKmh km/h con oleaje de ${String.format("%.1f", wavesM)}m. Evalúa la orientación de la cala antes de meterte al agua."
        val recommendations = listOf(
            "Consulta el estado de las banderas antes de entrar al agua.",
            "Elige calas protegidas por acantilados.",
            "Mantén vigilancia continua sobre niños y bañistas."
        )
        return Tuple4(title, headline, swimmingImpact, recommendations)
    }

    /**
     * Determines whether a beach orientation is shielded from the given wind type.
     */
    fun isBeachShelteredFromWind(orientation: Orientation, windType: WindType): Boolean {
        return when (windType) {
            WindType.LEVANTE -> {
                // East wind: Sheltered beaches face West, South-West, or South
                orientation == Orientation.WEST || 
                orientation == Orientation.SOUTH_WEST || 
                orientation == Orientation.SOUTH || 
                orientation == Orientation.NORTH_WEST
            }
            WindType.PONIENTE -> {
                // West wind: Sheltered beaches face East, South-East, North-East
                orientation == Orientation.EAST || 
                orientation == Orientation.SOUTH_EAST || 
                orientation == Orientation.NORTH_EAST
            }
            WindType.CALM -> true
        }
    }

    /**
     * Creates and triggers a simulated alert for testing purposes (Levante / Poniente / Safe).
     */
    fun generateSimulatedAlert(
        windType: WindType,
        severity: WindAlertSeverity,
        allBeaches: List<Beach>
    ): WindSafetyAlert {
        val speed = when (severity) {
            WindAlertSeverity.SAFE -> 8
            WindAlertSeverity.MODERATE_WARNING -> 24
            WindAlertSeverity.STRONG_DANGER -> 38
        }
        val waves = when (severity) {
            WindAlertSeverity.SAFE -> 0.3f
            WindAlertSeverity.MODERATE_WARNING -> 0.9f
            WindAlertSeverity.STRONG_DANGER -> 1.8f
        }

        val (title, headline, swimmingImpact, recommendations) = when (windType) {
            WindType.LEVANTE -> buildLevanteAlertContent(speed, waves, severity)
            WindType.PONIENTE -> buildPonienteAlertContent(speed, waves, severity)
            WindType.CALM -> buildGeneralWindAlertContent(speed, waves, severity)
        }

        val exposedBeaches = mutableListOf<String>()
        val shelteredBeaches = mutableListOf<String>()

        for (beach in allBeaches) {
            if (isBeachShelteredFromWind(beach.orientation, windType)) {
                shelteredBeaches.add(beach.id)
            } else {
                exposedBeaches.add(beach.id)
            }
        }

        return WindSafetyAlert(
            id = "sim_${windType.name}_${severity.name}",
            title = title,
            headline = headline,
            severity = severity,
            windType = windType,
            windSpeedKmh = speed,
            waveHeightMeters = waves,
            affectedZone = Zone.CABO_DE_GATA,
            cardinalDirection = if (windType == WindType.LEVANTE) "E (Levante 85°)" else "W (Poniente 265°)",
            swimmingImpactDescription = swimmingImpact,
            safetyRecommendations = recommendations,
            exposedBeachIds = exposedBeaches,
            shelteredBeachIds = shelteredBeaches,
            isSimulated = true
        )
    }

    /**
     * Posts a native Android System Notification when a dangerous wind alert is active.
     */
    fun sendWindAlertNotification(context: Context, alert: WindSafetyAlert) {
        createNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val bigText = "${alert.headline}\n\n${alert.swimmingImpactDescription}\n\n💡 Playas protegidas recomendadas disponibles en la app."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(alert.title)
            .setContentText(alert.headline)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(
                if (alert.severity == WindAlertSeverity.STRONG_DANGER) 
                    NotificationCompat.PRIORITY_HIGH 
                else 
                    NotificationCompat.PRIORITY_DEFAULT
            )
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Permission not granted on Android 13+
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Alertas de Viento y Baño en Almería",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones automáticas de seguridad cuando el Levante o Poniente afectan al baño en las playas"
                enableVibration(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

private data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

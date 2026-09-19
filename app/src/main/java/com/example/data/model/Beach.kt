package com.example.data.model

enum class Zone(val displayName: String, val description: String) {
    CABO_DE_GATA("Cabo de Gata - Níjar", "Parque Natural volcánico con calas vírgenes y fondos de posidonia"),
    LEVANTE_ALMERIENSE("Levante Almeriense", "Playas amplias y calas entre Carboneras, Mojácar, Garrucha, Vera y Pulpí"),
    ALMERIA_CAPITAL("Bahía de Almería", "Playas urbanas y costeras desde El Zapillo hasta Las Salinas y Cabo de Gata"),
    PONIENTE_ALMERIENSE("Poniente Almeriense", "Aguadulce, Roquetas de Mar, Almerimar y costa oeste")
}

enum class Orientation(val label: String, val degrees: Float, val cardinal: String) {
    NORTH("Norte", 0f, "N"),
    NORTH_EAST("Noreste", 45f, "NE"),
    EAST("Este (Levante)", 90f, "E"),
    SOUTH_EAST("Sureste", 135f, "SE"),
    SOUTH("Sur", 180f, "S"),
    SOUTH_WEST("Suroeste", 225f, "SW"),
    WEST("Oeste (Poniente)", 270f, "W"),
    NORTH_WEST("Noroeste", 315f, "NW")
}

enum class WindType(val label: String, val shortName: String, val description: String) {
    LEVANTE("Levante", "Levante (E / NE / SE)", "Viento húmedo que levanta oleaje en playas orientadas al Este"),
    PONIENTE("Poniente", "Poniente (W / SW / NW)", "Viento seco y terral que deja en calma las calas orientadas al Este"),
    CALM("Calma / Brisa Suave", "Calma", "Mar como un plato, condiciones ideales en casi toda la costa")
}

enum class AccessDifficulty(val label: String) {
    EASY("Acceso fácil en coche / a pie"),
    MODERATE("Sendero a pie 10-20 min"),
    HARD("Sendero escarpado 20-45 min"),
    BOAT_KAYAK_ONLY("Acceso por mar / kayak o sendero largo")
}

enum class WaterSport(val label: String, val iconName: String) {
    SNORKEL("Snorkel", "snorkel"),
    DIVING("Buceo con botella", "diving"),
    KAYAK("Kayak / Travesía", "kayak"),
    PADDLE_SURF("Paddle Surf (SUP)", "sup"),
    WINDSURF_KITE("Windsurf / Kitesurf", "windsurf"),
    SWIMMING("Aguas Abiertas", "swimming")
}

enum class FlagColor(val label: String, val colorHex: Long) {
    GREEN("Bandera Verde (Baño Seguro)", 0xFF2E7D32),
    YELLOW("Bandera Amarilla (Precaución)", 0xFFF57F17),
    RED("Bandera Roja (Baño Prohibido / Peligro)", 0xFFC62828)
}

data class BeachPhoto(
    val title: String,
    val description: String,
    val drawableResId: Int? = null,
    val imageUrl: String? = null
)

data class BeachServices(
    val hasLifeguard: Boolean = false,
    val hasChiringuito: Boolean = false,
    val hasShowers: Boolean = false,
    val hasToilets: Boolean = false,
    val hasDisabledAccess: Boolean = false,
    val hasSunbedRental: Boolean = false,
    val isVirginCove: Boolean = true
)

data class Beach(
    val id: String,
    val name: String,
    val zone: Zone,
    val municipality: String,
    val summary: String,
    val description: String,
    val sandType: String,
    val lengthMeters: Int,
    val widthMeters: Int,
    val orientation: Orientation,
    val latitude: Double,
    val longitude: Double,
    val isNaturalPark: Boolean,
    val isNudistFriendly: Boolean,
    val isPetFriendly: Boolean,
    val isFamilyFriendly: Boolean,
    val accessDifficulty: AccessDifficulty,
    val parkingInfo: String,
    val services: BeachServices,
    val snorkelRating: Float, // 1.0 to 5.0
    val snorkelDescription: String,
    val marineLifeHighlights: List<String>,
    val waterSports: List<WaterSport>,
    val photos: List<BeachPhoto>,
    val mainPhotoResId: Int? = null,
    val mainPhotoUrl: String = ""
)

enum class ForecastDay(
    val id: String,
    val title: String,
    val shortTitle: String,
    val dayOffset: Int
) {
    TODAY("today", "Hoy", "Hoy", 0),
    TOMORROW("tomorrow", "Mañana", "Mañana", 1),
    DAY_AFTER_TOMORROW("day_after", "Pasado mañana", "Pasado mañ.", 2);

    fun getDisplayDate(): String {
        val cal = java.util.Calendar.getInstance()
        cal.add(java.util.Calendar.DAY_OF_YEAR, dayOffset)
        val dayOfMonth = cal.get(java.util.Calendar.DAY_OF_MONTH)
        val monthStr = when (cal.get(java.util.Calendar.MONTH)) {
            0 -> "Ene"
            1 -> "Feb"
            2 -> "Mar"
            3 -> "Abr"
            4 -> "May"
            5 -> "Jun"
            6 -> "Jul"
            7 -> "Ago"
            8 -> "Sep"
            9 -> "Oct"
            10 -> "Nov"
            else -> "Dic"
        }
        return when (this) {
            TODAY -> "Hoy ($dayOfMonth $monthStr)"
            TOMORROW -> "Mañana ($dayOfMonth $monthStr)"
            DAY_AFTER_TOMORROW -> "Pasado mañ. ($dayOfMonth $monthStr)"
        }
    }
}

data class BathingSafetyAlert(
    val flag: FlagColor,
    val summary: String,
    val isProtectedFromCurrentWind: Boolean,
    val waveHeightEstimated: Float,
    val snorkelQualityToday: String,
    val windAdvice: String,
    val day: ForecastDay = ForecastDay.TODAY
)

data class MarineForecast(
    val zone: Zone,
    val currentWindType: WindType,
    val windSpeedKmh: Int,
    val windDirectionDegrees: Float,
    val windDirectionCardinal: String,
    val waveHeightMeters: Float,
    val wavePeriodSeconds: Float,
    val waterTemperatureCelsius: Float,
    val airTemperatureCelsius: Float,
    val uvIndex: Int,
    val weatherCondition: String,
    val tideStatus: String,
    val generalAdvice: String,
    val lastUpdatedText: String,
    val day: ForecastDay = ForecastDay.TODAY
)

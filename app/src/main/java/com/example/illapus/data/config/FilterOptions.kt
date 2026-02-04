package com.example.illapus.data.config

object FilterOptions {

    val locationOptions = listOf(
        "Quito",
        "Guayaquil",
        "Cuenca",
        "Ambato",
        "Manta",
        "Portoviejo",
        "Machala",
        "Santo Domingo",
        "Ibarra",
        "Riobamba"
    )

    val activityTypes = listOf(
        "CULTURA",
        "AVENTURA",
        "GASTRONOMIA",
        "NATURALEZA",
        "DEPORTES",
        "RELAX",
        "TURISMO",
        "EDUCATIVO"
    )

    // Configuración de rangos para sliders
    object PriceRange {
        const val MIN_PRICE = 0.0
        const val MAX_PRICE = 500.0
    }

    object PersonRange {
        const val MIN_PERSONS = 0
        const val MAX_PERSONS = 20
    }

    object RadiusRange {
        const val MIN_RADIUS = 0.0
        const val MAX_RADIUS = 100.0
    }
}

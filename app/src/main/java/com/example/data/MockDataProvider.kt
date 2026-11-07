package com.example.data

import com.example.model.*

object MockDataProvider {

    val initialFamilyMembers = listOf(
        FamilyMember(
            id = "user_me",
            name = "Daniel (Tú)",
            role = FamilyRole.PAPA,
            avatarEmoji = "👨‍💼",
            avatarBgColorHex = 0xFF0284C7,
            vehicleType = VehicleType.AUTO,
            phoneNumber = "+34 611 222 333",
            location = GeoPoint(lat = 4.6530, lng = -74.0835, streetName = "Av. Libertador #104"),
            heading = 45f,
            currentSpeedKmH = 54,
            batteryLevel = 91,
            isMoving = true,
            statusText = "Conduciendo a casa",
            destination = "Casa (Hogar)",
            etaMinutes = 18,
            lastSeenMinutesAgo = 0,
            isOnline = true
        ),
        FamilyMember(
            id = "member_wife",
            name = "María",
            role = FamilyRole.ESPOSA,
            avatarEmoji = "👩‍🦰",
            avatarBgColorHex = 0xFFEC4899,
            vehicleType = VehicleType.SUV,
            phoneNumber = "+34 612 889 123",
            location = GeoPoint(lat = 4.6710, lng = -74.0720, streetName = "Av. Las Palmas con Calle 85"),
            heading = 135f,
            currentSpeedKmH = 42,
            batteryLevel = 84,
            isMoving = true,
            statusText = "Saliendo del supermercado",
            destination = "Casa (Hogar)",
            etaMinutes = 22,
            lastSeenMinutesAgo = 1,
            isOnline = true
        ),
        FamilyMember(
            id = "member_son",
            name = "Lucas",
            role = FamilyRole.HIJO,
            avatarEmoji = "👦",
            avatarBgColorHex = 0xFF10B981,
            vehicleType = VehicleType.MOTO,
            phoneNumber = "+34 654 321 987",
            location = GeoPoint(lat = 4.6390, lng = -74.0950, streetName = "Calle El Rosal #42"),
            heading = 270f,
            currentSpeedKmH = 28,
            batteryLevel = 62,
            isMoving = true,
            statusText = "Saliendo de la universidad",
            destination = "Gimnasio",
            etaMinutes = 12,
            lastSeenMinutesAgo = 2,
            isOnline = true
        ),
        FamilyMember(
            id = "member_daughter",
            name = "Sofía",
            role = FamilyRole.HIJA,
            avatarEmoji = "👧",
            avatarBgColorHex = 0xFFA855F7,
            vehicleType = VehicleType.A_PIE,
            phoneNumber = "+34 678 456 123",
            location = GeoPoint(lat = 4.6620, lng = -74.0580, streetName = "Colegio Los Álamos (Patio)"),
            heading = 0f,
            currentSpeedKmH = 0,
            batteryLevel = 94,
            isMoving = false,
            statusText = "En colegio (Taller de arte)",
            destination = null,
            etaMinutes = null,
            lastSeenMinutesAgo = 0,
            isOnline = true
        )
    )

    val initialAlerts = listOf(
        TrafficAlert(
            id = "alert_1",
            type = AlertType.SPEED_CAMERA,
            location = GeoPoint(lat = 4.6580, lng = -74.0780, streetName = "Av. Libertador Km 4.2"),
            speedLimitKmH = 60,
            description = "Fotomulta activa en ambos sentidos. Límite 60 km/h.",
            reportedBy = "Comunidad Waze/FamRuta",
            upvotes = 42
        ),
        TrafficAlert(
            id = "alert_2",
            type = AlertType.SPEED_CAMERA,
            location = GeoPoint(lat = 4.6450, lng = -74.0880, streetName = "Boulevard Los Pinos #12"),
            speedLimitKmH = 50,
            description = "Cámara de fotomulta de semáforo y velocidad.",
            reportedBy = "María",
            upvotes = 18
        ),
        TrafficAlert(
            id = "alert_3",
            type = AlertType.HEAVY_TRAFFIC,
            location = GeoPoint(lat = 4.6650, lng = -74.0750, streetName = "Autopista Central Puente 80"),
            speedLimitKmH = null,
            description = "Tráfico detenido por alta afluencia. Retraso estimado +14 min.",
            reportedBy = "Carlos M.",
            upvotes = 29
        ),
        TrafficAlert(
            id = "alert_4",
            type = AlertType.POLICE,
            location = GeoPoint(lat = 4.6320, lng = -74.0890, streetName = "Av. San Martín cruce Calle 50"),
            speedLimitKmH = null,
            description = "Control de tránsito y documentación visible.",
            reportedBy = "Lucas",
            upvotes = 15
        ),
        TrafficAlert(
            id = "alert_5",
            type = AlertType.HAZARD,
            location = GeoPoint(lat = 4.6490, lng = -74.0670, streetName = "Av. Del Sol carril derecho"),
            speedLimitKmH = null,
            description = "Bache profundo señalizado con conos.",
            reportedBy = "Transporte Escolar",
            upvotes = 8
        ),
        TrafficAlert(
            id = "alert_6",
            type = AlertType.ROAD_WORK,
            location = GeoPoint(lat = 4.6730, lng = -74.0620, streetName = "Paseo de la Reforma #500"),
            speedLimitKmH = 30,
            description = "Reparación de alcantarillado, solo 1 carril habilitado.",
            reportedBy = "Alcaldía Vial",
            upvotes = 23
        )
    )

    val safeZones = listOf(
        SafeZone(
            id = "zone_home",
            name = "Casa (Hogar)",
            emoji = "🏡",
            location = GeoPoint(lat = 4.6680, lng = -74.0690, streetName = "Calle 92 #15-40"),
            radiusMeters = 200
        ),
        SafeZone(
            id = "zone_school",
            name = "Colegio Los Álamos",
            emoji = "🏫",
            location = GeoPoint(lat = 4.6620, lng = -74.0580, streetName = "Carrera 7 #110"),
            radiusMeters = 250
        ),
        SafeZone(
            id = "zone_office",
            name = "Oficina Daniel",
            emoji = "🏢",
            location = GeoPoint(lat = 4.6380, lng = -74.0920, streetName = "Torre Empresarial 4"),
            radiusMeters = 150
        ),
        SafeZone(
            id = "zone_mall",
            name = "Centro Comercial Andino",
            emoji = "🛍️",
            location = GeoPoint(lat = 4.6710, lng = -74.0720, streetName = "Zona T"),
            radiusMeters = 300
        )
    )

    val sampleMessages = listOf(
        FamilyMessage(
            id = "msg_1",
            senderId = "member_wife",
            senderName = "María",
            senderEmoji = "👩‍🦰",
            messageText = "Hola familia, ya compré la cena. Voy de regreso.",
            timeFormatted = "Hace 6 min"
        ),
        FamilyMessage(
            id = "msg_2",
            senderId = "member_son",
            senderName = "Lucas",
            senderEmoji = "👦",
            messageText = "Atentos en la Av. San Martín, hay control policial.",
            timeFormatted = "Hace 3 min"
        ),
        FamilyMessage(
            id = "msg_3",
            senderId = "user_me",
            senderName = "Daniel",
            senderEmoji = "👨‍💼",
            messageText = "Perfecto, los espero a todos en casa.",
            timeFormatted = "Hace 1 min"
        )
    )

    fun createSampleRouteTo(destination: GeoPoint, destinationName: String): TripRoute {
        return TripRoute(
            id = "route_${System.currentTimeMillis()}",
            title = "Ruta más rápida vía Av. Libertador",
            destinationName = destinationName,
            destinationLocation = destination,
            distanceKm = 6.8,
            durationMinutes = 16,
            delayMinutes = 3,
            points = listOf(
                GeoPoint(4.6530, -74.0835),
                GeoPoint(4.6560, -74.0800),
                GeoPoint(4.6590, -74.0770),
                GeoPoint(4.6640, -74.0730),
                GeoPoint(4.6680, -74.0690)
            ),
            steps = listOf(
                RouteInstruction(250, "Continúe recto por Av. Libertador", "Av. Libertador", TurnType.STRAIGHT),
                RouteInstruction(600, "¡Alerta! Fotomulta de 60 km/h en 300m", "Av. Libertador Km 4.2", TurnType.STRAIGHT),
                RouteInstruction(1200, "Gire a la derecha hacia Av. Las Palmas", "Av. Las Palmas", TurnType.RIGHT),
                RouteInstruction(850, "Gire a la izquierda en Calle 92", "Calle 92", TurnType.LEFT),
                RouteInstruction(150, "Su destino está a la derecha", destinationName, TurnType.ARRIVAL)
            )
        )
    }
}

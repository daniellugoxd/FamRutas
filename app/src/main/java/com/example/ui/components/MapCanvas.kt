package com.example.ui.components

import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun MapCanvas(
    familyMembers: List<FamilyMember>,
    trafficAlerts: List<TrafficAlert>,
    safeZones: List<SafeZone>,
    activeRoute: TripRoute?,
    selectedMember: FamilyMember?,
    isDayMode: Boolean,
    onSelectMember: (FamilyMember) -> Unit,
    onSelectAlert: (TrafficAlert) -> Unit,
    modifier: Modifier = Modifier
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }

    // Pulsing and continuous animation loop
    val infiniteTransition = rememberInfiniteTransition(label = "mapAnimations")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 18f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    // Continuous normalized phase (0..1) for cycling animations
    val animPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "animPhase"
    )

    val joyJump = abs(sin(animPhase * 2f * Math.PI.toFloat())) * 24f
    val walkBob = sin(animPhase * 4f * Math.PI.toFloat()) * 5f
    val vehicleBounce = sin(animPhase * 6f * Math.PI.toFloat()) * 2.8f
    val bikeBob = sin(animPhase * 5f * Math.PI.toFloat()) * 3.5f
    val impatientShake = sin(animPhase * 8f * Math.PI.toFloat()) * 4.5f
    val floatingHeartY = animPhase * 36f
    val floatingHeartAlpha = (1f - animPhase).coerceIn(0f, 1f)

    // Native Paints for text and emoji rendering with hardware acceleration
    val emojiPaint = remember {
        Paint().apply {
            textSize = 42f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }
    val smallEmojiPaint = remember {
        Paint().apply {
            textSize = 26f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }
    val bubbleTextPaint = remember {
        Paint().apply {
            textSize = 21f
            color = android.graphics.Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
    }
    val bubbleBgPaint = remember {
        Paint().apply {
            color = android.graphics.Color.argb(225, 15, 23, 42)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
    }
    val bubbleBorderPaint = remember {
        Paint().apply {
            color = android.graphics.Color.argb(170, 0, 165, 255)
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
            isAntiAlias = true
        }
    }

    // Center reference coordinate for relative projection
    val baseLat = 4.6550
    val baseLng = -74.0810

    // Colors according to Day / Night Mode
    val bgColor = if (isDayMode) Color(0xFFE2E8F0) else Color(0xFF0B132B)
    val parkColor = if (isDayMode) Color(0xFFC7E9C0) else Color(0xFF132A13)
    val waterColor = if (isDayMode) Color(0xFFBAE6FD) else Color(0xFF0F3057)
    val roadColor = if (isDayMode) Color(0xFFCBD5E1) else Color(0xFF172554)
    val roadBorderColor = if (isDayMode) Color(0xFF94A3B8) else Color(0xFF334155)

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
                .pointerInput(familyMembers, trafficAlerts, zoomScale, offsetX, offsetY) {
                    detectTapGestures { tapOffset ->
                        val centerX = size.width / 2f + offsetX
                        val centerY = size.height / 2f + offsetY

                        // Check tap on family members
                        val tappedMember = familyMembers.firstOrNull { member ->
                            val pos = projectToScreen(member.location, baseLat, baseLng, centerX, centerY, zoomScale)
                            (pos - tapOffset).getDistance() < 46f * density
                        }

                        if (tappedMember != null) {
                            onSelectMember(tappedMember)
                            return@detectTapGestures
                        }

                        // Check tap on alerts
                        val tappedAlert = trafficAlerts.firstOrNull { alert ->
                            val pos = projectToScreen(alert.location, baseLat, baseLng, centerX, centerY, zoomScale)
                            (pos - tapOffset).getDistance() < 36f * density
                        }

                        if (tappedAlert != null) {
                            onSelectAlert(tappedAlert)
                        }
                    }
                }
        ) {
            val centerX = size.width / 2f + offsetX
            val centerY = size.height / 2f + offsetY

            // 1. Draw Parks & Green Areas
            drawRoundRect(
                color = parkColor,
                topLeft = Offset(centerX - 420f * zoomScale, centerY - 380f * zoomScale),
                size = androidx.compose.ui.geometry.Size(320f * zoomScale, 240f * zoomScale),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(24f * zoomScale, 24f * zoomScale)
            )
            drawRoundRect(
                color = parkColor,
                topLeft = Offset(centerX + 180f * zoomScale, centerY + 120f * zoomScale),
                size = androidx.compose.ui.geometry.Size(280f * zoomScale, 200f * zoomScale),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(30f * zoomScale, 30f * zoomScale)
            )

            // 2. Draw River / Water body
            val riverPath = Path().apply {
                moveTo(centerX - 500f * zoomScale, centerY + 380f * zoomScale)
                cubicTo(
                    centerX - 200f * zoomScale, centerY + 300f * zoomScale,
                    centerX + 100f * zoomScale, centerY + 450f * zoomScale,
                    centerX + 500f * zoomScale, centerY + 350f * zoomScale
                )
            }
            drawPath(
                path = riverPath,
                color = waterColor,
                style = Stroke(width = 38f * zoomScale, cap = StrokeCap.Round)
            )

            // 3. Draw Road Network
            val roads = listOf(
                Offset(centerX, centerY - 600f * zoomScale) to Offset(centerX, centerY + 600f * zoomScale),
                Offset(centerX - 550f * zoomScale, centerY - 120f * zoomScale) to Offset(centerX + 550f * zoomScale, centerY - 80f * zoomScale),
                Offset(centerX - 400f * zoomScale, centerY + 250f * zoomScale) to Offset(centerX + 500f * zoomScale, centerY + 180f * zoomScale),
                Offset(centerX - 350f * zoomScale, centerY - 350f * zoomScale) to Offset(centerX + 450f * zoomScale, centerY - 350f * zoomScale),
                Offset(centerX - 250f * zoomScale, centerY - 500f * zoomScale) to Offset(centerX - 250f * zoomScale, centerY + 500f * zoomScale),
                Offset(centerX + 260f * zoomScale, centerY - 500f * zoomScale) to Offset(centerX + 260f * zoomScale, centerY + 500f * zoomScale),
                Offset(centerX - 480f * zoomScale, centerY + 60f * zoomScale) to Offset(centerX + 480f * zoomScale, centerY + 60f * zoomScale)
            )

            roads.forEach { (start, end) ->
                drawLine(
                    color = roadBorderColor,
                    start = start,
                    end = end,
                    strokeWidth = 26f * zoomScale,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = roadColor,
                    start = start,
                    end = end,
                    strokeWidth = 20f * zoomScale,
                    cap = StrokeCap.Round
                )
            }

            // Central Roundabout
            drawCircle(
                color = roadBorderColor,
                radius = 54f * zoomScale,
                center = Offset(centerX, centerY - 100f * zoomScale),
                style = Stroke(width = 24f * zoomScale)
            )
            drawCircle(
                color = roadColor,
                radius = 54f * zoomScale,
                center = Offset(centerX, centerY - 100f * zoomScale),
                style = Stroke(width = 18f * zoomScale)
            )
            drawCircle(
                color = parkColor,
                radius = 36f * zoomScale,
                center = Offset(centerX, centerY - 100f * zoomScale)
            )

            // 4. Traffic Flow Heatmap Lines
            drawLine(
                color = TrafficRed.copy(alpha = 0.85f),
                start = Offset(centerX, centerY - 250f * zoomScale),
                end = Offset(centerX, centerY + 80f * zoomScale),
                strokeWidth = 6f * zoomScale,
                cap = StrokeCap.Round
            )
            drawLine(
                color = TrafficOrange.copy(alpha = 0.85f),
                start = Offset(centerX - 150f * zoomScale, centerY + 230f * zoomScale),
                end = Offset(centerX + 220f * zoomScale, centerY + 200f * zoomScale),
                strokeWidth = 6f * zoomScale,
                cap = StrokeCap.Round
            )
            drawLine(
                color = TrafficGreen.copy(alpha = 0.75f),
                start = Offset(centerX - 450f * zoomScale, centerY - 120f * zoomScale),
                end = Offset(centerX - 100f * zoomScale, centerY - 110f * zoomScale),
                strokeWidth = 6f * zoomScale,
                cap = StrokeCap.Round
            )

            // 5. Active Navigation Route Line
            if (activeRoute != null && activeRoute.points.isNotEmpty()) {
                val routeOffsets = activeRoute.points.map { pt ->
                    projectToScreen(pt, baseLat, baseLng, centerX, centerY, zoomScale)
                }

                val routePath = Path().apply {
                    moveTo(routeOffsets.first().x, routeOffsets.first().y)
                    for (i in 1 until routeOffsets.size) {
                        lineTo(routeOffsets[i].x, routeOffsets[i].y)
                    }
                }

                drawPath(
                    path = routePath,
                    color = WazeCyanPrimary.copy(alpha = 0.35f),
                    style = Stroke(width = 24f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                drawPath(
                    path = routePath,
                    color = WazeCyanPrimary,
                    style = Stroke(width = 12f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                val destOffset = routeOffsets.last()
                drawCircle(color = TrafficRed, radius = 14f * zoomScale, center = destOffset)
                drawCircle(color = Color.White, radius = 6f * zoomScale, center = destOffset)
            }

            // 6. Draw Safe Zones
            safeZones.forEach { zone ->
                val pos = projectToScreen(zone.location, baseLat, baseLng, centerX, centerY, zoomScale)
                drawCircle(
                    color = WazeCyanPrimary.copy(alpha = 0.12f),
                    radius = (zone.radiusMeters / 6f) * zoomScale,
                    center = pos
                )
                drawCircle(
                    color = WazeCyanPrimary.copy(alpha = 0.4f),
                    radius = (zone.radiusMeters / 6f) * zoomScale,
                    center = pos,
                    style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f)))
                )
            }

            // 7. Draw Traffic Alerts (Fotomultas)
            trafficAlerts.forEach { alert ->
                val pos = projectToScreen(alert.location, baseLat, baseLng, centerX, centerY, zoomScale)

                when (alert.type) {
                    AlertType.SPEED_CAMERA -> {
                        drawCircle(color = SpeedCameraRed.copy(alpha = pulseAlpha), radius = pulseRadius * zoomScale, center = pos)
                        drawCircle(color = Color.White, radius = 18f * zoomScale, center = pos)
                        drawCircle(color = SpeedCameraRed, radius = 16f * zoomScale, center = pos)
                        drawCircle(color = Color.White, radius = 12f * zoomScale, center = pos)
                    }
                    AlertType.POLICE -> {
                        drawCircle(color = PoliceBlue, radius = 15f * zoomScale, center = pos)
                        drawCircle(color = Color.White, radius = 5f * zoomScale, center = pos)
                    }
                    AlertType.HEAVY_TRAFFIC -> {
                        drawCircle(color = TrafficOrange, radius = 14f * zoomScale, center = pos)
                        drawCircle(color = Color.White, radius = 4f * zoomScale, center = pos)
                    }
                    else -> {
                        drawCircle(color = HazardYellow, radius = 14f * zoomScale, center = pos)
                        drawCircle(color = Color.Black, radius = 4f * zoomScale, center = pos)
                    }
                }
            }

            // 8. Draw Family Members with RICH Dynamic Vehicle, Meeting & Waiting Animations!
            familyMembers.forEach { member ->
                val basePos = projectToScreen(member.location, baseLat, baseLng, centerX, centerY, zoomScale)
                val isSelected = selectedMember?.id == member.id
                val memberColor = Color(member.avatarBgColorHex)

                // Compute dynamic vertical & horizontal offsets based on animationState
                var animOffsetY = 0f
                var animOffsetX = 0f

                when (member.animationState) {
                    AvatarAnimationState.JOY_MEETING -> {
                        // High joyful bouncing / jumping together!
                        animOffsetY = -joyJump * zoomScale
                    }
                    AvatarAnimationState.JOY_ROUTE_EN_CAMINO -> {
                        // Forward eager bounce
                        animOffsetY = -(joyJump * 0.65f) * zoomScale
                    }
                    AvatarAnimationState.WAITING_IMPATIENT -> {
                        // Impatient nervous vibration / foot tapping
                        animOffsetX = impatientShake * zoomScale
                    }
                    AvatarAnimationState.WAITING_HAPPY -> {
                        // Relaxed friendly sway
                        animOffsetX = sin(animPhase * 2f * Math.PI.toFloat()) * 3f * zoomScale
                    }
                    AvatarAnimationState.MOVING_WALK -> {
                        // Walking bobbing rhythm
                        animOffsetY = -abs(walkBob) * zoomScale
                    }
                    AvatarAnimationState.MOVING_AUTO, AvatarAnimationState.MOVING_BUS -> {
                        // Vehicle engine & suspension vibration
                        animOffsetY = vehicleBounce * zoomScale
                    }
                    AvatarAnimationState.MOVING_BIKE -> {
                        // Pedaling cadence
                        animOffsetY = bikeBob * zoomScale
                    }
                    AvatarAnimationState.MOVING_MOTO -> {
                        // Motorcycle tilt & bounce
                        animOffsetY = vehicleBounce * 1.3f * zoomScale
                    }
                    AvatarAnimationState.NORMAL -> {
                        animOffsetY = 0f
                    }
                }

                val currentPos = Offset(basePos.x + animOffsetX, basePos.y + animOffsetY)

                // Draw Smoke puffs behind moving vehicles
                if (member.isMoving && (member.vehicleType == VehicleType.AUTO || member.vehicleType == VehicleType.SUV || member.vehicleType == VehicleType.BUS || member.vehicleType == VehicleType.MOTO)) {
                    val smokeOffset = Offset(
                        basePos.x - cos(Math.toRadians(member.heading.toDouble())).toFloat() * 26f * zoomScale,
                        basePos.y + sin(Math.toRadians(member.heading.toDouble())).toFloat() * 26f * zoomScale
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = pulseAlpha * 0.5f),
                        radius = (8f + pulseRadius * 0.35f) * zoomScale,
                        center = smokeOffset
                    )
                }

                // If Joyful Meeting (Abrazo o Salto de Alegría): Render glowing golden hearts aura
                if (member.animationState == AvatarAnimationState.JOY_MEETING) {
                    drawCircle(
                        color = Color(0xFFFFD700).copy(alpha = 0.4f),
                        radius = (34f + pulseRadius * 0.2f) * zoomScale,
                        center = currentPos
                    )
                    drawCircle(
                        color = Color(0xFFFF69B4).copy(alpha = 0.5f),
                        radius = (28f) * zoomScale,
                        center = currentPos,
                        style = Stroke(width = 3f * zoomScale)
                    )

                    // Draw floating rising hearts
                    drawContext.canvas.nativeCanvas.drawText(
                        "❤️",
                        currentPos.x - 18f * zoomScale,
                        currentPos.y - (38f + floatingHeartY) * zoomScale,
                        smallEmojiPaint.apply { alpha = (floatingHeartAlpha * 255).toInt() }
                    )
                    drawContext.canvas.nativeCanvas.drawText(
                        "✨",
                        currentPos.x + 18f * zoomScale,
                        currentPos.y - (44f + floatingHeartY) * zoomScale,
                        smallEmojiPaint.apply { alpha = (floatingHeartAlpha * 255).toInt() }
                    )
                }

                // If Impatient Waiting (Desespero por demora): Render pulsing red alert aura
                if (member.animationState == AvatarAnimationState.WAITING_IMPATIENT) {
                    drawCircle(
                        color = SpeedCameraRed.copy(alpha = pulseAlpha * 0.8f),
                        radius = (32f + pulseRadius * 0.25f) * zoomScale,
                        center = currentPos
                    )
                    drawCircle(
                        color = TrafficOrange,
                        radius = 26f * zoomScale,
                        center = currentPos,
                        style = Stroke(width = 3f * zoomScale)
                    )
                }

                // Moving radar pulse for regular movements
                if (member.isMoving) {
                    drawCircle(
                        color = memberColor.copy(alpha = pulseAlpha * 0.6f),
                        radius = (pulseRadius + 10f) * zoomScale,
                        center = currentPos
                    )
                }

                // Selected highlight circle
                if (isSelected) {
                    drawCircle(
                        color = Color.White,
                        radius = 29f * zoomScale,
                        center = currentPos,
                        style = Stroke(width = 4f * zoomScale)
                    )
                }

                // Ground drop shadow
                drawCircle(
                    color = Color.Black.copy(alpha = 0.45f),
                    radius = 22f * zoomScale,
                    center = Offset(basePos.x, basePos.y + 6f * zoomScale)
                )

                // Avatar Main Pin Circle
                drawCircle(
                    color = memberColor,
                    radius = 22f * zoomScale,
                    center = currentPos
                )
                drawCircle(
                    color = Color.White,
                    radius = 22f * zoomScale,
                    center = currentPos,
                    style = Stroke(width = 2.5f * zoomScale)
                )

                // Native Hardware Render of Avatar Emoji inside the circle
                emojiPaint.textSize = 30f * zoomScale
                drawContext.canvas.nativeCanvas.drawText(
                    member.avatarEmoji,
                    currentPos.x,
                    currentPos.y + 11f * zoomScale,
                    emojiPaint
                )

                // Draw Vehicle Badge (🚗, 🚌, 🏍️, 🚲, 🚶) at bottom-right corner of the pin
                val badgePos = Offset(currentPos.x + 17f * zoomScale, currentPos.y + 15f * zoomScale)
                drawCircle(color = RoadDark, radius = 10f * zoomScale, center = badgePos)
                drawCircle(color = Color.White, radius = 10f * zoomScale, center = badgePos, style = Stroke(width = 1.5f * zoomScale))
                smallEmojiPaint.textSize = 15f * zoomScale
                drawContext.canvas.nativeCanvas.drawText(
                    member.vehicleType.iconEmoji,
                    badgePos.x,
                    badgePos.y + 5.5f * zoomScale,
                    smallEmojiPaint
                )

                // Battery status dot at top-right
                val batteryColor = when {
                    member.batteryLevel > 50 -> TrafficGreen
                    member.batteryLevel > 20 -> TrafficOrange
                    else -> SpeedCameraRed
                }
                drawCircle(color = batteryColor, radius = 5f * zoomScale, center = Offset(currentPos.x + 16f * zoomScale, currentPos.y - 16f * zoomScale))
                drawCircle(color = Color.White, radius = 5f * zoomScale, center = Offset(currentPos.x + 16f * zoomScale, currentPos.y - 16f * zoomScale), style = Stroke(width = 1.5f * zoomScale))

                // Floating Mood & Animation Bubble above the avatar
                val bubbleText = member.moodBubble ?: member.name
                bubbleTextPaint.textSize = 18f * zoomScale
                val textWidth = bubbleTextPaint.measureText(bubbleText)
                val bubblePadding = 14f * zoomScale
                val bubbleRect = RectF(
                    currentPos.x - textWidth / 2f - bubblePadding,
                    currentPos.y - 48f * zoomScale,
                    currentPos.x + textWidth / 2f + bubblePadding,
                    currentPos.y - 25f * zoomScale
                )

                // Highlight bubble border according to mood
                bubbleBorderPaint.color = when (member.animationState) {
                    AvatarAnimationState.JOY_MEETING -> android.graphics.Color.argb(220, 255, 105, 180)
                    AvatarAnimationState.WAITING_IMPATIENT -> android.graphics.Color.argb(220, 239, 68, 68)
                    AvatarAnimationState.JOY_ROUTE_EN_CAMINO -> android.graphics.Color.argb(220, 0, 165, 255)
                    else -> android.graphics.Color.argb(180, 255, 255, 255)
                }

                drawContext.canvas.nativeCanvas.drawRoundRect(bubbleRect, 10f * zoomScale, 10f * zoomScale, bubbleBgPaint)
                drawContext.canvas.nativeCanvas.drawRoundRect(bubbleRect, 10f * zoomScale, 10f * zoomScale, bubbleBorderPaint)
                drawContext.canvas.nativeCanvas.drawText(
                    bubbleText,
                    currentPos.x,
                    currentPos.y - 32f * zoomScale,
                    bubbleTextPaint
                )
            }
        }

        // Map Control Floating Actions (Zoom In, Zoom Out, Center)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FloatingActionButton(
                onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(2.5f) },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Acercar mapa")
            }

            FloatingActionButton(
                onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.6f) },
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Alejar mapa")
            }

            FloatingActionButton(
                onClick = {
                    offsetX = 0f
                    offsetY = 0f
                    zoomScale = 1.0f
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.size(46.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Centrar en mi ubicación")
            }
        }
    }
}

private fun projectToScreen(
    location: GeoPoint,
    baseLat: Double,
    baseLng: Double,
    centerX: Float,
    centerY: Float,
    zoomScale: Float
): Offset {
    val dLng = (location.lng - baseLng) * 26000.0 * zoomScale
    val dLat = (location.lat - baseLat) * 26000.0 * zoomScale
    val x = centerX + dLng.toFloat()
    val y = centerY - dLat.toFloat()
    return Offset(x, y)
}

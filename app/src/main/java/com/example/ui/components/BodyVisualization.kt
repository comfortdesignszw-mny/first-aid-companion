package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class BodyPart(val displayName: String, val icon: String, val description: String) {
    ALL("All Issues", "🌐", "All scenarios"),
    HEAD("Head & Neck", "🧠", "Concussions, Seizures, Choking"),
    CHEST("Chest & Heart", "🫀", "Cardiac Arrest, CPR, Heart Attacks"),
    LIMBS("Limbs & Joints", "🦴", "Broken limbs, Severe Bleeding"),
    GENERAL("Full Body & Skin", "🔥", "Burns, Shock, Electrocution")
}

@Composable
fun BodyVisualization(
    selectedPart: BodyPart,
    onPartSelected: (BodyPart) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF231414).copy(alpha = 0.4f)),
        color = Color(0xFF160E0E),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3A1F1F)),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "ANATOMICAL SEARCH",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                    Text(
                        text = "Tap a region to filter guidelines",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray
                    )
                }
                
                // Clear filter chip
                if (selectedPart != BodyPart.ALL) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFD32F2F).copy(alpha = 0.2f))
                            .clickable { onPartSelected(BodyPart.ALL) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Reset Filter ✕",
                            color = Color(0xFFEF5350),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Interactive Anatomy Canvas Canvas
                val activeHeadColor by animateColorAsState(if (selectedPart == BodyPart.HEAD) Color(0xFFD32F2F) else Color(0x66FF5252))
                val activeChestColor by animateColorAsState(if (selectedPart == BodyPart.CHEST) Color(0xFFD32F2F) else Color(0x66FF5252))
                val activeLimbsColor by animateColorAsState(if (selectedPart == BodyPart.LIMBS) Color(0xFFD32F2F) else Color(0x66FF5252))
                val activeGeneralColor by animateColorAsState(if (selectedPart == BodyPart.GENERAL) Color(0xFFD32F2F) else Color(0x33FF5252))

                Box(
                    modifier = Modifier
                        .weight(1.1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(160.dp, 240.dp)
                            .testTag("body_canvas")
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val centerX = canvasWidth / 2f

                        // 1. Draw Stylized Hologram Grid background
                        drawRect(
                            color = Color(0xFF421E1E).copy(alpha = 0.1f),
                            size = size
                        )

                        // Outline Skeleton Lines
                        val bgOutlineColor = Color(0xFF422222)
                        
                        // Head
                        drawCircle(
                            color = bgOutlineColor,
                            radius = 22f,
                            center = Offset(centerX, 40f),
                            style = Stroke(width = 2f)
                        )
                        // Neck & Torso Spinal line
                        drawLine(
                            color = bgOutlineColor,
                            start = Offset(centerX, 62f),
                            end = Offset(centerX, 160f),
                            strokeWidth = 3f
                        )
                        // Shoulders
                        drawLine(
                            color = bgOutlineColor,
                            start = Offset(centerX - 42f, 75f),
                            end = Offset(centerX + 42f, 75f),
                            strokeWidth = 3f
                        )
                        // Hip line
                        drawLine(
                            color = bgOutlineColor,
                            start = Offset(centerX - 28f, 160f),
                            end = Offset(centerX + 28f, 160f),
                            strokeWidth = 3f
                        )

                        // 2. Interactive Regions (Fill translucent red on highlight)
                        // HEAD highlight
                        drawCircle(
                            color = if (selectedPart == BodyPart.HEAD) activeHeadColor else Color.Transparent,
                            radius = 18f,
                            center = Offset(centerX, 40f)
                        )
                        drawCircle(
                            color = activeHeadColor,
                            radius = 20f,
                            center = Offset(centerX, 40f),
                            style = Stroke(width = if (selectedPart == BodyPart.HEAD) 4f else 2f)
                        )

                        // CHEST/HEART highlight (draw shield-like torso chest outline & glowing center heart node)
                        val chestWidth = 48f
                        val chestHeight = 55f
                        val chestTopLeft = Offset(centerX - chestWidth / 2, 85f)
                        if (selectedPart == BodyPart.CHEST) {
                            drawRoundRect(
                                color = activeChestColor.copy(alpha = 0.3f),
                                topLeft = chestTopLeft,
                                size = Size(chestWidth, chestHeight),
                                cornerRadius = CornerRadius(10f, 10f)
                            )
                        }
                        drawRoundRect(
                            color = activeChestColor,
                            topLeft = chestTopLeft,
                            size = Size(chestWidth, chestHeight),
                            cornerRadius = CornerRadius(10f, 10f),
                            style = Stroke(width = if (selectedPart == BodyPart.CHEST) 4f else 2f)
                        )
                        // Small glowing heart node in chest
                        drawCircle(
                            color = if (selectedPart == BodyPart.CHEST) Color.White else activeChestColor,
                            radius = 7f,
                            center = Offset(centerX - 8f, 110f)
                        )

                        // LIMBS (Arms and leg lines)
                        // Left & Right Arms
                        if (selectedPart == BodyPart.LIMBS) {
                            drawLine(color = activeLimbsColor.copy(alpha = 0.4f), start = Offset(centerX - 40f, 75f), end = Offset(centerX - 68f, 140f), strokeWidth = 14f)
                            drawLine(color = activeLimbsColor.copy(alpha = 0.4f), start = Offset(centerX + 40f, 75f), end = Offset(centerX + 68f, 140f), strokeWidth = 14f)
                            drawLine(color = activeLimbsColor.copy(alpha = 0.4f), start = Offset(centerX - 22f, 160f), end = Offset(centerX - 35f, 230f), strokeWidth = 14f)
                            drawLine(color = activeLimbsColor.copy(alpha = 0.4f), start = Offset(centerX + 22f, 160f), end = Offset(centerX + 35f, 230f), strokeWidth = 14f)
                        }
                        
                        // Arm lines
                        drawLine(color = activeLimbsColor, start = Offset(centerX - 40f, 75f), end = Offset(centerX - 68f, 140f), strokeWidth = if (selectedPart == BodyPart.LIMBS) 4f else 2f)
                        drawLine(color = activeLimbsColor, start = Offset(centerX + 40f, 75f), end = Offset(centerX + 68f, 140f), strokeWidth = if (selectedPart == BodyPart.LIMBS) 4f else 2f)
                        // Leg lines
                        drawLine(color = activeLimbsColor, start = Offset(centerX - 22f, 160f), end = Offset(centerX - 35f, 230f), strokeWidth = if (selectedPart == BodyPart.LIMBS) 4f else 2f)
                        drawLine(color = activeLimbsColor, start = Offset(centerX + 22f, 160f), end = Offset(centerX + 35f, 230f), strokeWidth = if (selectedPart == BodyPart.LIMBS) 4f else 2f)

                        // 3. FULL BODY / SYSTEMIC (Dotted or glowing soft border around full area)
                        if (selectedPart == BodyPart.GENERAL) {
                            drawRoundRect(
                                color = activeGeneralColor.copy(alpha = 0.15f),
                                topLeft = Offset(10f, 10f),
                                size = Size(canvasWidth - 20f, canvasHeight - 20f),
                                cornerRadius = CornerRadius(20f, 20f)
                            )
                        }
                        drawRoundRect(
                            color = activeGeneralColor.copy(alpha = 0.5f),
                            topLeft = Offset(10f, 10f),
                            size = Size(canvasWidth - 20f, canvasHeight - 20f),
                            cornerRadius = CornerRadius(20f, 20f),
                            style = Stroke(width = 1.5f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
                        )
                    }
                }

                // Interactive Buttons pointing to body parts
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    BodyPart.values().filter { it != BodyPart.ALL }.forEach { part ->
                        val isSelected = selectedPart == part
                        val targetBgColor = if (isSelected) Color(0xFFD32F2F) else Color(0xFF221515)
                        val targetTextColor = if (isSelected) Color.White else Color.LightGray
                        val targetBorderColor = if (isSelected) Color(0xFFEF5350) else Color(0xFF3D2121)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(targetBgColor)
                                .clickable {
                                    onPartSelected(if (isSelected) BodyPart.ALL else part)
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                                .testTag("body_part_${part.name.lowercase()}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = part.icon,
                                    fontSize = 16.sp
                                )
                                Column {
                                    Text(
                                        text = part.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = targetTextColor,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = part.description,
                                        fontSize = 9.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray,
                                        lineHeight = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.drawStylisedScrollbar(
    scrollState: ScrollState,
    color: Color = Color(0xFFD32F2F), // Accent stylized red
    trackColor: Color = Color(0xFF424242).copy(alpha = 0.2f),
    scrollbarWidth: Dp = 6.dp
): Modifier = this.drawWithContent {
    // Draw the view's content first
    drawContent()

    val maxValue = scrollState.maxValue
    if (maxValue > 0) {
        val viewportHeight = size.height
        val contentHeight = viewportHeight + maxValue
        
        // Calculate proportional thumb height
        val thumbHeight = (viewportHeight / contentHeight) * viewportHeight
        val thumbMinHeight = 40f // Ensure it's never too small to see/click
        val adjustedThumbHeight = maxOf(thumbHeight, thumbMinHeight)
        
        // Compute scroll progress placement
        val scrollPercent = scrollState.value.toFloat() / maxValue
        val scrollTrackActiveLength = viewportHeight - adjustedThumbHeight
        val thumbOffset = scrollPercent * scrollTrackActiveLength
        
        val widthPx = scrollbarWidth.toPx()
        
        // Draw track background (right aligned)
        drawRect(
            color = trackColor,
            topLeft = Offset(size.width - widthPx - 4f, 0f),
            size = Size(widthPx, viewportHeight)
        )
        
        // Draw elegant capsule/pill scrollbar thumb
        drawRoundRect(
            color = color,
            topLeft = Offset(size.width - widthPx - 4f, thumbOffset),
            size = Size(widthPx, adjustedThumbHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(widthPx / 2, widthPx / 2)
        )
    }
}

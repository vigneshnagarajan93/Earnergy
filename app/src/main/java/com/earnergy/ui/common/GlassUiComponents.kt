package com.earnergy.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val baseColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.12f)
    val borderColor = Color.White.copy(alpha = 0.25f)
    val overlay = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.1f),
            Color.White.copy(alpha = 0.02f)
        )
    )
    Surface(
        modifier = modifier
            .shadow(elevation = 12.dp, shape = shape, clip = false)
            .background(baseColor, shape)
            .border(width = 1.dp, color = borderColor, shape = shape),
        color = Color.Transparent,
        shape = shape
    ) {
        Box(
            modifier = Modifier
                .background(overlay, shape)
                .padding(16.dp),
            content = content
        )
    }
}

@Composable
fun GradientButton(
    text: String,
    colors: List<Color>,
    icon: (@Composable (() -> Unit))? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val gradient = Brush.horizontalGradient(colors)
    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .background(gradient, RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                    icon()
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun GlassChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (selected) selectedColor.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f)
    val borderColor = if (selected) selectedColor else Color.White.copy(alpha = 0.25f)
    Box(
        modifier = modifier
            .background(background, RoundedCornerShape(16.dp))
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.75f)
        )
    }
}

@Composable
fun ProgressRing(
    progress: Float,
    size: Dp,
    strokeWidth: Dp = 12.dp,
    color: Color = Color(0xFFFCD34D),
    trackColor: Color = Color.White.copy(alpha = 0.15f)
) {
    Canvas(modifier = Modifier.size(size)) {
        val radius = size.toPx() / 2
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        drawCircle(color = trackColor, radius = radius, center = center, style = stroke)
        drawArc(
            brush = Brush.sweepGradient(listOf(color.copy(alpha = 0.6f), color)),
            startAngle = -90f,
            sweepAngle = (progress.coerceIn(0f, 1f)) * 360,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2),
            style = stroke
        )
    }
}

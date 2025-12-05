package com.streamflux.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WavyRefreshIcon(
    isRefreshing: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    val rotation = remember { Animatable(0f) }
    val wavePhase = remember { Animatable(0f) }
    
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            launch {
                rotation.animateTo(
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
            }
            launch {
                wavePhase.animateTo(
                    targetValue = 2 * PI.toFloat(),
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
            }
        } else {
            rotation.stop()
            wavePhase.stop()
            rotation.animateTo(0f, ExpressiveMotion.expressiveSpring())
            wavePhase.snapTo(0f)
        }
    }
    
    Canvas(
        modifier = modifier
            .size(48.dp)
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(bounded = false, radius = 24.dp)
            )
    ) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val radius = size.minDimension / 2 * 0.7f
        
        rotate(rotation.value, pivot = Offset(centerX, centerY)) {
            // Draw wavy circular path
            val path = Path()
            val points = 60
            val waveAmplitude = 4f
            val waveFrequency = 6f
            
            for (i in 0..points) {
                val angle = (i.toFloat() / points) * 2 * PI
                val wave = waveAmplitude * sin(waveFrequency * angle + wavePhase.value)
                val r = radius + wave
                
                val x = centerX + r * cos(angle).toFloat()
                val y = centerY + r * sin(angle).toFloat()
                
                if (i == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }
            path.close()
            
            drawPath(
                path = path,
                color = color,
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
            
            // Draw arrow
            val arrowSize = 12f
            val arrowAngle = PI / 6
            val arrowX = centerX + radius * cos(PI / 2).toFloat()
            val arrowY = centerY + radius * sin(PI / 2).toFloat()
            
            drawLine(
                color = color,
                start = Offset(arrowX, arrowY),
                end = Offset(
                    arrowX - arrowSize * cos(PI / 2 - arrowAngle).toFloat(),
                    arrowY - arrowSize * sin(PI / 2 - arrowAngle).toFloat()
                ),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
            
            drawLine(
                color = color,
                start = Offset(arrowX, arrowY),
                end = Offset(
                    arrowX - arrowSize * cos(PI / 2 + arrowAngle).toFloat(),
                    arrowY - arrowSize * sin(PI / 2 + arrowAngle).toFloat()
                ),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

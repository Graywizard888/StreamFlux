package com.streamflux.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WavyProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    backgroundColor: Color = Color.White.copy(alpha = 0.3f)
) {
    val wavePhase = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        wavePhase.animateTo(
            targetValue = 2 * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        
        // Background wave
        val bgPath = Path()
        val points = 100
        val waveAmplitude = height / 3
        val waveFrequency = 4f
        
        for (i in 0..points) {
            val x = (i.toFloat() / points) * width
            val wave = waveAmplitude * sin(waveFrequency * (i.toFloat() / points) * 2 * PI + wavePhase.value)
            val y = centerY + wave.toFloat()
            
            if (i == 0) {
                bgPath.moveTo(x, y)
            } else {
                bgPath.lineTo(x, y)
            }
        }
        
        drawPath(
            path = bgPath,
            color = backgroundColor,
            style = Stroke(
                width = height,
                cap = StrokeCap.Round
            )
        )
        
        // Progress wave
        val progressPath = Path()
        val progressWidth = width * progress.coerceIn(0f, 1f)
        val progressPoints = (points * progress).toInt().coerceAtLeast(1)
        
        for (i in 0..progressPoints) {
            val x = (i.toFloat() / points) * width
            if (x > progressWidth) break
            
            val wave = waveAmplitude * sin(waveFrequency * (i.toFloat() / points) * 2 * PI + wavePhase.value)
            val y = centerY + wave.toFloat()
            
            if (i == 0) {
                progressPath.moveTo(x, y)
            } else {
                progressPath.lineTo(x, y)
            }
        }
        
        drawPath(
            path = progressPath,
            color = color,
            style = Stroke(
                width = height,
                cap = StrokeCap.Round
            )
        )
    }
}

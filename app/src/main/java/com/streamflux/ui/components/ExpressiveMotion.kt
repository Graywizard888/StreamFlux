package com.streamflux.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.VectorConverter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset

object ExpressiveMotion {
    // Spring physics for expressive motion
    fun <T> expressiveSpring() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    fun <T> emphasizedDecelerate() = tween<T>(
        durationMillis = 400,
        easing = androidx.compose.animation.core.FastOutSlowInEasing
    )
    
    fun <T> emphasizedAccelerate() = tween<T>(
        durationMillis = 200,
        easing = androidx.compose.animation.core.FastOutLinearInEasing
    )
}

@Composable
fun rememberExpressiveAnimatable(initialValue: Float = 0f) = 
    remember { Animatable(initialValue) }

@Composable
fun rememberExpressiveOffsetAnimatable(initialValue: Offset = Offset.Zero) = 
    remember { Animatable(initialValue, Offset.VectorConverter) }

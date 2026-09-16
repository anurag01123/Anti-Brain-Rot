package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun CloudsAndBirds(modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val infiniteTransition = rememberInfiniteTransition(label = "clouds")
    
    // Animate cloud drift
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )
    
    // Animate bird bobbing
    val bob by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    Canvas(modifier = modifier) {
        // Draw cloud 1
        drawCircle(color = primaryColor.copy(alpha = 0.2f), radius = size.width * 0.2f, center = Offset(size.width * 0.85f + drift, size.height * 1.1f))
        drawCircle(color = primaryColor.copy(alpha = 0.2f), radius = size.width * 0.15f, center = Offset(size.width * 0.65f + drift, size.height * 1.2f))
        drawCircle(color = primaryColor.copy(alpha = 0.2f), radius = size.width * 0.25f, center = Offset(size.width * 1.05f + drift, size.height * 0.9f))
        
        // Draw cloud 2
        drawCircle(color = secondaryColor.copy(alpha = 0.15f), radius = size.width * 0.1f, center = Offset(size.width * 0.15f - drift, size.height * -0.1f))
        drawCircle(color = secondaryColor.copy(alpha = 0.15f), radius = size.width * 0.15f, center = Offset(size.width * 0.35f - drift, size.height * -0.2f))
        
        // Draw bird 1
        val path = Path().apply {
            moveTo(size.width * 0.75f, size.height * 0.2f + bob)
            quadraticTo(size.width * 0.78f, size.height * 0.15f + bob, size.width * 0.81f, size.height * 0.2f + bob)
            quadraticTo(size.width * 0.84f, size.height * 0.15f + bob, size.width * 0.87f, size.height * 0.2f + bob)
        }
        drawPath(path = path, color = primaryColor.copy(alpha = 0.6f), style = Stroke(width = 4f, cap = StrokeCap.Round))
        
        // Draw bird 2
        val path2 = Path().apply {
            moveTo(size.width * 0.65f, size.height * 0.35f - bob)
            quadraticTo(size.width * 0.67f, size.height * 0.32f - bob, size.width * 0.69f, size.height * 0.35f - bob)
            quadraticTo(size.width * 0.71f, size.height * 0.32f - bob, size.width * 0.73f, size.height * 0.35f - bob)
        }
        drawPath(path = path2, color = primaryColor.copy(alpha = 0.6f), style = Stroke(width = 3f, cap = StrokeCap.Round))
    }
}

@Composable
fun MountainScene(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    
    val infiniteTransition = rememberInfiniteTransition(label = "mountains")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mountain_drift"
    )

    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(-drift, size.height * 0.7f)
            quadraticTo(size.width * 0.25f, size.height * 0.5f, size.width * 0.5f - drift, size.height * 0.65f)
            quadraticTo(size.width * 0.75f, size.height * 0.8f, size.width + drift, size.height * 0.55f)
            lineTo(size.width + drift, size.height)
            lineTo(-drift, size.height)
            close()
        }
        drawPath(path, color = tertiary.copy(alpha = 0.1f))
        
        val path2 = Path().apply {
            moveTo(-drift * 0.5f, size.height * 0.8f)
            quadraticTo(size.width * 0.3f, size.height * 0.7f, size.width * 0.6f + drift * 0.5f, size.height * 0.85f)
            quadraticTo(size.width * 0.8f, size.height * 0.9f, size.width + drift * 0.5f, size.height * 0.75f)
            lineTo(size.width + drift * 0.5f, size.height)
            lineTo(-drift * 0.5f, size.height)
            close()
        }
        drawPath(path2, color = primary.copy(alpha = 0.15f))
    }
}

@Composable
fun GrowingPlant(modifier: Modifier = Modifier, progress: Float) {
    val primaryColor = MaterialTheme.colorScheme.primary
    
    val infiniteTransition = rememberInfiniteTransition(label = "sway")
    val sway by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway_anim"
    )
    
    Canvas(modifier = modifier) {
        val path = Path().apply {
            moveTo(size.width / 2, size.height)
            quadraticTo(
                size.width / 2 + sway, 
                size.height * (1f - progress * 0.5f), 
                size.width / 2, 
                size.height * (1f - progress)
            )
        }
        drawPath(path, color = primaryColor, style = Stroke(width = 8f, cap = StrokeCap.Round))
        
        if (progress > 0.3f) {
            val leafPath = Path().apply {
                moveTo(size.width / 2, size.height * 0.7f)
                quadraticTo(size.width * 0.8f + sway, size.height * 0.6f, size.width * 0.7f, size.height * 0.5f)
                quadraticTo(size.width * 0.6f, size.height * 0.4f, size.width / 2 + sway*0.5f, size.height * 0.65f)
            }
            drawPath(leafPath, color = primaryColor.copy(alpha = 0.8f), style = Stroke(width = 4f))
        }
        if (progress > 0.7f) {
            val leafPath2 = Path().apply {
                moveTo(size.width / 2, size.height * 0.4f)
                quadraticTo(size.width * 0.2f + sway, size.height * 0.3f, size.width * 0.3f, size.height * 0.2f)
                quadraticTo(size.width * 0.4f, size.height * 0.1f, size.width / 2 + sway*0.5f, size.height * 0.35f)
            }
            drawPath(leafPath2, color = primaryColor.copy(alpha = 0.8f), style = Stroke(width = 4f))
        }
    }
}

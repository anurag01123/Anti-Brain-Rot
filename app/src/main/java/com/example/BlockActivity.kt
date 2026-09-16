package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import java.util.Calendar

import androidx.activity.compose.BackHandler

class BlockActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val blockedAppName = intent.getStringExtra("BLOCKED_APP") ?: "Target App"
        val blockReason = intent.getStringExtra("BLOCK_REASON")
        val db = AppDatabase.getDatabase(applicationContext)
        val repository = AppRepository(db.appDao())
        
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                
                BackHandler(enabled = true) {
                    val homeIntent = Intent(Intent.ACTION_MAIN)
                    homeIntent.addCategory(Intent.CATEGORY_HOME)
                    homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(homeIntent)
                }

                val contacts by repository.allContacts.collectAsStateWithLifecycle(initialValue = emptyList())
                
                var timeRemaining by remember { mutableStateOf("00:00:00") }
                
                LaunchedEffect(Unit) {
                    while (kotlinx.coroutines.currentCoroutineContext().isActive) {
                        val cal = Calendar.getInstance()
                        val current = cal.timeInMillis
                        cal.set(Calendar.HOUR_OF_DAY, 23)
                        cal.set(Calendar.MINUTE, 59)
                        cal.set(Calendar.SECOND, 59)
                        val endOfDay = cal.timeInMillis
                        val diff = endOfDay - current
                        
                        val hours = (diff / (1000 * 60 * 60)) % 24
                        val minutes = (diff / (1000 * 60)) % 60
                        val seconds = (diff / 1000) % 60
                        timeRemaining = String.format("%02d:%02d:%02d", hours, minutes, seconds)
                        
                        delay(1000)
                    }
                }
                
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startOfDay = calendar.timeInMillis
                
                val minContactsRequired = 3
                val calledCount = contacts.count { it.lastCalledTimestamp >= startOfDay }
                val allCleared = contacts.size >= minContactsRequired && calledCount >= contacts.size

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    val primary = MaterialTheme.colorScheme.primary
                    val tertiary = MaterialTheme.colorScheme.tertiary
                    val errorColor = MaterialTheme.colorScheme.error
                    
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        // Night sky / dramatic background elements
                        drawCircle(color = errorColor.copy(alpha = 0.1f), radius = size.width * 0.8f, center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.1f))
                        
                        // Mountains
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, size.height * 0.6f)
                            quadraticTo(size.width * 0.25f, size.height * 0.4f, size.width * 0.5f, size.height * 0.55f)
                            quadraticTo(size.width * 0.75f, size.height * 0.7f, size.width, size.height * 0.45f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(path, color = tertiary.copy(alpha = 0.2f))
                        
                        val path2 = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, size.height * 0.7f)
                            quadraticTo(size.width * 0.3f, size.height * 0.6f, size.width * 0.6f, size.height * 0.75f)
                            quadraticTo(size.width * 0.8f, size.height * 0.8f, size.width, size.height * 0.65f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(path2, color = primary.copy(alpha = 0.25f))
                        
                        // Small animal silhouette (e.g., bird or owl)
                        val animalPath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(size.width * 0.8f, size.height * 0.2f)
                            quadraticTo(size.width * 0.82f, size.height * 0.18f, size.width * 0.85f, size.height * 0.2f)
                            quadraticTo(size.width * 0.87f, size.height * 0.18f, size.width * 0.89f, size.height * 0.2f)
                        }
                        drawPath(animalPath, color = primary.copy(alpha = 0.5f), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
                    }

                    // Glass bottom sheet
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .fillMaxHeight(0.8f)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                            .padding(horizontal = 24.dp, vertical = 32.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = "LOCKED OUT",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                letterSpacing = 2.sp
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = blockedAppName,
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (blockReason != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = blockReason,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, androidx.compose.foundation.shape.CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    progress = { 1f },
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.error,
                                    strokeWidth = 8.dp
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = timeRemaining,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Cooldown",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Text(
                                text = "Call Completion Checklist",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            if (contacts.isEmpty()) {
                                Text("No penalty contacts set. You are locked until tomorrow.", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                            } else if (contacts.size < minContactsRequired) {
                                Text("You need at least 3 contacts to unlock. You are locked until tomorrow.", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
                            } else {
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(contacts) { contact ->
                                        val isCompleted = contact.lastCalledTimestamp >= startOfDay
                                        Card(
                                            modifier = Modifier.fillMaxWidth().clickable {
                                                val intent = Intent(Intent.ACTION_DIAL)
                                                intent.data = Uri.parse("tel:${contact.phoneNumber}")
                                                context.startActivity(intent)
                                            },
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.cardColors(containerColor = if (isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha=0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.4f)),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha=0.3f) else Color.Transparent)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier.size(48.dp).background(if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        if (isCompleted) {
                                                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                                                        } else {
                                                            val onSurfaceVar = MaterialTheme.colorScheme.onSurfaceVariant
                                                            androidx.compose.foundation.Canvas(modifier = Modifier.size(24.dp)) {
                                                                drawCircle(color = onSurfaceVar, radius = size.width * 0.4f, center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.5f))
                                                                drawCircle(color = onSurfaceVar, radius = size.width * 0.2f, center = androidx.compose.ui.geometry.Offset(size.width * 0.2f, size.height * 0.2f))
                                                                drawCircle(color = onSurfaceVar, radius = size.width * 0.2f, center = androidx.compose.ui.geometry.Offset(size.width * 0.8f, size.height * 0.2f))
                                                            }
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(16.dp))
                                                    Column {
                                                        Text(contact.contactName, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                        Text(if (isCompleted) "Completed" else "Pending Call", style = MaterialTheme.typography.bodySmall, color = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Button(
                                    onClick = {
                                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                            repository.incrementUrgeInterrupted()
                                        }
                                        val homeIntent = Intent(Intent.ACTION_MAIN)
                                        homeIntent.addCategory(Intent.CATEGORY_HOME)
                                        homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                        startActivity(homeIntent)
                                        // Do not call finish(), so the lock persists when returning
                                    },
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(28.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                                ) {
                                    Text("Give Up", fontWeight = FontWeight.Bold)
                                }
                                
                                Button(
                                    onClick = { finish() },
                                    enabled = allCleared,
                                    modifier = Modifier.weight(1f).height(56.dp),
                                    shape = RoundedCornerShape(28.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text("Unlock App", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                    
                    // Floating icon
                    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha"
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .offset(y = 60.dp)
                            .size(100.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(32.dp))
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val primaryColor = MaterialTheme.colorScheme.primary
                        com.example.ui.components.GrowingPlant(modifier = Modifier.fillMaxSize(), progress = 1f)
                    }
                }
            }
        }
    }
}

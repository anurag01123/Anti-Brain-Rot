package com.example

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.provider.CallLog
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.Manifest

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

import android.view.WindowManager
import androidx.activity.compose.BackHandler

class BlockActivity : ComponentActivity() {
    private var blockedAppNameState = mutableStateOf("Target App")
    private var blockReasonState = mutableStateOf<String?>(null)
    private var packageNameState = mutableStateOf("")
    private var limitMinutesState = mutableIntStateOf(0)
    private var isUnconditionalState = mutableStateOf(false)

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return
        blockedAppNameState.value = intent.getStringExtra("BLOCKED_APP") ?: "Target App"
        blockReasonState.value = intent.getStringExtra("BLOCK_REASON")
        packageNameState.value = intent.getStringExtra("PACKAGE_NAME") ?: ""
        limitMinutesState.intValue = intent.getIntExtra("LIMIT_MINUTES", 0)
        isUnconditionalState.value = intent.getBooleanExtra("IS_UNCONDITIONAL", false)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
        SystemAlertWindowService.onActivityDisplayed(this)
    }

    override fun onResume() {
        super.onResume()
        SystemAlertWindowService.onActivityDisplayed(this)
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        SystemAlertWindowService.hideBlockOverlay(this)
        BlockerAccessibilityService.notifyAppExited(packageNameState.value)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        SystemAlertWindowService.hideBlockOverlay(this)
        BlockerAccessibilityService.notifyAppExited(packageNameState.value)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemAlertWindowService.onActivityDisplayed(this)
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        handleIncomingIntent(intent)
        
        val db = AppDatabase.getDatabase(applicationContext)
        val repository = AppRepository(db.appDao())
        
        setContent {
            val blockedAppName = blockedAppNameState.value
            val blockReason = blockReasonState.value
            val packageName = packageNameState.value
            val limitMinutes = limitMinutesState.intValue
            val isUnconditional = isUnconditionalState.value
            val prefs = applicationContext.getSharedPreferences("block_settings", android.content.Context.MODE_PRIVATE)
                val isDarkMode = prefs.getBoolean("dark_mode", false)
                MyApplicationTheme(darkTheme = isDarkMode) {
                val context = LocalContext.current
                var hasCallLog by remember { mutableStateOf(androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALL_LOG) == android.content.pm.PackageManager.PERMISSION_GRANTED) }
                val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                    androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    hasCallLog = isGranted
                }
                
                androidx.compose.runtime.LaunchedEffect(Unit) {
                    if (!hasCallLog) {
                        permissionLauncher.launch(android.Manifest.permission.READ_CALL_LOG)
                    }
                }
                
                BackHandler(enabled = true) {
                    SystemAlertWindowService.hideBlockOverlay(context)
                    BlockerAccessibilityService.goToHome()
                    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_HOME)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    context.startActivity(homeIntent)
                    (context as? android.app.Activity)?.finish()
                }

                val contacts by repository.allContacts.collectAsStateWithLifecycle(initialValue = emptyList())
                
                var timeRemaining by remember { mutableStateOf("00:00:00") }
                
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                    val calendar = Calendar.getInstance()
                                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                                    calendar.set(Calendar.MINUTE, 0)
                                    calendar.set(Calendar.SECOND, 0)
                                    calendar.set(Calendar.MILLISECOND, 0)
                                    val startOfDay = calendar.timeInMillis
                                    
                                    val hasCallLog = androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALL_LOG) == android.content.pm.PackageManager.PERMISSION_GRANTED
                                    val cursor = if (hasCallLog) context.contentResolver.query(
                                        CallLog.Calls.CONTENT_URI,
                                        arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE, CallLog.Calls.DURATION),
                                        "${CallLog.Calls.DATE} >= ?",
                                        arrayOf(startOfDay.toString()),
                                        "${CallLog.Calls.DATE} DESC"
                                    ) else null
                                    
                                    if (hasCallLog) cursor?.use { c ->
                                        val numIndex = c.getColumnIndex(CallLog.Calls.NUMBER)
                                        val dateIndex = c.getColumnIndex(CallLog.Calls.DATE)
                                        val typeIndex = c.getColumnIndex(CallLog.Calls.TYPE)
                                        val durationIndex = c.getColumnIndex(CallLog.Calls.DURATION)
                                        
                                        val calledNumbers = mutableMapOf<String, Long>()
                                        while (c.moveToNext()) {
                                            val number = c.getString(numIndex)
                                            val date = c.getLong(dateIndex)
                                            val type = c.getInt(typeIndex)
                                            val duration = c.getLong(durationIndex)
                                            if (type == CallLog.Calls.OUTGOING_TYPE && duration >= 60) {
                                                calledNumbers[number] = date
                                            }
                                        }
                                        
                                        val currentContacts = repository.getAllContactsSync()
                                        for (contact in currentContacts) {
                                            if (contact.lastCalledTimestamp < startOfDay) {
                                                val match = calledNumbers.entries.find { 
                                                    android.telephony.PhoneNumberUtils.compare(it.key, contact.phoneNumber) ||
                                                    (it.key.replace(Regex("[^0-9]"), "").takeLast(7) == contact.phoneNumber.replace(Regex("[^0-9]"), "").takeLast(7) && contact.phoneNumber.replace(Regex("[^0-9]"), "").length >= 7)
                                                }
                                                if (match != null) {
                                                    repository.markContactCalled(contact.phoneNumber, match.value)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

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

                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val shieldPulse by infiniteTransition.animateFloat(
                    initialValue = 0.96f,
                    targetValue = 1.04f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "shield_pulse"
                )

                val stats by repository.allDailyStats.collectAsStateWithLifecycle(initialValue = emptyList())
                val todayEpoch = java.time.LocalDate.now().toEpochDay()
                val todayStat = stats.find { it.dateEpochDay == todayEpoch }
                val urgesResisted = todayStat?.urgesInterrupted ?: 0

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0A0814))
                ) {
                    // Ambient radial atmospheric background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x334F46E5),
                                        Color(0x117C3AED),
                                        Color.Transparent
                                    ),
                                    center = androidx.compose.ui.geometry.Offset(500f, 200f),
                                    radius = 1200f
                                )
                            )
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .navigationBarsPadding(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // 1. Hero Shield & Badges
                        item {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier.size(116.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Glowing halo
                                    Box(
                                        modifier = Modifier
                                            .size(116.dp)
                                            .scale(shieldPulse)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    colors = listOf(
                                                        Color(0x446366F1),
                                                        Color.Transparent
                                                    )
                                                )
                                            )
                                    )
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_lockout_shield),
                                        contentDescription = "Lockout Shield",
                                        modifier = Modifier.size(92.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Surface(
                                    color = Color(0x226366F1),
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x55818CF8))
                                ) {
                                    Text(
                                        text = "ANTI BRAIN ROT • FOCUS LOCK",
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFA5B4FC),
                                        letterSpacing = 1.2.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = blockedAppName,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = blockReason ?: if (isUnconditional) {
                                        "Application is locked to break compulsive digital habits."
                                    } else {
                                        "Daily limit reached (${limitMinutes}m). Reclaim your time and energy."
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF94A3B8),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }

                        // 2. Cooldown Timer Card
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(26.dp))
                                    .background(Color(0xFF131024))
                                    .border(
                                        width = 1.dp,
                                        color = Color(0x33818CF8),
                                        shape = RoundedCornerShape(26.dp)
                                    )
                                    .padding(22.dp)
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_cooldown_timer),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "MIDNIGHT RESET COOLDOWN",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF818CF8),
                                            letterSpacing = 1.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = timeRemaining,
                                        style = MaterialTheme.typography.displayMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = 2.sp
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Hours : Minutes : Seconds remaining today",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }

                        // 3. Urge Pause & Mindfulness Card
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(Color(0xFF17132C))
                                    .border(
                                        width = 1.dp,
                                        color = Color(0x22F43F5E),
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_flame_streak),
                                        contentDescription = null,
                                        modifier = Modifier.size(54.dp)
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Urges Resisted Today",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFB7185)
                                        )
                                        Text(
                                            text = "$urgesResisted impulses resisted",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Each urge resisted restores mental clarity.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                        }

                        // 4. Accountability Contacts Section (if not unconditional lock)
                        if (!isUnconditional) {
                            item {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "Emergency Accountability",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "Phone contacts (60s+) to verify intentional use",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }

                                        Surface(
                                            color = if (allCleared) Color(0x3310B981) else Color(0x226366F1),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(
                                                text = "$calledCount / $minContactsRequired",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (allCleared) Color(0xFF34D399) else Color(0xFFA5B4FC)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    if (contacts.isEmpty()) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(Color(0xFF131024))
                                                .padding(18.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "No penalty contacts set up in AntiBrainRot. Add 3 contacts in the app to enable emergency unlocks.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF64748B),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    } else {
                                        contacts.forEach { contact ->
                                            val isCalled = contact.lastCalledTimestamp >= startOfDay
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 4.dp)
                                                    .clip(RoundedCornerShape(18.dp))
                                                    .background(Color(0xFF131024))
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (isCalled) Color(0x4410B981) else Color(0x22818CF8),
                                                        shape = RoundedCornerShape(18.dp)
                                                    )
                                                    .clickable {
                                                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                                            data = Uri.parse("tel:${contact.phoneNumber}")
                                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                                        }
                                                        startActivity(dialIntent)
                                                    }
                                                    .padding(14.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(42.dp)
                                                                .clip(CircleShape)
                                                                .background(
                                                                    if (isCalled) Color(0x2210B981) else Color(0x226366F1)
                                                                ),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(
                                                                imageVector = if (isCalled) Icons.Rounded.CheckCircle else Icons.Rounded.Call,
                                                                contentDescription = null,
                                                                tint = if (isCalled) Color(0xFF34D399) else Color(0xFF818CF8),
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Column {
                                                            Text(
                                                                text = contact.contactName,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White,
                                                                style = MaterialTheme.typography.bodyMedium
                                                            )
                                                            Text(
                                                                text = contact.phoneNumber,
                                                                color = Color(0xFF64748B),
                                                                style = MaterialTheme.typography.labelSmall
                                                            )
                                                        }
                                                    }

                                                    Surface(
                                                        color = if (isCalled) Color(0x2210B981) else Color(0x226366F1),
                                                        shape = RoundedCornerShape(10.dp)
                                                    ) {
                                                        Text(
                                                            text = if (isCalled) "Verified" else "Call Now",
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isCalled) Color(0xFF34D399) else Color(0xFFA5B4FC)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 5. Action Buttons
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Primary Button: Resist Urge & Go Home
                                Button(
                                    onClick = {
                                        SystemAlertWindowService.hideBlockOverlay(context)
                                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                            repository.incrementUrgeInterrupted()
                                        }
                                        BlockerAccessibilityService.goToHome()
                                        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                                            addCategory(Intent.CATEGORY_HOME)
                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        }
                                        startActivity(homeIntent)
                                        finish()
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(28.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4F46E5),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Home,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Resist Urge & Go Home",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    )
                                }

                                // Secondary Unlock Button (if calls completed)
                                if (!isUnconditional && allCleared) {
                                    Button(
                                        onClick = {
                                            SystemAlertWindowService.hideBlockOverlay(context)
                                            val bonusKey = "bonus_time_${packageName}_${startOfDay}"
                                            val currentBonus = prefs.getLong(bonusKey, 0L)
                                            val additionalBonus = limitMinutes * 60 * 1000L
                                            prefs.edit().putLong(bonusKey, currentBonus + additionalBonus).apply()

                                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                                                val currentContacts = repository.getAllContactsSync()
                                                for (c in currentContacts) {
                                                    repository.markContactCalled(c.phoneNumber, 0L)
                                                }
                                                repository.incrementUnlockOccurrence()
                                                repository.logUnlockEvent(
                                                    packageName = packageName,
                                                    appName = blockedAppName,
                                                    timestamp = System.currentTimeMillis(),
                                                    bonusMinutesGranted = limitMinutes,
                                                    newEffectiveLimitMinutes = limitMinutes + ((currentBonus + additionalBonus) / (60 * 1000L)).toInt()
                                                )
                                            }
                                            finish()
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp),
                                        shape = RoundedCornerShape(28.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF059669),
                                            contentColor = Color.White
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.CheckCircle,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "Claim Bonus Screen Time (+${limitMinutes}m)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
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
}

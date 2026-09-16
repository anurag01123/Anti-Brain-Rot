import re

with open("app/src/main/java/com/example/MainActivity.kt", "r") as f:
    content = f.read()

old_theme_block = """            MyApplicationTheme(darkTheme = isDarkMode) {
                MainScreen(
                    viewModel = viewModel,
                    onCheckUsagePermission = { checkUsageStatsPermission() },
                    onCheckAccessibility = {
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                )
            }"""

new_theme_block = """            MyApplicationTheme(darkTheme = isDarkMode) {
                PermissionGate {
                    MainScreen(
                        viewModel = viewModel,
                        onCheckUsagePermission = { checkUsageStatsPermission() },
                        onCheckAccessibility = {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        }
                    )
                }
            }"""

if old_theme_block in content:
    content = content.replace(old_theme_block, new_theme_block)
else:
    print("Could not find old_theme_block")


permission_gate_code = """
@Composable
fun PermissionGate(onPermissionsGranted: @Composable () -> Unit) {
    val context = LocalContext.current
    var hasContacts by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) }
    var hasCallLog by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) }
    
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasContacts = permissions[Manifest.permission.READ_CONTACTS] ?: hasContacts
        hasCallLog = permissions[Manifest.permission.READ_CALL_LOG] ?: hasCallLog
    }

    // Also check on resume in case they grant it via settings
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasContacts = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                hasCallLog = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (hasContacts && hasCallLog) {
        onPermissionsGranted()
    } else {
        Scaffold { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Call, 
                    contentDescription = null, 
                    modifier = Modifier.size(72.dp), 
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Permissions Required", 
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "AntiBrainRot requires access to your Contacts and Call Log. This allows the app to verify when you call a Penalty Contact to unlock your device.", 
                    textAlign = TextAlign.Center, 
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(48.dp))
                Button(
                    onClick = {
                        permissionLauncher.launch(arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG))
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Grant Permissions", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
"""

# Append PermissionGate to the end of the file
content += permission_gate_code

with open("app/src/main/java/com/example/MainActivity.kt", "w") as f:
    f.write(content)


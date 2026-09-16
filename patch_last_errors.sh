#!/bin/bash
sed -i 's/Icons.Default.ArrowForward/Icons.AutoMirrored.Filled.ArrowForward/g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/fun ContactsScreen(viewModel: MainViewModel)/@Composable\nfun ContactsScreen(viewModel: MainViewModel)/g' app/src/main/java/com/example/MainActivity.kt

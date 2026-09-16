#!/bin/bash
sed -i 's/Icons.Rounded.ArrowForward/Icons.Default.ArrowForward/g' app/src/main/java/com/example/MainActivity.kt
sed -i 's/@Composable\n@Composable/@Composable/g' app/src/main/java/com/example/MainActivity.kt

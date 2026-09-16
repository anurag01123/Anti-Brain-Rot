#!/bin/bash
sed -i 's/padding(horizontal = 24.dp, top = 16.dp, bottom = 8.dp)/padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp)/g' app/src/main/java/com/example/MainActivity.kt

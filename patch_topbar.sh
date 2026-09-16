#!/bin/bash
sed -i '123,158c\
        topBar = {\
            Card(\
                modifier = Modifier\
                    .fillMaxWidth()\
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 8.dp),\
                shape = RoundedCornerShape(24.dp),\
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)\
            ) {\
                Row(\
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),\
                    horizontalArrangement = Arrangement.SpaceBetween,\
                    verticalAlignment = Alignment.CenterVertically\
                ) {\
                    Text(\
                        "What is your\\ngoal for today?", \
                        fontWeight = FontWeight.ExtraBold,\
                        style = MaterialTheme.typography.headlineMedium,\
                        color = MaterialTheme.colorScheme.onPrimaryContainer,\
                        modifier = Modifier.weight(1f)\
                    )\
                    Text("👋", fontSize = 48.sp)\
                }\
            }\
        },' app/src/main/java/com/example/MainActivity.kt

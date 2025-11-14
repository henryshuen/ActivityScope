package com.example.myactivitylauncher.data

data class RecentItem(
    val packageName: String,
    val activityName: String,
    val label: String,
    val timeMillis: Long
)

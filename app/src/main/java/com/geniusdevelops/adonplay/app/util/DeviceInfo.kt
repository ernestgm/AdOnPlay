package com.geniusdevelops.adonplay.app.util

data class DeviceInfo(
    val totalRam: Long,
    val freeRam: Long,
    val cpuUsage: Double,
    val totalDisk: Long,
    val freeDisk: Long
)

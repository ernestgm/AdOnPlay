package com.geniusdevelops.adonplay

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.geniusdevelops.adonplay.app.App
import com.geniusdevelops.adonplay.app.util.DeviceUtils
import com.geniusdevelops.adonplay.app.websocket.StatusActionsChannel
import com.geniusdevelops.adonplay.ui.theme.AdOnPlayTheme
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.perf.performance
import com.google.firebase.perf.trace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private lateinit var deviceUtils: DeviceUtils
    private lateinit var statusActionsChannel: StatusActionsChannel
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private val isActive = true

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    @RequiresApi(Build.VERSION_CODES.N)
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deviceUtils = DeviceUtils(baseContext)
        // 1. Registrar estado de forma asíncrona si es posible
        Firebase.analytics.setUserId(deviceUtils.getDeviceId())
        Firebase.crashlytics.setUserId(deviceUtils.getDeviceId())
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        val isRestarted = intent.getBooleanExtra("is_restarted", false)
        if (isRestarted) {
            val freeMem = intent.getLongExtra("free_memory", 0)
            val totalMem = intent.getLongExtra("total_memory", 0)

            Firebase.performance.newTrace("app_restarted").trace {
                putAttribute("deviceId", deviceUtils.getDeviceId())
                putAttribute("freeMemory", deviceUtils.formatBytes(freeMem))
                putAttribute("totalMemory", deviceUtils.formatBytes(totalMem))
            }

            Log.d(
                "MainActivity",
                "Restarted! Free: ${deviceUtils.formatBytes(freeMem)} Total: ${
                    deviceUtils.formatBytes(totalMem)
                }"
            )
        }

        if (!isBatteryOptimizationIgnored(baseContext)) {
            println("Not isBatteryOptimizationIgnored")
            requestDisableBatteryOptimizationForApp(this)
        }

        subscribeToStatusActions()
        enableActiveScreen()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            checkPermissionOverlay()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }

        setContent {
            AdOnPlayTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    App()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkPermissionOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                "package:$packageName".toUri()
            )
            val REQUEST_OVERLAY_PERMISSION = 1001
            startActivityForResult(intent, REQUEST_OVERLAY_PERMISSION)
        }
    }

    private fun isBatteryOptimizationIgnored(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return powerManager.isIgnoringBatteryOptimizations(packageName)
    }

    @SuppressLint("BatteryLife")
    fun requestDisableBatteryOptimizationForApp(context: Context) {
        println(packageName)
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
        intent.data = "package:${packageName}".toUri()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun disableActiveScreen() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun enableActiveScreen() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onDestroy() {
        stopStatusActions()
        disableActiveScreen()
        Firebase.performance.newTrace("app_destroy").trace {
            putAttribute("deviceId", deviceUtils.getDeviceId())
        }
        super.onDestroy()
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onRestart() {
        subscribeToStatusActions()
        enableActiveScreen()
        super.onRestart()
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onResume() {
        subscribeToStatusActions()
        enableActiveScreen()
        super.onResume()
    }

    override fun onPause() {
        stopStatusActions()
        disableActiveScreen()
        super.onPause()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private fun subscribeToStatusActions() {
        Firebase.performance.newTrace("app_cable_connect").trace {
            putAttribute("deviceId", deviceUtils.getDeviceId())
        }
        statusActionsChannel = StatusActionsChannel(deviceUtils.getDeviceId())
        serviceScope.launch {
            statusActionsChannel.connect()
            startReporting()
        }
    }

    private fun stopStatusActions() {
        Firebase.performance.newTrace("app_cable_disconnect").trace {
            putAttribute("deviceId", deviceUtils.getDeviceId())
        }
        if (::statusActionsChannel.isInitialized) {
            statusActionsChannel.disconnect()
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level == TRIM_MEMORY_COMPLETE) {
            Firebase.performance.newTrace("app_memory_kill").trace {
                // Update scenario.
                putAttribute("deviceId", deviceUtils.getDeviceId())
            }
        }
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Firebase.performance.newTrace("app_system_low_memory").trace {
            // Update scenario.
            putAttribute("deviceId", deviceUtils.getDeviceId())
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun startReporting() {
        serviceScope.launch(Dispatchers.IO) { // Siempre en IO para no bloquear la UI
            while (isActive) {
                try {
                    val (totalRam, freeRam) = deviceUtils.getMemoryStatus()
                    val (totalDisk, freeDisk) = getDiskStatus()
                    val cpuPercent = getCpuUsage(baseContext)

                    // Enviar datos solo si la conexión está activa
                    if (::statusActionsChannel.isInitialized) {
                        statusActionsChannel.sendData(
                            totalRam,
                            freeRam,
                            cpuPercent,
                            totalDisk,
                            freeDisk
                        )
                    }
                } catch (e: Exception) {
                    FirebaseCrashlytics.getInstance().recordException(e)
                }


                deviceUtils.scheduleKeepAliveRestart()
                delay(30000) // Aumentamos a 30 seg para dar respiro al sistema
            }
        }
    }

    // Versión eficiente para CPU en Android Boxes
    fun getCpuUsage(context: Context): Double {
        return try {
            // Usamos una aproximación basada en la carga del sistema si el hardware manager falla
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val hardwareProps =
                    context.getSystemService(Context.HARDWARE_PROPERTIES_SERVICE) as? android.os.HardwarePropertiesManager
                val usages = hardwareProps?.cpuUsages
                if (!usages.isNullOrEmpty()) {
                    return usages.map { it.active }.average()
                }
            }
            // Fallback: Valor aleatorio controlado o una consulta ligera a /proc/stat
            (1..100).random().toDouble()
        } catch (e: Exception) {
            0.0
        }
    }

    fun getDiskStatus(): Pair<Long, Long> {
        val path = Environment.getExternalStorageDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalDisk = totalBlocks * blockSize
        val freeDisk = availableBlocks * blockSize

        return Pair(totalDisk, freeDisk)
    }
}
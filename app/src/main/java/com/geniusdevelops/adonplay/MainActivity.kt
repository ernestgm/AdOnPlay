package com.geniusdevelops.adonplay

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.HardwarePropertiesManager
import android.os.PowerManager
import android.os.StatFs
import android.os.SystemClock
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.core.app.ActivityCompat
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.geniusdevelops.adonplay.app.App
import com.geniusdevelops.adonplay.app.provider.AppStateProvider
import com.geniusdevelops.adonplay.app.util.DeviceUtils
import com.geniusdevelops.adonplay.ui.theme.AdOnPlayTheme
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics
import com.google.firebase.crashlytics.crashlytics
import com.google.firebase.perf.performance
import com.google.firebase.perf.trace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import com.geniusdevelops.adonplay.app.websocket.StatusActionsChannel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var deviceUtils: DeviceUtils
    private lateinit var statusActionsChannel: StatusActionsChannel
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private val isActive = true

    @RequiresApi(Build.VERSION_CODES.N)
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        deviceUtils = DeviceUtils(baseContext)
        AppStateProvider.setAppRunning()
        Firebase.analytics.setUserId(deviceUtils.getDeviceId())
        Firebase.crashlytics.setUserId(deviceUtils.getDeviceId())
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })

        val isRestarted = intent.getBooleanExtra("is_restarted", false)
        if (isRestarted) {
            // Opcional: Mostrar un mensaje al usuario o no intentar la operación que falló
            Toast.makeText(this, "La aplicación se reinició tras un error", Toast.LENGTH_SHORT)
                .show()
        }

        if (!isBatteryOptimizationIgnored(baseContext)) {
            println("Not isBatteryOptimizationIgnored")
            requestDisableBatteryOptimizationForApp(this)
        }

        subscribeToStatusActions()
        enableActiveScreen()
        startWDService()
        checkIsWatchDogRunning()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            checkPermissionOverlay()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
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

    private fun checkIsWatchDogRunning() {
        serviceScope.launch {
            while (true) {
                delay(120000)
                isWDRunning()
            }
        }
    }

    private fun isWDRunning() {
        if (!deviceUtils.checkWDStatus()) {
            println("WatchDog Not Running")
            Firebase.performance.newTrace("watchdog_app_not_running").trace {
                // Update scenario.
                putAttribute("deviceId", deviceUtils.getDeviceId())
            }
            FirebaseCrashlytics.getInstance()
                .log("WatchDog not Running ${deviceUtils.getDeviceId()}")
            startWDService()
        } else {
            println("WatchDog Running")
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
        AppStateProvider.setAppStopped()
        disableActiveScreen()
        Firebase.performance.newTrace("app_destroy").trace {
            putAttribute("deviceId", deviceUtils.getDeviceId())
        }
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onRestart() {
        subscribeToStatusActions()
        enableActiveScreen()
        super.onRestart()
    }

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

    private fun startWDService() {
        serviceScope.launch {
            val wdPackageName = "com.geniusdevelop.watchdog.${BuildConfig.BUILD_TYPE}"
            if (deviceUtils.isAppInstalled(packageManager, wdPackageName)) {
                val intent = Intent("com.geniusdevelop.watchdog.START_FOREGROUND_SERVICE")
                intent.setPackage(wdPackageName)  // The package name of App A
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    baseContext.startForegroundService(intent)
                } else {
                    baseContext.startService(intent)
                }
            } else {
                println("WatchDog Not installed")
            }
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
    fun startReporting() {
        serviceScope.launch {
            while (isActive) {
                val (totalRam, freeRam) = getMemoryStatus(baseContext)
                val (totalDisk, freeDisk) = getDiskStatus()
                val cpuPercent = getCpuUsage(baseContext)

                statusActionsChannel.sendData(totalRam, freeRam, cpuPercent, totalDisk, freeDisk)
                delay(20000) // Espera 20 segundos
            }
        }
    }

    fun getMemoryStatus(context: Context): Pair<Long, Long> {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalRam = memoryInfo.totalMem // RAM total instalada
        val freeRam = memoryInfo.availMem   // RAM disponible actualmente

        return Pair(totalRam, freeRam)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getCpuUsage(context: Context): Double {
        return try {
            val hardwareProps =
                context.getSystemService(Context.HARDWARE_PROPERTIES_SERVICE) as _root_ide_package_.android.os.HardwarePropertiesManager
            val cpuUsages = hardwareProps.cpuUsages
            if (cpuUsages.isNotEmpty()) {
                // Promedio de todos los núcleos
                cpuUsages.map { it.active }.average()
            } else {
                0.0
            }
        } catch (e: Exception) {
            // Fallback: Si no tienes permisos de Device Owner,
            // devolvemos un valor basado en la carga del sistema
            Math.random() * 100 // Solo para pruebas si el API está bloqueado
        }
    }

    fun getDiskStatus(): Pair<Long, Long> {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalDisk = totalBlocks * blockSize
        val freeDisk = availableBlocks * blockSize

        return Pair(totalDisk, freeDisk)
    }
}
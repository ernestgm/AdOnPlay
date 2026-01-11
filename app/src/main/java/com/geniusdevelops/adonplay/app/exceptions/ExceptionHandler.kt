package com.geniusdevelops.adonplay.app.exceptions

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Process
import androidx.annotation.RequiresPermission
import com.geniusdevelops.adonplay.app.util.DeviceUtils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlin.system.exitProcess

class MyExceptionHandler(
    private val context: Context,
    private val activityToStart: Class<*>
) : Thread.UncaughtExceptionHandler {

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        // 1. Enviar el error manualmente a Firebase
        // Podemos añadir llaves personalizadas para dar más contexto
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.setCustomKey("restarted_by_handler", true)
        crashlytics.setCustomKey("thread_name", thread.name)
        crashlytics.recordException(throwable)
        crashlytics.log("La aplicación se cerró inesperadamente y fue reiniciada por el Handler.")

        // 2. IMPORTANTE: Firebase necesita un momento para persistir el error en disco
        // antes de que matemos el proceso.
        Thread.sleep(500)

        val deviceUtils = DeviceUtils(context)
        deviceUtils.scheduleKeepAliveRestart()

        // 4. Terminar el proceso
        Process.killProcess(Process.myPid())
        exitProcess(10)
    }
}
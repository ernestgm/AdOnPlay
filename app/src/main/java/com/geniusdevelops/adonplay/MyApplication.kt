package com.geniusdevelops.adonplay

import android.app.Application
import android.content.Intent
import android.os.Build
import android.util.Log
import com.geniusdevelops.adonplay.app.exceptions.MyExceptionHandler
import com.geniusdevelops.adonplay.app.service.HighPriorityService

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Establecer el manejador por defecto para todos los hilos
        Thread.setDefaultUncaughtExceptionHandler(
            MyExceptionHandler(this, MainActivity::class.java)
        )

        startPriorityService()
    }

    fun startPriorityService() {
        val intent = Intent(this, HighPriorityService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }
}
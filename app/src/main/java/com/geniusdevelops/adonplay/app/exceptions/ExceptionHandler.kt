package com.geniusdevelops.adonplay.app.exceptions

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlin.system.exitProcess

class MyExceptionHandler(
    private val context: Context,
    private val activityToStart: Class<*>
) : Thread.UncaughtExceptionHandler {

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

        // 3. Configurar el reinicio
        val intent = Intent(context, activityToStart).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_NEW_TASK
            )
            putExtra("crash_detected", true)
            putExtra("is_restarted", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, pendingIntent)

        // 4. Terminar el proceso
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(10)
    }
}
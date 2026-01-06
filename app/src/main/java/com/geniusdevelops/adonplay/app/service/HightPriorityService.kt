package com.geniusdevelops.adonplay.app.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.geniusdevelops.adonplay.MainActivity

class HighPriorityService : Service() {

    private val CHANNEL_ID = "ForegroundServiceChannel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App en Alta Prioridad")
            .setContentText("Estamos trabajando para mantener todo en orden.")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Cambia por tu icono
            .setContentIntent(pendingIntent)
            // ESTA ES LA PROPIEDAD CORRECTA:
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()

        // IMPORTANTE: Iniciar en primer plano
        // En Android 14+ debes especificar el tipo de servicio (si aplica)
        startForeground(1, notification)

        // START_STICKY hace que el sistema intente recrear el servicio si lo mata
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Canal de Servicio Prioritario",
                NotificationManager.IMPORTANCE_LOW // Low para que no haga ruido cada vez
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }
}
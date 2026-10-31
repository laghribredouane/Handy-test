package com.example.telephony

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.StateFlow

/**
 * Android Service that uses CellularSignalMetricsCollector to continuously collect
 * cellular signal metrics (RSRP, RSRQ, SINR) in the background.
 */
class CellularMetricsService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var collector: CellularSignalMetricsCollector
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        val service: CellularMetricsService
            get() = this@CellularMetricsService

        val metrics: StateFlow<CellularMetrics>
            get() = collector.metrics

        val isCollecting: StateFlow<Boolean>
            get() = collector.isCollecting
    }

    override fun onCreate() {
        super.onCreate()
        collector = CellularSignalMetricsCollector(applicationContext, serviceScope)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = createNotification("Collecte des mesures cellulaires en cours...")
        startForeground(NOTIFICATION_ID, notification)

        collector.startCollecting()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onDestroy() {
        super.onDestroy()
        collector.stopCollecting()
        serviceScope.cancel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Mesures Réseau Cellulaire",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notification du service de télémétrie RF (RSRP, RSRQ, SINR)"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NetField Handy - Surveillance RF")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "cellular_metrics_channel"
        const val NOTIFICATION_ID = 101

        fun startService(context: Context) {
            val intent = Intent(context, CellularMetricsService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, CellularMetricsService::class.java)
            context.stopService(intent)
        }
    }
}

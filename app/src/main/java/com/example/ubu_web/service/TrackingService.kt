package com.example.ubu_web.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.location.Location
import android.os.BatteryManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ServiceCompat
import com.example.ubu_web.TrackingApplication
import com.example.ubu_web.data.local.LocationEntity
import com.example.ubu_web.util.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sqrt

/**
 * Servicio en primer plano que captura ubicaciones GPS continuamente
 * y las guarda en la base de datos local (Room).
 *
 * Incluye:
 *  - Captura de alta frecuencia (2 segundos)
 *  - Filtro de precisión (descarta puntos con error > 50m)
 *  - Filtro de saltos imposibles (velocidad > 150 km/h)
 */
class TrackingService : Service() {

    companion object {
        private const val TAG = "TrackingService"
        const val ACTION_START = "com.example.ubu_web.action.START"
        const val ACTION_STOP = "com.example.ubu_web.action.STOP"

        fun start(context: Context) {
            val intent = Intent(context, TrackingService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, TrackingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Estado para el filtro de saltos imposibles
    private var lastPoint: Location? = null
    private var lastPointTime: Long = 0L

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            handleNewLocation(location)
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        NotificationHelper.createChannel(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                Log.i(TAG, "Deteniendo servicio")
                stopTracking()
                return START_NOT_STICKY
            }
            else -> startTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        Log.i(TAG, "Iniciando rastreo")

        val notification = NotificationHelper.buildNotification(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                Constants.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(Constants.NOTIFICATION_ID, notification)
        }

        requestLocationUpdates()
    }

    private fun requestLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            Constants.LOCATION_INTERVAL_MS
        )
            .setMinUpdateDistanceMeters(Constants.LOCATION_MIN_DISTANCE_M)
            .setWaitForAccurateLocation(false)
            .build()

        try {
            fusedLocationClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.i(TAG, "Solicitadas actualizaciones de alta precisión cada ${Constants.LOCATION_INTERVAL_MS}ms")
        } catch (e: SecurityException) {
            Log.e(TAG, "Sin permisos: ${e.message}")
            stopSelf()
        }
    }

    private fun handleNewLocation(location: Location) {
        val ahora = System.currentTimeMillis()

        // ─── Filtro 1: Precisión ────────────────────────────
        if (location.hasAccuracy() && location.accuracy > Constants.MAX_ACCURACY_M) {
            Log.w(TAG, "⚠️ Punto descartado por baja precisión: ${location.accuracy.toInt()}m")
            return
        }

        // ─── Filtro 2: Salto imposible ──────────────────────
        if (lastPoint != null) {
            val distancia = lastPoint!!.distanceTo(location)
            val tiempoSeg = (ahora - lastPointTime) / 1000.0
            val velocidadKmh = if (tiempoSeg > 0) (distancia / tiempoSeg) * 3.6 else 0.0

            if (velocidadKmh > Constants.MAX_SPEED_KMH) {
                Log.w(TAG, "⚠️ Salto descartado: ${distancia.toInt()}m en ${"%.1f".format(tiempoSeg)}s = ${velocidadKmh.toInt()}km/h")
                return
            }
        }

        Log.d(TAG, "✅ Ubicación: ${location.latitude}, ${location.longitude} (±${location.accuracy.toInt()}m)")

        val app = application as TrackingApplication

        val entity = LocationEntity(
            latitude = location.latitude,
            longitude = location.longitude,
            timestamp = ahora,
            accuracy = if (location.hasAccuracy()) location.accuracy else null,
            speed = if (location.hasSpeed()) location.speed else null,
            heading = if (location.hasBearing()) location.bearing else null,
            batteryLevel = getBatteryLevel()
        )

        // Actualizar referencia para el próximo filtro
        lastPoint = location
        lastPointTime = ahora

        serviceScope.launch {
            val id = app.repository.saveLocation(entity)
            Log.d(TAG, "Guardado local id=$id")

            val success = app.repository.syncPendingLocations()
            Log.d(TAG, if (success) "Sincronizado con servidor" else "Pendiente (sin red)")
        }
    }

    private fun getBatteryLevel(): Int? {
        return try {
            val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        } catch (_: Exception) {
            null
        }
    }

    private fun stopTracking() {
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (_: Exception) {}
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        } catch (_: Exception) {}
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
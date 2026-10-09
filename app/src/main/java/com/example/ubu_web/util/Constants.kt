package com.example.ubu_web.util

import android.os.Build

/**
 * Constantes globales de la aplicación.
 *
 * DEVICE_NAME y DEVICE_LABEL son dinámicos — se generan automáticamente
 * a partir del fabricante, modelo y ID de compilación del dispositivo.
 * Esto permite instalar el MISMO APK en varios dispositivos y que cada
 * uno aparezca independiente en el dashboard.
 */
object Constants {

    /**
     * URL base del servidor.
     *   - Emulador Android + servidor local   → "http://10.0.2.2:3000"
     *   - Dispositivo físico + servidor remoto → "http://65.52.194.124"
     */
    const val SERVER_BASE_URL = "http://65.52.194.124"

    /** Endpoint para enviar ubicaciones. */
    const val ENDPOINT_LOCATIONS = "/api/locations"

    // ============================================================
    // IDENTIFICACIÓN DEL DISPOSITIVO
    // ============================================================
    val DEVICE_NAME: String
        get() {
            val rawName = "${Build.MANUFACTURER}_${Build.MODEL}_${Build.ID.takeLast(6)}"
            return rawName.replace(Regex("[^A-Za-z0-9_]"), "_")
        }

    val DEVICE_LABEL: String
        get() = "${Build.MANUFACTURER} ${Build.MODEL}".trim()

    // ============================================================
    // CONFIGURACIÓN DE CAPTURA GPS
    // ============================================================

    /**
     * Intervalo entre capturas de ubicación (milisegundos).
     * 2_000 = 2 segundos (alta frecuencia, más batería)
     */
    const val LOCATION_INTERVAL_MS = 2_000L

    /**
     * Distancia mínima entre capturas (metros).
     * 0f = capturar siempre, sin importar el desplazamiento.
     */
    const val LOCATION_MIN_DISTANCE_M = 0f

    /**
     * Precisión máxima aceptable en metros.
     * Puntos con error mayor a este valor se descartan.
     */
    const val MAX_ACCURACY_M = 50f

    /**
     * Velocidad máxima permitida entre puntos consecutivos (km/h).
     * Sirve para descartar saltos imposibles (outliers).
     */
    const val MAX_SPEED_KMH = 150.0

    // ============================================================
    // CONFIGURACIÓN DE SINCRONIZACIÓN
    // ============================================================

    /**
     * Intervalo de sincronización en segundo plano (minutos).
     * Mínimo aceptado por Android: 15 minutos.
     */
    const val SYNC_INTERVAL_MINUTES = 15L

    // ============================================================
    // NOTIFICACIONES
    // ============================================================

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_ID = 1001

    // ============================================================
    // PREFERENCIAS (SharedPreferences)
    // ============================================================

    const val PREFS_NAME = "tracking_prefs"
    const val PREF_IS_TRACKING = "is_tracking"
}
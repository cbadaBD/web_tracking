package com.example.ubu_web.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.ubu_web.util.Constants

/**
 * Receiver que se ejecuta tras un reinicio del dispositivo.
 * Si el rastreo estaba activo antes de apagar, lo reactiva.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON") {

            val prefs = context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)
            val wasTracking = prefs.getBoolean(Constants.PREF_IS_TRACKING, false)

            if (wasTracking) {
                Log.i(TAG, "Reiniciando rastreo tras boot")
                TrackingService.start(context)
            }
        }
    }
}
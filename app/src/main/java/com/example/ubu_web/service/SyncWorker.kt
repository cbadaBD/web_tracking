package com.example.ubu_web.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.ubu_web.TrackingApplication

/**
 * Worker de WorkManager que sincroniza las ubicaciones pendientes
 * con el servidor. Se ejecuta cuando hay red disponible.
 */
class SyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "SyncWorker"
    }

    override suspend fun doWork(): Result {
        Log.i(TAG, "Sincronizando ubicaciones pendientes")
        return try {
            val app = applicationContext as TrackingApplication
            val success = app.repository.syncPendingLocations()
            if (success) {
                Log.i(TAG, "Sincronización exitosa")
                Result.success()
            } else {
                Log.w(TAG, "Reintentando más tarde")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error: ${e.message}")
            Result.retry()
        }
    }
}
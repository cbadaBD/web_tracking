package com.example.ubu_web

import android.app.Application
import com.example.ubu_web.data.local.AppDatabase
import com.example.ubu_web.data.repository.LocationRepository
import com.example.ubu_web.service.SyncScheduler

/**
 * Clase Application personalizada. Se crea una sola vez al arrancar
 * la app y expone el repositorio como singleton.
 */
class TrackingApplication : Application() {

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
    val repository: LocationRepository by lazy { LocationRepository(database.locationDao()) }

    override fun onCreate() {
        super.onCreate()

        // Programar sincronización periódica en segundo plano (cada 15 min)
        SyncScheduler.schedulePeriodicSync(this)
    }
}
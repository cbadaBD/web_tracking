package com.example.ubu_web.data.repository

import com.example.ubu_web.data.local.LocationDao
import com.example.ubu_web.data.local.LocationEntity
import com.example.ubu_web.data.remote.ApiClient
import com.example.ubu_web.data.remote.dto.LocationPayload
import com.example.ubu_web.data.remote.dto.PointDto
import com.example.ubu_web.util.Constants
import kotlinx.coroutines.flow.Flow

class LocationRepository(private val dao: LocationDao) {

    val pendingCount: Flow<Int> = dao.countByStatus()

    suspend fun saveLocation(entity: LocationEntity): Long = dao.insert(entity)

    suspend fun getPendingLocations(limit: Int = 500): List<LocationEntity> =
        dao.getPending(limit = limit)

    suspend fun syncPendingLocations(): Boolean {
        val pending = dao.getPending(limit = 500)
        if (pending.isEmpty()) return true

        val points = pending.map {
            PointDto(
                lat = it.latitude,
                lng = it.longitude,
                timestamp = it.timestamp,
                accuracy = it.accuracy,
                speed = it.speed,
                heading = it.heading,
                battery = it.batteryLevel
            )
        }

        val payload = LocationPayload(
            device_name = Constants.DEVICE_NAME,
            device_label = Constants.DEVICE_LABEL,
            points = points
        )

        val success = ApiClient.sendLocations(payload)
        if (success) {
            dao.markAsSynced(pending.map { it.id })
        }
        return success
    }
}
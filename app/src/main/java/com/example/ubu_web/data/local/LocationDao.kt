package com.example.ubu_web.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Insert
    suspend fun insert(location: LocationEntity): Long

    @Query("""
        SELECT * FROM locations
        WHERE status = :status
        ORDER BY timestamp ASC
        LIMIT :limit
    """)
    suspend fun getPending(
        status: String = LocationEntity.STATUS_PENDING,
        limit: Int = 500
    ): List<LocationEntity>

    @Query("SELECT COUNT(*) FROM locations WHERE status = :status")
    fun countByStatus(status: String = LocationEntity.STATUS_PENDING): Flow<Int>

    @Query("""
        UPDATE locations
        SET status = :newStatus
        WHERE id IN (:ids)
    """)
    suspend fun markAsSynced(
        ids: List<Long>,
        newStatus: String = LocationEntity.STATUS_SYNCED
    )

    @Query("SELECT COUNT(*) FROM locations")
    suspend fun countAll(): Int
}
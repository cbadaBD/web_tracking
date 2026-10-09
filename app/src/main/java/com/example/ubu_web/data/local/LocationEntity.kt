package com.example.ubu_web.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Representa un punto GPS almacenado localmente en Room.
 *
 * Estados:
 *  - PENDING: aún no se ha enviado al servidor
 *  - SYNCED: ya fue enviada y confirmada
 */
@Entity(tableName = "locations")
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val latitude: Double,
    val longitude: Double,

    /** Timestamp Unix en milisegundos cuando se capturó la ubicación. */
    val timestamp: Long,

    /** Precisión en metros (del GPS). */
    val accuracy: Float? = null,

    /** Velocidad en m/s. */
    val speed: Float? = null,

    /** Dirección en grados (0-360). */
    val heading: Float? = null,

    /** Nivel de batería (0-100). */
    val batteryLevel: Int? = null,

    /** Estado de sincronización. */
    val status: String = STATUS_PENDING
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_SYNCED = "SYNCED"
    }
}
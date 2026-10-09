package com.example.ubu_web.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ubu_web.TrackingApplication
import com.example.ubu_web.service.SyncScheduler
import com.example.ubu_web.service.TrackingService
import com.example.ubu_web.util.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class TrackingViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = (app as TrackingApplication).repository
    private val prefs = app.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    private val _isTracking = MutableStateFlow(
        prefs.getBoolean(Constants.PREF_IS_TRACKING, false)
    )
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    val pendingCount: StateFlow<Int> = repository.pendingCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun toggleTracking() {
        val newValue = !_isTracking.value
        _isTracking.value = newValue
        prefs.edit().putBoolean(Constants.PREF_IS_TRACKING, newValue).apply()

        if (newValue) {
            TrackingService.start(getApplication())
        } else {
            TrackingService.stop(getApplication())
            SyncScheduler.scheduleImmediateSync(getApplication())
        }
    }

    fun syncNow() {
        SyncScheduler.scheduleImmediateSync(getApplication())
    }
}

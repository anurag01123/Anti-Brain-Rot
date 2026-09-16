package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import com.example.data.AppDatabase
import com.example.data.AppRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CallMonitorReceiver : BroadcastReceiver() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    
    // Static state to avoid duplicate events
    companion object {
        private var lastState = TelephonyManager.EXTRA_STATE_IDLE
        private var lastNumber: String? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        
        val db = AppDatabase.getDatabase(context)
        val repository = AppRepository(db.appDao())

        // Depending on Android version, outgoing calls can be detected via ACTION_NEW_OUTGOING_CALL
        // or PHONE_STATE action. Here we check the state.
        if (intent.action == "android.intent.action.NEW_OUTGOING_CALL") {
            val savedNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER)
            if (!savedNumber.isNullOrEmpty()) {
                scope.launch {
                    repository.markContactCalled(savedNumber, System.currentTimeMillis())
                }
            }
        } else if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER) 
            
            // Note: EXTRA_INCOMING_NUMBER is populated for incoming and occasionally outgoing.
            // A more robust modern approach involves CallLog observer or InCallService,
            // but for simplicity we rely on NEW_OUTGOING_CALL and state OFFHOOK.
            if (state == TelephonyManager.EXTRA_STATE_OFFHOOK && lastState == TelephonyManager.EXTRA_STATE_IDLE) {
                // Outgoing call started or incoming accepted.
                // If we captured NEW_OUTGOING_CALL, we already updated DB.
            }
            if (state != null) {
                lastState = state
            }
        }
    }
}

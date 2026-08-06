package com.example.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class BatteryStatus(
    val percentage: Int = 100,
    val isCharging: Boolean = false
) {
    val isLowBattery: Boolean get() = percentage <= 20 && !isCharging
}

object BatteryUtils {

    fun getBatteryStatus(context: Context): BatteryStatus {
        return try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatusIntent = context.registerReceiver(null, intentFilter)
            val level = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = batteryStatusIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1

            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val pct = if (level >= 0 && scale > 0) {
                ((level.toFloat() / scale.toFloat()) * 100).toInt()
            } else 100

            BatteryStatus(percentage = pct, isCharging = isCharging)
        } catch (e: Exception) {
            BatteryStatus(percentage = 100, isCharging = false)
        }
    }

    fun observeBatteryStatus(context: Context): Flow<BatteryStatus> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL
                    val pct = if (level >= 0 && scale > 0) {
                        ((level.toFloat() / scale.toFloat()) * 100).toInt()
                    } else 100
                    trySend(BatteryStatus(percentage = pct, isCharging = isCharging))
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        try {
            context.registerReceiver(receiver, filter)
        } catch (e: Exception) {
            // Ignore
        }
        trySend(getBatteryStatus(context))

        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}

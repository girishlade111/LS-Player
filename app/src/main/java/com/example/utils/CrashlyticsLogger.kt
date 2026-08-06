package com.example.utils

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

object CrashlyticsLogger {

    fun logException(throwable: Throwable, tag: String = "AppException") {
        Log.e(tag, "Logged Exception: ${throwable.message}", throwable)
        try {
            FirebaseCrashlytics.getInstance().apply {
                log("[$tag] ${throwable.localizedMessage ?: throwable.message ?: "Runtime Exception"}")
                recordException(throwable)
            }
        } catch (e: Exception) {
            Log.w(tag, "Firebase Crashlytics not initialized or google-services.json missing: ${e.message}")
        }
    }

    fun log(message: String) {
        Log.i("CrashlyticsLogger", message)
        try {
            FirebaseCrashlytics.getInstance().log(message)
        } catch (e: Exception) {
            // Ignore if Firebase unavailable
        }
    }

    fun setCustomKey(key: String, value: String) {
        try {
            FirebaseCrashlytics.getInstance().setCustomKey(key, value)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun setCustomKey(key: String, value: Boolean) {
        try {
            FirebaseCrashlytics.getInstance().setCustomKey(key, value)
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun isAvailable(): Boolean {
        return try {
            FirebaseCrashlytics.getInstance()
            true
        } catch (e: Exception) {
            false
        }
    }
}

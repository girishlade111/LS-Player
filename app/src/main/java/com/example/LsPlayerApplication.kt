package com.example

import android.app.Application
import android.util.Log
import com.example.utils.CrashlyticsLogger

class LsPlayerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        setupCrashlyticsExceptionHandler()
    }

    private fun setupCrashlyticsExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("LsPlayerApplication", "Uncaught exception on thread ${thread.name}", throwable)
            CrashlyticsLogger.log("Uncaught Exception on thread: ${thread.name}")
            CrashlyticsLogger.setCustomKey("last_crash_thread", thread.name)
            CrashlyticsLogger.logException(throwable, tag = "UncaughtException")

            // Re-delegate to default handler so app handles system crash cleanly
            defaultHandler?.uncaughtException(thread, throwable)
        }
        CrashlyticsLogger.log("LsPlayerApplication initialized with Firebase Crashlytics monitoring.")
    }
}

package com.paxonf.sharesummarizer

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.StrictMode

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Set strict mode policies for debugging - only in debug builds
        val isDebug = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

        if (isDebug) {
            StrictMode.setThreadPolicy(
                    StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build()
            )

            StrictMode.setVmPolicy(
                    StrictMode.VmPolicy.Builder()
                            .detectLeakedSqlLiteObjects()
                            .detectLeakedClosableObjects()
                            .penaltyLog()
                            .build()
            )
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        // Clean up resources when memory is low
    }

    override fun onLowMemory() {
        super.onLowMemory()
        // Handle low memory situations
    }
}

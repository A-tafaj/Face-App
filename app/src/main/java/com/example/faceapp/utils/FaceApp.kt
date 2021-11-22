package com.example.faceapp.utils

import android.app.Application
import android.util.Log
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig

class FaceApp : Application(), CameraXConfig.Provider {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        private lateinit var instance: FaceApp

        @JvmName("getInstance1")
        fun getInstance(): FaceApp {
            return instance
        }
    }

    /** Sets the minimum logging level to be used for CameraX logs.*/
    override fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig()).setMinimumLoggingLevel(Log.ERROR).build()
    }
}
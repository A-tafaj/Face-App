package com.example.faceapp.utils

import android.app.Application

class FaceApp : Application(){
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object{
        private lateinit var instance: FaceApp

        @JvmName("getInstance1")
        fun getInstance(): FaceApp {
            return instance
        }
    }
}
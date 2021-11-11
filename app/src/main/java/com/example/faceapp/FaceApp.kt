package com.example.faceapp

import android.app.Application

class FaceApp : Application(){

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object{
        lateinit var instance: FaceApp

        @JvmName("getInstance1")
        fun getInstance(): FaceApp {
            return instance
        }
    }
}
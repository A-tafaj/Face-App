package com.example.faceapp

import android.app.Application

class FaceApp : Application(){

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object{
        var instance: FaceApp? = null

        @JvmName("getInstance1")
        fun getInstance(): FaceApp {
            return instance!!
        }
    }

}
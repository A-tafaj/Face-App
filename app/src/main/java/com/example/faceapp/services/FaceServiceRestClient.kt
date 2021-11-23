package com.example.faceapp.services

import com.microsoft.projectoxford.face.FaceServiceRestClient

object FaceServiceRestClient {
    private const val API_KEY = "a599f5e2faa24c0baed15f54e715d2e3"
    private const val API_ENDPOINT = "https://fiek.cognitiveservices.azure.com/face/v1.0"

    lateinit var instace: FaceServiceRestClient

    fun getFaceServiceRestClient(): FaceServiceRestClient {
        if (!this::instace.isInitialized) {
            return FaceServiceRestClient(API_ENDPOINT, API_KEY).also { instace = it }
        }
        return instace
    }
}

package com.example.faceapp.services

import com.microsoft.projectoxford.face.FaceServiceRestClient

object FaceServiceRestClient {
    private const val API_KEY = "your_key"
    private const val API_ENDPOINT = "https://your_tag.cognitiveservices.azure.com/face/v1.0/"

    lateinit var instace: FaceServiceRestClient

    fun getFaceServiceRestClient(): FaceServiceRestClient {
        if (!this::instace.isInitialized) {
            return FaceServiceRestClient(API_ENDPOINT, API_KEY).also { instace = it }
        }
        return instace
    }
}

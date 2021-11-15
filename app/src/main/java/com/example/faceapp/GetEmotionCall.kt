package com.example.faceapp

import android.widget.ImageView

class GetEmotionCall {
    lateinit var img: ImageView

    fun GetEmotionCall(img: ImageView) {
        this.img = img
    } // this function is called before the API call is made

}
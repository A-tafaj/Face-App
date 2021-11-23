package com.example.faceapp.utils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

object GenericMethods {
    fun decimalFormat(number: Double): String {
        return DecimalFormat("#.#", DecimalFormatSymbols(Locale.US)).format(number)
    }
}
package com.example.volook.shared.helpers

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.pow
import kotlin.math.sqrt

object Helpers {

    fun millisToLocaleDateTime(millis: Long): LocalDateTime{
        val zoneId = ZoneId.of("Europe/Rome")
        val instant = Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDateTime()
        return instant
    }

    fun formatDate(dateTime: LocalDateTime): String{
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        val formattedDateTime = dateTime.format(formatter)
        return formattedDateTime;
    }

    fun generateQRCode(text: String): Bitmap? {
        val width = 500
        val height = 500

        val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, null)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }

    fun euclideanDistance(x1: Double, x2: Double, y1: Double, y2: Double): Double{
        return sqrt((x2 - x1).pow(2) + (y2 - y1).pow(2))
    }

    fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
        return emailRegex.matches(email)
    }

    fun displayMessage(view: TextView, message: Int){
        view.setText(message)
        view.visibility = View.VISIBLE
        view.animate()
            .setDuration(3000)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view.visibility = View.GONE
                }
            })
            .start()
    }
}
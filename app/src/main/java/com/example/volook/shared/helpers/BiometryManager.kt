package com.example.volook.shared.helpers

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometryManager {
    fun checkBiometricSensor(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        var isBiometricSensorAvailable: Boolean = false
        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS ->{
                Log.d("BIOMETRIC", "L'autenticazione biometrica è supportata.")
                isBiometricSensorAvailable = true
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                Log.e("BIOMETRIC", "Il dispositivo non ha un sensore biometrico.")
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                Log.e("BIOMETRIC", "Il sensore biometrico non è attualmente disponibile.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                Log.e("BIOMETRIC", "L'utente non ha registrato alcuna impronta digitale.")
        }
        return isBiometricSensorAvailable
    }

    fun authenticateWithBiometry(context: FragmentActivity, onSuccessAction: ()-> Any, onFailedAction: ()->Any, onErrorAction: ()->Any){
        val executor = ContextCompat.getMainExecutor(context)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccessAction()
            }
            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onFailedAction()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onErrorAction()
            }
        }
        val biometricPrompt = BiometricPrompt(context, executor, callback)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autenticazione biometrica")
            .setSubtitle("Posiziona il dito sul sensore")
            .setNegativeButtonText("Annulla")
            .build()
        biometricPrompt.authenticate(promptInfo)
    }
}
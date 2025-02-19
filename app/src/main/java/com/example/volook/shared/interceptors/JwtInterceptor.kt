package com.example.volook.shared.interceptors
import android.util.Log
import com.example.volook.MainActivity
import com.example.volook.shared.services.JwtService
import okhttp3.*

class JwtInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = JwtService.readJwt(MainActivity.instance)
        val tokenExpired = JwtService.isExpired(MainActivity.instance)
        var newRequest: Request
        if(token!=null && token.isNotBlank() && !tokenExpired){
            newRequest = chain.request().newBuilder()
                .header("Accept", "application/json")
                .addHeader("Authorization", "Bearer $token")
                .build()
        }else{
            newRequest = chain.request().newBuilder()
                .header("Accept", "application/json")
                .build()
        }

        Log.v("INTERCEPTOR", newRequest.toString());
        return chain.proceed(newRequest)
    }
}
package com.example.volook.shared.services

import android.content.Context
import android.content.SharedPreferences
import com.example.volook.shared.configs.Constants
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.serialization.json.Json
import com.google.gson.Gson
import java.time.Instant
import java.util.Date

object JwtService {

    fun readJwt(context: Context):String{
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString(Constants.JWT_KEY,"")
        return token.toString()
    }

    fun storeJwt(context: Context,token:String):Boolean {
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val saved:Boolean = editor.putString(Constants.JWT_KEY,token).commit()
        return saved
    }

    fun removeJwt(context: Context): Boolean{
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val removed: Boolean = editor.remove(Constants.JWT_KEY).commit()
        return removed
    }

    fun getRole(context: Context):String{
        val tokenBase64 = readJwt(context)
        if(tokenBase64.isBlank()){
            return ""
        }
        val token: Jwt = convertFromBase64ToJwt(tokenBase64)
        return token.role;
    }

    fun getId(context: Context):String{
        val tokenBase64 = readJwt(context)
        if(tokenBase64.isBlank()){
            return ""
        }
        val token: Jwt = convertFromBase64ToJwt(tokenBase64)
        return token.sub;
    }

    private fun getExp(context: Context):Long{
        val tokenBase64 = readJwt(context)
        if(tokenBase64.isBlank()){
            return 0
        }
        val token: Jwt = convertFromBase64ToJwt(tokenBase64)
        return token.exp;
    }

    fun isExpired(context: Context): Boolean{
        val tokenBase64 = readJwt(context)
        if (tokenBase64.isBlank()){
            return true
        }
        val token: Jwt = convertFromBase64ToJwt(tokenBase64)
        val exp = token.exp
        val isExpired = Instant.ofEpochSecond(JwtService.getExp(context)).isBefore(Date().toInstant())
        return isExpired
    }

    fun isLogged(context: Context):Boolean{
        val token = readJwt(context)
        return token.isNotBlank()
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun convertFromBase64ToJwt(token:String): Jwt{
        val jsonSerializer = Json
        val gson = Gson()

        val userDataBase64: String = token.split('.')[1]
        val decodedData = Base64.decode(userDataBase64)
        val stringData = String(decodedData, Charsets.UTF_8)
        val jwt: Jwt = gson.fromJson(stringData, Jwt::class.java)
        return jwt
    }

    data class Jwt(val sub:String, val role:String, val exp:Long)
}
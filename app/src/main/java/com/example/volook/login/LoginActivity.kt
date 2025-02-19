package com.example.volook.login

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.volook.MainActivity
import com.example.volook.R
import com.example.volook.shared.api.user.JwtDto
import com.example.volook.shared.api.user.LoginDto
import com.example.volook.shared.configs.Constants
import com.example.volook.shared.helpers.BiometryManager
import com.example.volook.shared.helpers.Helpers
import com.example.volook.shared.services.ApiClient
import com.example.volook.shared.services.JwtService
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var errorMessage: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = this@LoginActivity.getSharedPreferences(
            Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onStart() {
        super.onStart()
        //UI SRC
        val emailAddressField: EditText = findViewById<EditText>(R.id.login_email_address_field)
        val passwordField: EditText = findViewById<EditText>(R.id.login_password_field)
        errorMessage = findViewById<TextView>(R.id.login_error_msg)
        val rememberMeCheckbox: CheckBox = findViewById<CheckBox>(R.id.login_checkbox_remember_me)
        val signInButton: Button = findViewById<Button>(R.id.login_button_login)
        val signUpButton: Button = findViewById<Button>(R.id.login_button_signup)
        val fingerprintButton: Button = findViewById<Button>(R.id.login_button_fingerprint)
        val keepAccess = sharedPreferences.getString(Constants.KEEP_ACCESS,"false")
        val isBiometricSensorAvailable:Boolean = BiometryManager.checkBiometricSensor(this)

        if(keepAccess.toBoolean() && isBiometricSensorAvailable){
            fingerprintButton.visibility = View.VISIBLE
        }else{
            fingerprintButton.visibility = View.INVISIBLE
        }

        signInButton.setOnClickListener{
            //Getting data
            val emailAddress: String = emailAddressField.text.toString()
            val password: String = passwordField.text.toString()
            val keepAccessCheck: Boolean = rememberMeCheckbox.isChecked
            val loginDto: LoginDto = LoginDto(emailAddress,password)
            //Api Call
            if(emailAddress.isNullOrBlank() || password.isNullOrBlank()){
                Helpers.displayMessage(errorMessage,R.string.fill_in_all_field_msg)
            }else{
                login(loginDto,keepAccessCheck)
            }
        }

        signUpButton.setOnClickListener{
            val emailAddress: String = emailAddressField.text.toString()
            val password: String = passwordField.text.toString()
            val keepAccessCheck: Boolean = rememberMeCheckbox.isChecked
            val loginDto: LoginDto = LoginDto(emailAddress, password)
            //Api Call
            if(password.isNullOrBlank()){
                Helpers.displayMessage(errorMessage,R.string.fill_in_all_field_msg)
            }else if(emailAddress.isNullOrBlank() || !Helpers.isValidEmail(emailAddress)){
                Helpers.displayMessage(errorMessage,R.string.login_invalid_email_format)
            }else{
                signUp(loginDto, keepAccessCheck)
            }
        }

        fingerprintButton.setOnClickListener{
            loginWithFingerprint()
        }
    }

    private fun login(loginDto:LoginDto,keepAccess:Boolean){
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val call = ApiClient.userService.login(loginDto)
                call.enqueue(object : Callback<JwtDto> {
                    override fun onResponse(call: Call<JwtDto>, response: Response<JwtDto>) {
                        if (response.isSuccessful) {
                            val jwtDto = response.body() as JwtDto
                            val token = jwtDto.jwt
                            Log.v("TOKEN",token)
                            if(token!!.isNotBlank()){
                                JwtService.storeJwt(this@LoginActivity, token)
                                val editor = sharedPreferences.edit()
                                val keepAccessSave = editor.putString(Constants.KEEP_ACCESS,keepAccess.toString()).commit()
                                val homeActivity = Intent(this@LoginActivity, MainActivity::class.java).putExtra(
                                    Constants.FIRST_STARTUP, false
                                )
                                startActivity(homeActivity)
                                finish()
                            }
                        } else {
                            Log.v("LOGIN_ERROR_BODY", response.errorBody()!!.string())
                            Helpers.displayMessage(errorMessage,R.string.login_wrong_credentials)
                        }
                    }

                    override fun onFailure(call: Call<JwtDto>, t: Throwable) {
                        Log.v("LOGIN_FAILURE",t.toString())
                        Helpers.displayMessage(errorMessage,R.string.login_wrong_credentials)
                    }
                })
                //withContext(Dispatchers.Main) {}
            }catch (e:Exception){
                withContext(Dispatchers.Main) {
                    Log.v("LOGIN_EXCEPTION ",e.toString())
                    Helpers.displayMessage(errorMessage,R.string.server_error)
                }
            }
        }
    }

    private fun signUp(loginDto:LoginDto,keepAccess: Boolean){
        CoroutineScope(Dispatchers.IO).launch {
            try{
                val call = ApiClient.userService.signUp(loginDto)
                call.enqueue(object : Callback<JwtDto> {

                    override fun onResponse(call: Call<JwtDto>, response: Response<JwtDto>) {
                        if (response.isSuccessful) {
                            val jwtDto = response.body() as JwtDto
                            val token = jwtDto.jwt
                            Log.v("TOKEN",token)
                            if(token!!.isNotBlank()){
                                JwtService.storeJwt(this@LoginActivity, token)
                                Toast.makeText(this@LoginActivity,R.string.login_signup_success, Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Log.v("SIGN_UP_ERROR_BODY", response.errorBody()!!.string())
                            Helpers.displayMessage(errorMessage,R.string.login_signup_failed)
                        }
                    }

                    override fun onFailure(call: Call<JwtDto>, t: Throwable) {
                        Log.v("SIGN_UP_FAILURE",t.toString())
                        Helpers.displayMessage(errorMessage,R.string.server_error)
                    }
                })
                //withContext(Dispatchers.Main) {}
            }catch (e:Exception){
                withContext(Dispatchers.Main) {
                    Log.v("SIGN_UP_EXCEPTION ",e.toString())
                    Helpers.displayMessage(errorMessage,R.string.server_error)
                }
            }
        }
    }

    private fun loginWithFingerprint(){
        val isBiometricSensorAvailable:Boolean = BiometryManager.checkBiometricSensor(this)
        if(isBiometricSensorAvailable){
            val onSuccessAction = {
                val homeActivity = Intent(this@LoginActivity, MainActivity::class.java).putExtra(
                    Constants.FIRST_STARTUP, false
                )
                startActivity(homeActivity)
                finish()
            }
            val onFailedAction = {
                Toast.makeText(this,"Impronta non riconosciuta!", Toast.LENGTH_SHORT).show()
            }
            val onErrorAction = {
                Toast.makeText(this,"Autenticazione rifiutata!", Toast.LENGTH_SHORT).show()
            }
            BiometryManager.authenticateWithBiometry(this@LoginActivity,onSuccessAction,onFailedAction, onErrorAction )
        }
    }


}
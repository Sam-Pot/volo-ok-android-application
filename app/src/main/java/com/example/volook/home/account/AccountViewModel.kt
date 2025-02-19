package com.example.volook.home.account

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.volook.MainActivity
import com.example.volook.shared.api.user.User
import com.example.volook.shared.configs.Constants
import com.example.volook.shared.configs.StatusCode
import com.example.volook.shared.helpers.Helpers
import com.example.volook.shared.services.ApiClient
import com.example.volook.shared.services.JwtService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Date

class AccountViewModel : ViewModel() {
    private var _user = MutableLiveData<User>()
    var statusMessage = MutableLiveData<StatusCode>()

    private val sharedPreferences: SharedPreferences = MainActivity.instance.getSharedPreferences(
        Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    init {
        getData()
    }

    fun getData(){
        viewModelScope.launch {
            try{
                val call = ApiClient.userService.findOne()
                call.enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            val data = response.body() as User
                            _user.value = data
                            Log.v("USER_SERVICE_FIND_ONE",data.toString())
                        } else {
                            Log.v("USER_SERVICE_FIND_ONE_ERROR", response.errorBody()!!.string())
                            statusMessage.value = StatusCode.ERROR
                        }
                    }
                    override fun onFailure(call: Call<User>, t: Throwable) {
                        Log.v("USER_SERVICE_FIND_ONE_FAILURE",t.toString())
                        statusMessage.value = StatusCode.FAILURE
                    }
                })
                //withContext(Dispatchers.Main) {}
            }catch (e:Exception){
                withContext(Dispatchers.Main) {
                    Log.v("USER_SERVICE_FIND_ONE_EXCEPTION ",e.toString())
                    statusMessage.value = StatusCode.EXCEPTION
                }
            }
        }
    }

    fun update(userData:User){
        viewModelScope.launch {
            try{
                val call = ApiClient.userService.update(userData)
                call.enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            val data = response.body() as User
                            _user.value = data
                            Log.v("UPDATE USER",data.toString())
                            statusMessage.value = StatusCode.SUCCESS
                        } else {
                            Log.v("USER_SERVICE_UPDATE_ERROR", response.errorBody()!!.string())
                            statusMessage.value = StatusCode.ERROR
                        }
                    }
                    override fun onFailure(call: Call<User>, t: Throwable) {
                        Log.v("USER_SERVICE_UPDATE_FAILURE",t.toString())
                        statusMessage.value = StatusCode.FAILURE
                    }
                })
                //withContext(Dispatchers.Main) {}
            }catch (e:Exception){
                withContext(Dispatchers.Main) {
                    Log.v("USER_SERVICE_UPDATE_EXCEPTION ",e.toString())
                    statusMessage.value = StatusCode.EXCEPTION
                }
            }
        }
    }

    fun delete(){
        viewModelScope.launch {
            try{
                val call = ApiClient.userService.delete()
                call.enqueue(object : Callback<User> {
                    override fun onResponse(call: Call<User>, response: Response<User>) {
                        if (response.isSuccessful) {
                            Log.v("USER_SERVICE_DELETE_SUCCESSFULL","ACCOUNT DELETED!")
                            logout()
                        } else {
                            Log.v("USER_SERVICE_DELETE_ERROR", response.errorBody()!!.string())
                            statusMessage.value = StatusCode.ERROR
                        }
                    }
                    override fun onFailure(call: Call<User>, t: Throwable) {
                        Log.v("USER_SERVICE_FIND_ONE_FAILURE",t.toString())
                        statusMessage.value = StatusCode.FAILURE
                    }
                })
                //withContext(Dispatchers.Main) {}
            }catch (e:Exception){
                withContext(Dispatchers.Main) {
                    Log.v("USER_SERVICE_FIND_ONE_EXCEPTION ",e.toString())
                    statusMessage.value = StatusCode.EXCEPTION
                }
            }
        }
    }

    fun logout(){
        JwtService.removeJwt(MainActivity.instance)
        val editor = sharedPreferences.edit()
        val removed: Boolean = editor.remove(Constants.KEEP_ACCESS).commit()
    }

    val user: LiveData<User> = _user
}


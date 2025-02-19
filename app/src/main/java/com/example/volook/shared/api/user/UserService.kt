package com.example.volook.shared.api.user

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserService {
    @POST(UserApiURL.LOGIN_URL)
    fun login(@Body data: LoginDto): Call<JwtDto>

    @POST(UserApiURL.SIGN_UP_URL)
    fun signUp(@Body data: LoginDto):Call<JwtDto>

    @PUT(UserApiURL.UPDATE_URL)
    fun update(@Body user: User):Call<User>

    @GET(UserApiURL.FIND_ONE_URL)
    fun findOne():Call<User>

    @DELETE(UserApiURL.DELETE_URL)
    fun delete():Call<User>
}

data class LoginDto(val emailAddress:String, val password:String)
data class User(val name:String?, val surname: String?, var birthDate: Any?, val email: String, val saltedPassword: String?, val customerCode: String?, val residualPoints: Number?, val role: Role?)
data class JwtDto(val jwt:String)
enum class Role{
    ADMIN,
    CUSTOMER,
    LOYALTY_CUSTOMER
}
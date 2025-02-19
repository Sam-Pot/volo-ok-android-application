package com.example.volook.shared.api.user

class UserApiURL {
    companion object{
        private const val USER_CONTROLLER = "/user"

        const val LOGIN_URL: String = "$USER_CONTROLLER/login"
        const val SIGN_UP_URL:String = "$USER_CONTROLLER/signin"
        const val FIND_ONE_URL:String = USER_CONTROLLER
        const val  DELETE_URL:String = USER_CONTROLLER
        const val UPDATE_URL:String = USER_CONTROLLER
    }
}
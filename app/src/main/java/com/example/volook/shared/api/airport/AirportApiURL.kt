package com.example.volook.shared.api.airport

class AirportApiURL {
    companion object{
        private const val AIRPORT_CONTROLLER = "/airports"

        const val FIND_ALL:String = "$AIRPORT_CONTROLLER"
        const val FIND_ONE:String = "$AIRPORT_CONTROLLER/{id}"

    }
}
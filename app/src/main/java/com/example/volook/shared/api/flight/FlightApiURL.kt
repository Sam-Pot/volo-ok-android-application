package com.example.volook.shared.api.flight

class FlightApiURL {
    companion object{
        private const val FLIGHTS_CONTROLLER = "/flights"

        const val GENERATE_FLIGHTS_URL:String = "$FLIGHTS_CONTROLLER/generateFlights"
        const val FIND_ONE_URL:String = "$FLIGHTS_CONTROLLER/{id}"
    }
}
package com.example.volook.shared.api.booking

class BookingApiURL {
    companion object{
        private const val BOOKING_CONTROLLER = "/bookings"

        const val FIND_ONE_URL:String = "$BOOKING_CONTROLLER/{id}"
        const val FIND_URL: String = BOOKING_CONTROLLER
        const val UPDATE_URL: String = BOOKING_CONTROLLER
        const val SAVE_URL: String = BOOKING_CONTROLLER
        const val DELETE_URL: String = "$BOOKING_CONTROLLER/{id}"
    }
}
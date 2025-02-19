package com.example.volook.shared.api.booking

import androidx.lifecycle.MutableLiveData
import com.example.volook.shared.api.flight.FlightService
import com.example.volook.shared.api.ticket.TicketService
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.io.Serializable

interface BookingService {

    @GET(BookingApiURL.FIND_ONE_URL)
    fun findOne(@Path("id") id:String): Call<Booking>

    @GET(BookingApiURL.FIND_URL)
    fun find(): Call<PaginatedBookings>

    @POST(BookingApiURL.SAVE_URL)
    fun save(@Body data: Booking): Call<Booking>

    @PUT(BookingApiURL.UPDATE_URL)
    fun update(@Body data: Booking): Call<Booking>

    @DELETE(BookingApiURL.DELETE_URL)
    fun delete(@Path("id") id:String): Call<Booking>

    data class Booking(val id:String?, val state: BookingState, val expirationDate: Long?,
                       val userId: String?, val tickets: List<TicketService.Ticket>, var flightName: MutableLiveData<FlightService.Flight>?):Serializable
    data class PaginatedBookings(val elementsNumber: Int, val bookings: List<Booking>)
    enum class BookingState { EMPTY, OPEN, CONFIRMED, EXPIRED }
}
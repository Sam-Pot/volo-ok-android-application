package com.example.volook.shared.api.flight

import com.example.volook.shared.api.airport.AirportService
import com.example.volook.shared.api.fare.FareService
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.io.Serializable
import java.util.Date

interface FlightService {

    @GET(FlightApiURL.FIND_ONE_URL)
    fun findOne(@Path("id") id: String): Call<Flight>


    @GET(FlightApiURL.GENERATE_FLIGHTS_URL)
    fun generateFlights(@Query("from") from: String,
                        @Query("to") to: String,
                        @Query("departureDate") departureDate: Long,
                        @Query("fare") fare: String): Call<List<Flight>>

    data class Promotion(val id:String, val name:String, val discountPercentage:Float,
                            val endDate:Long,val startDate: Long, val onlyForLoyalCustomer: Boolean):Serializable

    data class Flight(val id:String, val name:String, val departureDateTime: Long,
                               val distance: Float, val departure: AirportService.Airport,
                               val destination: AirportService.Airport, val promotion:Promotion, val fares: List<FareService.Fare> ): Serializable
    data class AvailableFlight(val name:String, val departureDateTime: Date)

}
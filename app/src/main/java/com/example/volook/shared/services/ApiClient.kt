package com.example.volook.shared.services
import com.example.volook.shared.api.airport.AirportService
import com.example.volook.shared.api.booking.BookingService
import com.example.volook.shared.api.fare.FareService
import com.example.volook.shared.api.flight.FlightService
import com.example.volook.shared.api.ticket.TicketService
import com.example.volook.shared.configs.Constants
import com.example.volook.shared.api.user.UserService
import com.example.volook.shared.interceptors.JwtInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val jwtInterceptor = JwtInterceptor()
    private val client = OkHttpClient().newBuilder().apply {
        addInterceptor(jwtInterceptor)
    }.build()
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.API_GATEWAY_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }
}

object ApiClient {
    val userService: UserService by lazy {
        RetrofitClient.retrofit.create(UserService::class.java)
    }
    val flightService: FlightService by lazy {
        RetrofitClient.retrofit.create(FlightService::class.java)
    }
    val airportService: AirportService by lazy {
        RetrofitClient.retrofit.create(AirportService::class.java)
    }
    val fareService: FareService by lazy {
        RetrofitClient.retrofit.create(FareService::class.java)
    }
    val bookingService: BookingService by lazy {
        RetrofitClient.retrofit.create(BookingService::class.java)
    }
    val ticketService: TicketService by lazy {
        RetrofitClient.retrofit.create(TicketService::class.java)
    }
}
package com.example.volook.shared.api.airport

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.Serializable

interface AirportService {
    @GET(AirportApiURL.FIND_ALL)
    fun findAll(): Call<AirportDto>

    @GET(AirportApiURL.FIND_ONE)
    fun findOne(@Path("id") id: String): Call<Airport>

    data class AirportDto(val airports: List<Airport>)
    data class Airport(val id: String, val name:String, val iata: String,
                          val latitude: Double, val longitude: Double): Serializable
}
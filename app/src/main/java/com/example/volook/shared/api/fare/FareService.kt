package com.example.volook.shared.api.fare

import retrofit2.Call
import retrofit2.http.GET
import java.io.Serializable

interface FareService {
    @GET(FareApiURL.FIND_ALL)
    fun findAll(): Call<FareDto>

    data class FareDto(val elementsNumber: Int, val fares: List<Fare>)
    data class Fare(val editable:Boolean, val id: String, val modificationPrice: Float, val name: String, val price: Float): Serializable
}
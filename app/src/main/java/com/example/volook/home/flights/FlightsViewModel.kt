package com.example.volook.home.flights

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.volook.MainActivity
import com.example.volook.shared.api.airport.AirportService
import com.example.volook.shared.api.fare.FareService
import com.example.volook.shared.api.flight.FlightService
import com.example.volook.shared.configs.StatusCode
import com.example.volook.shared.helpers.Helpers
import com.example.volook.shared.helpers.LocationManager
import com.example.volook.shared.services.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FlightsViewModel : ViewModel() {
    private var _flights = MutableLiveData<List<FlightService.Flight>>()
    private var _airports = MutableLiveData<List<AirportService.Airport>>()
    private var _fares = MutableLiveData<List<FareService.Fare>>()
    //LOCATION
    private var locationManager = LocationManager(MainActivity.instance)
    private val _location =  MutableLiveData<LocationManager.DeviceLocation>()
    private var located = false
    //LIVE DATA
    val flights: LiveData<List<FlightService.Flight>> = _flights
    val airports: LiveData<List<AirportService.Airport>> = _airports
    val fares: LiveData<List<FareService.Fare>> = _fares
    val defaultAirport = MutableLiveData<AirportService.Airport>()
    var statusMessage = MutableLiveData<StatusCode>()
    //FILTERS
    lateinit var departureFilter: String
    lateinit var destinationFilter: String
    lateinit var fareFilter: String
    var departureDateFilter: Long = 0

    init{
        searchAirports()
        searchFares()
    }

    fun searchFlights(){
        if(departureFilter.isNullOrBlank() || destinationFilter.isNullOrBlank() ||
            fareFilter.isNullOrBlank() || departureDateFilter.toInt() == 0){
            statusMessage.value = StatusCode.EXCEPTION
        }else{
            val departureAirport = airports.value?.filter{ it.name == departureFilter }?.getOrNull(0)
            val destinationAirport = airports.value?.filter{ it.name == destinationFilter }?.getOrNull(0)
            val fare = fares.value?.filter { it.name == fareFilter }?.getOrNull(0)
            if(departureAirport!=null && destinationAirport!=null && fare != null){
                viewModelScope.launch {
                    try{
                        val call = ApiClient.flightService.generateFlights(departureAirport.id, destinationAirport.id, departureDateFilter, fare.id)
                        call.enqueue(object : Callback<List<FlightService.Flight>> {
                            override fun onResponse(
                                call: Call<List<FlightService.Flight>>,
                                response: Response<List<FlightService.Flight>>
                            ) {
                                if(response.isSuccessful){
                                    val data = response.body() as List<FlightService.Flight>
                                    _flights.value = data
                                    Log.v("FLIGHTS_GENERATE_FLIGHTS",data.toString())
                                    statusMessage.value = StatusCode.SUCCESS
                                }else{
                                    Log.v("FLIGHTS_GENERATE_FLIGHTS_ERROR", response.errorBody()!!.string())
                                    statusMessage.value = StatusCode.ERROR
                                }
                            }

                            override fun onFailure(
                                call: Call<List<FlightService.Flight>>,
                                t: Throwable
                            ) {
                                Log.v("FLIGHTS_GENERATE_FLIGHTS_FAILURE",t.toString())
                                statusMessage.value = StatusCode.FAILURE
                            }
                        })
                    }catch (e:Exception){
                        withContext(Dispatchers.Main) {
                            Log.v("FLIGHTS_GENERATE_FLIGHTS_EXCEPTION ",e.toString())
                            statusMessage.value = StatusCode.EXCEPTION
                        }
                    }
                }
            }
        }
    }

    private fun searchAirports(){
        viewModelScope.launch {
            try{
                val call = ApiClient.airportService.findAll()
                call.enqueue(object : Callback<AirportService.AirportDto> {
                    override fun onResponse(
                        call: Call<AirportService.AirportDto>,
                        response: Response<AirportService.AirportDto>
                    ) {
                        if(response.isSuccessful){
                            val data = response.body()?.airports as List<AirportService.Airport>
                            Log.v("AIRPORTS", data.toString())
                            _airports.value = data
                        }else{
                            Log.v("AIRPORT_FIND_ALL_ERROR", response.errorBody()!!.string())
                            statusMessage.value = StatusCode.ERROR
                        }
                    }

                    override fun onFailure(
                        call: Call<AirportService.AirportDto>,
                        t: Throwable
                    ) {
                        Log.v("AIRPORT_FIND_ALL_FAILURE",t.toString())
                        statusMessage.value = StatusCode.FAILURE
                    }
                })
            }catch (e:Exception){
                withContext(Dispatchers.Main) {
                    Log.v("AIRPORT_FIND_ALL_EXCEPTION ",e.toString())
                    statusMessage.value = StatusCode.EXCEPTION
                }
            }
        }
    }

    private fun searchFares(){
        viewModelScope.launch {
            try{
                val call = ApiClient.fareService.findAll()
                call.enqueue(object : Callback<FareService.FareDto> {
                    override fun onResponse(
                        call: Call<FareService.FareDto>,
                        response: Response<FareService.FareDto>
                    ) {
                        if(response.isSuccessful){
                            val data = response.body()?.fares as List<FareService.Fare>
                            Log.v("FARES", data.toString())
                            _fares.value = data
                        }else{
                            Log.v("FARE_FIND_ALL_ERROR", response.errorBody()!!.string())
                            statusMessage.value = StatusCode.ERROR
                        }
                    }

                    override fun onFailure(
                        call: Call<FareService.FareDto>,
                        t: Throwable
                    ) {
                        Log.v("FARE_FIND_ALL_FAILURE",t.toString())
                        statusMessage.value = StatusCode.FAILURE
                    }
                })
            }catch (e:Exception){
                withContext(Dispatchers.Main) {
                    Log.v("FARE_FIND_ALL_EXCEPTION ",e.toString())
                    statusMessage.value = StatusCode.EXCEPTION
                }
            }
        }
    }

    fun selectNearestAirport(){
        if(!located){
            locationManager.getLocation(_location)
            _location.observe(MainActivity.instance,  Observer {
                val latitude = it.latitude
                val longitude = it.longitude
                var nearestDistance: Double = Double.POSITIVE_INFINITY
                var nearestAirport: AirportService.Airport? = null
                airports.observe(MainActivity.instance, Observer { airports ->
                    if(airports.isNotEmpty()){
                        for(airport in airports){
                            val distance = Helpers.euclideanDistance(airport.latitude, latitude, airport.longitude, longitude)
                            if(distance<nearestDistance){
                                nearestDistance = distance
                                nearestAirport = airport
                            }
                        }
                        defaultAirport.value = nearestAirport!!
                    }
                })
            })
            located = true
        }
    }

}
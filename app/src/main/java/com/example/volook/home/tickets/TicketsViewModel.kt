package com.example.volook.home.tickets

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.volook.MainActivity
import com.example.volook.shared.api.flight.FlightService
import com.example.volook.shared.api.ticket.TicketService
import com.example.volook.shared.api.ticket.TicketService.PaginatedTickets
import com.example.volook.shared.configs.StatusCode
import com.example.volook.shared.services.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TicketsViewModel : ViewModel() {

    private var _tickets = MutableLiveData<List<TicketService.Ticket>>()
    private var _bookings = MutableLiveData<List<TicketService.Ticket>>()
    private var counter = 0
    var statusMessage = MutableLiveData<StatusCode>()

    fun searchTickets(){
        viewModelScope.launch(Dispatchers.Main){
            try{
                val call = ApiClient.ticketService.find()
                call.enqueue(object : Callback<TicketService.PaginatedTickets> {
                    override fun onResponse(
                        call: Call<PaginatedTickets>,
                        response: Response<PaginatedTickets>
                    ) {
                        if(response.isSuccessful){
                            if(!response.body()?.tickets.isNullOrEmpty()){
                                val data = response.body()?.tickets as List<TicketService.Ticket>
                                //GETTING FLIGHT NAME
                                for(t in data){
                                    if(t.flightId.isNotBlank()){
                                        val flightId = t.flightId
                                        var flightContainer = MutableLiveData<FlightService.Flight>()
                                        findFlight(flightId,flightContainer)
                                        flightContainer.observe(MainActivity.instance, Observer {
                                            t.flightName = it.name
                                            counter++
                                            if(counter>=data.size){
                                                _tickets.value = data.filter{it.state == TicketService.TicketState.PURCHASED}
                                                _bookings.value = data.filter { it.state == TicketService.TicketState.BOOKED }
                                                Log.v("FOUND TICKETS", _tickets.value.toString())
                                                Log.v("FOUND BOOKINGS", _bookings.value.toString())
                                                statusMessage.value = StatusCode.SUCCESS
                                                counter = 0
                                            }
                                        })
                                    }
                                }
                            }
                        }else{
                            Log.v("TICKETS_FIND_ERROR", response.errorBody()!!.string())
                            statusMessage.value = StatusCode.ERROR
                        }
                    }

                    override fun onFailure(
                        call: Call<PaginatedTickets>,
                        t: Throwable
                    ) {
                        Log.v("TICKETS_FIND_FAILURE",t.toString())
                        statusMessage.value = StatusCode.FAILURE
                    }
                })
            }catch (e:Exception){
                withContext(Dispatchers.Main) {
                    Log.v("TICKETS_FIND_EXCEPTION ",e.toString())
                    statusMessage.value = StatusCode.EXCEPTION
                }
            }
        }
    }

    fun findFlight(flightId: String, flight: MutableLiveData<FlightService.Flight>){
        viewModelScope.launch {
            try{
                val call = ApiClient.flightService.findOne(flightId)
                call.enqueue(object : Callback<FlightService.Flight> {
                    override fun onResponse(
                        call: Call<FlightService.Flight>,
                        response: Response<FlightService.Flight>
                    ) {
                        if(response.isSuccessful){
                            val data = response.body() as FlightService.Flight
                            Log.v("FLIGHT", data.toString())
                            //GETTING FLIGHT NAME
                            flight.value = data
                        }else{
                            Log.v("FIND_FLIGHT_ERROR", response.errorBody()!!.string())
                            statusMessage.value = StatusCode.ERROR
                        }
                    }

                    override fun onFailure(
                        call: Call<FlightService.Flight>,
                        t: Throwable
                    ) {
                        Log.v("FIND_FLIGHT_FAILURE",t.toString())
                        statusMessage.value = StatusCode.FAILURE
                    }
                })
            }catch (e:Exception){
                withContext(Dispatchers.Main) {
                    Log.v("FIND_FLIGHT_EXCEPTION ",e.toString())
                    statusMessage.value = StatusCode.EXCEPTION
                }
            }
        }
    }

    fun clearData(){
        _tickets.value = mutableListOf()
        _bookings.value = mutableListOf()
    }

    val tickets: LiveData<List<TicketService.Ticket>> = _tickets
    val bookings: LiveData<List<TicketService.Ticket>> = _bookings
}
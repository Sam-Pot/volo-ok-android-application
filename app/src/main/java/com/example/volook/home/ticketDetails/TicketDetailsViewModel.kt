package com.example.volook.home.ticketDetails

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.volook.shared.api.airport.AirportService
import com.example.volook.shared.api.flight.FlightService
import com.example.volook.shared.api.ticket.TicketService
import com.example.volook.shared.configs.StatusCode
import com.example.volook.shared.services.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TicketDetailsViewModel: ViewModel() {

    private var _ticket = MutableLiveData<TicketService.Ticket>()
    private var _statusMessage = MutableLiveData<StatusCode>()
    private var _departureAirport = MutableLiveData<AirportService.Airport>()
    private var _destinationAirport = MutableLiveData<AirportService.Airport>()

    val departureAirport: LiveData<AirportService.Airport> = _departureAirport
    val destinationAirport: LiveData<AirportService.Airport> = _destinationAirport
    val ticket: LiveData<TicketService.Ticket> = _ticket
    val statusMessage: LiveData<StatusCode> = _statusMessage

    fun findAirportById(id: String, isDepartureAirport: Boolean){
        viewModelScope.launch {
            try{
                val call = ApiClient.airportService.findOne(id)
                call.enqueue(object : Callback<AirportService.Airport> {
                    override fun onResponse(
                        call: Call<AirportService.Airport>,
                        response: Response<AirportService.Airport>
                    ) {
                        if(response.isSuccessful){
                            val data = response.body() as AirportService.Airport
                            Log.v("AIRPORT", data.toString())
                            if(isDepartureAirport){
                                _departureAirport.value = data
                            }else{
                                _destinationAirport.value = data
                            }
                        }else{
                            Log.v("FIND_AIRPORT_BY_ID_ERROR", response.errorBody()!!.string())
                        }
                    }

                    override fun onFailure(
                        call: Call<AirportService.Airport>,
                        t: Throwable
                    ) {
                        Log.v("FIND_AIRPORT_BY_ID_FAILURE",t.toString())
                        _statusMessage.value = StatusCode.FAILURE
                    }
                })
            }catch (e:Exception){
                withContext(Dispatchers.Main) {
                    Log.v("FIND_AIRPORT_BY_ID_EXCEPTION ",e.toString())
                    _statusMessage.value = StatusCode.EXCEPTION
                }
            }
        }
    }

    fun buyTicket(ticketToBuy: TicketService.Ticket){
        val billingInformationDto = TicketService.BillingInformationsDto(ticketToBuy.passengerName,ticketToBuy.passengerSurname,
            1,2030,123,"fakePan",ticketToBuy.price)
        val ticketDto: TicketService.BuyTicketDto = TicketService.BuyTicketDto(ticketToBuy,billingInformationDto)
        viewModelScope.launch {
            try{
                val call = ApiClient.ticketService.buy(ticketDto)
                call.enqueue(object : Callback<TicketService.BooleanDto> {
                    override fun onResponse(
                        call: Call<TicketService.BooleanDto>,
                        response: Response<TicketService.BooleanDto>
                    ) {
                        if(response.isSuccessful){
                            val data = response.body() as TicketService.BooleanDto
                            Log.v("TICKET_PURCHASE_STATUS", data.toString())
                            if(data.ok) {
                                val purchasedTicket = ticket.value?.copy()
                                if(purchasedTicket!=null){
                                    purchasedTicket.state = TicketService.TicketState.PURCHASED
                                    _ticket.value = purchasedTicket!!
                                    _statusMessage.value = StatusCode.SUCCESS
                                }
                                Log.v("STATO", ticket.value?.state.toString())
                            }
                        }else{
                            Log.v("TICKET_PURCHASE_ERROR", response.errorBody()!!.string())
                            _statusMessage.value = StatusCode.ERROR
                        }
                    }

                    override fun onFailure(
                        call: Call<TicketService.BooleanDto>,
                        t: Throwable
                    ) {
                        Log.v("TICKET_PURCHASE_FAILURE",t.toString())
                        _statusMessage.value = StatusCode.FAILURE
                    }
                })
            }catch (e:Exception){
                withContext(Dispatchers.Main) {
                    Log.v("TICKET_PURCHASE_EXCEPTION ",e.toString())
                    _statusMessage.value = StatusCode.EXCEPTION
                }
            }
        }
    }

    fun setTicket(ticket: TicketService.Ticket){
        _ticket.value = ticket
    }
}
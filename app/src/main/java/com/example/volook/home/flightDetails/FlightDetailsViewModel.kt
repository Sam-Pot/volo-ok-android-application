package com.example.volook.home.flightDetails

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.volook.shared.api.booking.BookingService
import com.example.volook.shared.api.ticket.TicketService
import com.example.volook.shared.api.user.User
import com.example.volook.shared.configs.StatusCode
import com.example.volook.shared.services.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FlightDetailsViewModel: ViewModel() {

    var statusMessage = MutableLiveData<StatusCode>()

    fun buyTicket(ticketToBuy: TicketService.Ticket){
        val billingInformationDto = TicketService.BillingInformationsDto(ticketToBuy.passengerName,ticketToBuy.passengerSurname,
            1,2030,123,"fakePan",ticketToBuy.price)
        ticketToBuy.state = TicketService.TicketState.PURCHASED
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
                            statusMessage.value = StatusCode.SUCCESS
                        }else{
                            Log.v("TICKET_PURCHASE_ERROR", response.errorBody()!!.string())
                            statusMessage.value = StatusCode.ERROR
                        }
                    }

                    override fun onFailure(
                        call: Call<TicketService.BooleanDto>,
                        t: Throwable
                    ) {
                        Log.v("TICKET_PURCHASE_FAILURE",t.toString())
                        statusMessage.value = StatusCode.FAILURE
                    }
                })
            }catch (e:Exception){
                withContext(Dispatchers.Main) {
                    Log.v("TICKET_PURCHASE_EXCEPTION ",e.toString())
                    statusMessage.value = StatusCode.EXCEPTION
                }
            }
        }
    }

    fun saveBooking(booking: BookingService.Booking){
        viewModelScope.launch {
            try{
                val call = ApiClient.bookingService.save(booking)
                call.enqueue(object : Callback<BookingService.Booking> {
                    override fun onResponse(
                        call: Call<BookingService.Booking>,
                        response: Response<BookingService.Booking>
                    ) {
                        if(response.isSuccessful){
                            val data = response.body() as BookingService.Booking
                            Log.v("BOOKING_SAVE", data.toString())
                            statusMessage.value = StatusCode.SUCCESS
                        }else{
                            Log.v("BOOKING_SAVE_ERROR", response.errorBody()!!.string())
                            statusMessage.value = StatusCode.ERROR
                        }
                    }

                    override fun onFailure(
                        call: Call<BookingService.Booking>,
                        t: Throwable
                    ) {
                        Log.v("BOOKING_SAVE_FAILURE",t.toString())
                        statusMessage.value = StatusCode.FAILURE
                    }
                })
            }catch (e:Exception){
                withContext(Dispatchers.Main) {
                    Log.v("BOOKING_SAVE_EXCEPTION ",e.toString())
                    statusMessage.value = StatusCode.EXCEPTION
                }
            }
        }
    }

    fun getUser(user: MutableLiveData<User>){
        viewModelScope.launch {
            try{
                val call = ApiClient.userService.findOne()
                call.enqueue(object : Callback<User> {
                    override fun onResponse(
                        call: Call<User>,
                        response: Response<User>
                    ) {
                        if(response.isSuccessful){
                            val data = response.body() as User
                            Log.v("USER", data.toString())
                            user.value = data
                        }else{
                            Log.v("USER_ERROR", response.errorBody()!!.string())
                            statusMessage.value = StatusCode.ERROR
                        }
                    }

                    override fun onFailure(
                        call: Call<User>,
                        t: Throwable
                    ) {
                        Log.v("USER_FAILURE",t.toString())
                        statusMessage.value = StatusCode.FAILURE
                    }
                })
            }catch (e:Exception){
                withContext(Dispatchers.Main) {
                    Log.v("USER_EXCEPTION ",e.toString())
                    statusMessage.value = StatusCode.EXCEPTION
                }
            }
        }
    }
}
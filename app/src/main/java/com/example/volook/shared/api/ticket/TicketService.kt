package com.example.volook.shared.api.ticket

import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import com.example.volook.shared.api.flight.FlightService
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.io.Serializable


interface TicketService {

    @GET(TicketApiURL.FIND_ONE_URL)
    fun findOne(@Path("id") id:String): Call<Ticket>

    @GET(TicketApiURL.FIND_URL)
    fun find(): Call<PaginatedTickets>

    @PUT(TicketApiURL.UPDATE_URL)
    fun update(@Body data: BuyTicketDto): Call<Ticket>

    @POST(TicketApiURL.BUY_URL)
    fun buy(@Body data: BuyTicketDto): Call<BooleanDto>

    @PUT(TicketApiURL.RESEND_TICKET)
    fun resendTicket(@Path("id") id:String): Call<Boolean>

    data class Ticket(val id: String?, val passengerName: String, val passengerSurname:String, val fareId: String, val customerCode: String?,
                      val price: Float, val generatedPoints: Int, val usedPoints: Int, val flightId: String, var state: TicketState,
                      val flightDate: Long, val bookingId: String?, val userId: String, val from: String, val to: String,
                      var flightName: String?): Serializable
    enum class TicketState{ VOID, BOOKED, PURCHASED }
    data class PaginatedTickets(val elementsNumber: Int, val tickets: List<Ticket>)

    data class BuyTicketDto(
        val ticket: Ticket,
        val billingInformation: BillingInformationsDto
    )

    data class BillingInformationsDto(val cardHolderName: String, val cardHolderSurname: String, val expiryMonth: Int,
                                      val expiryYear: Int, val cvv: Int, val PAN: String, val cost: Float)
    data class BooleanDto(val ok: Boolean)
}
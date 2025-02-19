package com.example.volook.home.flightDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.volook.MainActivity
import com.example.volook.R
import com.example.volook.databinding.FragmentFlightDetailsBinding
import com.example.volook.shared.api.booking.BookingService
import com.example.volook.shared.api.flight.FlightService
import com.example.volook.shared.api.ticket.TicketService
import com.example.volook.shared.api.user.User
import com.example.volook.shared.configs.StatusCode
import com.example.volook.shared.helpers.Helpers
import com.example.volook.shared.services.JwtService
import com.google.android.material.snackbar.Snackbar

class FlightDetailsFragment : Fragment() {
    private var _binding: FragmentFlightDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val flightDetailsViewModel =
            ViewModelProvider(this)[FlightDetailsViewModel::class.java]
        _binding = FragmentFlightDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        MainActivity.instance.scanFloatingButton.hide()
        //GETTING FIELDS
        val flightName: TextView = binding.bookingDetailsFlightName
        val departure: TextView = binding.bookingDetailsDeparture
        val destination: TextView = binding.bookingDetailsDestination
        val flightDate: TextView = binding.bookingDetailsFlightDate
        val passengerName: EditText = binding.bookingDetailsPassengerName
        val passengerSurname: EditText = binding.bookingDetailsPassengerSurname
        val price: TextView = binding.bookingDetailsPrice
        val errorMessage: TextView = binding.errorMessage
        val buyButton: Button = binding.bookingDetailsBuyButton
        val bookNowButton: Button = binding.bookingDetailsBookNowButton
        //GETTING DATA
        val flight: FlightService.Flight =  arguments?.getSerializable("flight") as FlightService.Flight
        val userId: String = JwtService.getId(MainActivity.instance)
        flightName.text = flight.name
        departure.text = flight.departure.name
        destination.text = flight.destination.name
        flightDate.text = Helpers.formatDate(Helpers.millisToLocaleDateTime(flight.departureDateTime))
        val priceValue: Float = (flight.distance * flight.fares[0].price)
        price.text = priceValue.toString()
        //ACTIONS
        buyButton.setOnClickListener{
            if(passengerName.text.isNullOrBlank() || passengerSurname.text.isNullOrBlank()){
                Helpers.displayMessage(errorMessage,R.string.fill_in_all_field_msg)
            }else{
                val user = MutableLiveData<User>()
                flightDetailsViewModel.getUser(user)
                user.observe(viewLifecycleOwner){
                    val ticket: TicketService.Ticket = TicketService.Ticket(
                        null,
                        passengerName.text.toString(),
                        passengerSurname.text.toString(),
                        flight.fares[0].id,
                        it.customerCode,
                        priceValue,
                        flight.distance.toInt(),
                        0,
                        flight.id,
                        TicketService.TicketState.PURCHASED,
                        flight.departureDateTime,
                        null,
                        userId,
                        flight.departure.id,
                        flight.destination.id,
                        null)
                    flightDetailsViewModel.buyTicket(ticket)
                }
            }
        }

        bookNowButton.setOnClickListener{
            if(passengerName.text.isNullOrBlank() || passengerSurname.text.isNullOrBlank()){
                Helpers.displayMessage(errorMessage,R.string.fill_in_all_field_msg)
            }else{
                val user = MutableLiveData<User>()
                flightDetailsViewModel.getUser(user)
                user.observe(viewLifecycleOwner){
                    val ticket: TicketService.Ticket = TicketService.Ticket(
                        null,
                        passengerName.text.toString(),
                        passengerSurname.text.toString(),
                        flight.fares[0].id,
                        it.customerCode,
                        priceValue,
                        flight.distance.toInt(),
                        0,
                        flight.id,
                        TicketService.TicketState.PURCHASED,
                        flight.departureDateTime,
                        null,
                        userId,
                        flight.departure.id,
                        flight.destination.id,
                        null)
                    val booking = BookingService.Booking(null, BookingService.BookingState.OPEN, null,
                        userId, listOf(ticket), null)
                    flightDetailsViewModel.saveBooking(booking)
                }
            }
        }
        flightDetailsViewModel.statusMessage.observe(viewLifecycleOwner){
            if(it == StatusCode.SUCCESS){
                Snackbar.make(root, R.string.operation_performed, Snackbar.LENGTH_SHORT).show()
                findNavController().navigateUp()
                findNavController().navigateUp()
            }else{
                Snackbar.make(root, R.string.an_error_occurred, Snackbar.LENGTH_SHORT).show()
            }
        }
        return root
    }

    override fun onStart() {
        super.onStart()
        MainActivity.instance.scanFloatingButton.hide()
    }

    override fun onPause() {
        super.onPause()
        MainActivity.instance.scanFloatingButton.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
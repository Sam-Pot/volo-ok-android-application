package com.example.volook.home.ticketDetails

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.volook.MainActivity
import com.example.volook.R
import com.example.volook.databinding.FragmentTicketDetailsBinding
import com.example.volook.shared.api.airport.AirportService
import com.example.volook.shared.api.ticket.TicketService
import com.example.volook.shared.configs.StatusCode
import com.example.volook.shared.helpers.Helpers
import com.google.android.material.snackbar.Snackbar

class TicketDetailsFragment : Fragment() {

    private var _binding: FragmentTicketDetailsBinding? = null
    private val binding get() = _binding!!

    private lateinit var qrCode: ImageView
    private lateinit var buyButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ticketDetailsViewModel =
            ViewModelProvider(this)[TicketDetailsViewModel::class.java]
        _binding = FragmentTicketDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        //GETTING FIELDS
        val flightName: TextView = binding.ticketDetailsFlightName
        val departure: TextView = binding.ticketDetailsDeparture
        val destination: TextView = binding.ticketDetailsDestination
        val flightDate: TextView = binding.ticketDetailsFlightDate
        val passenger: TextView = binding.ticketDetailsPassenger
        qrCode = binding.ticketDetailsQRCode
        buyButton = binding.ticketDetailsBuyButton
        //GETTING DATA
        val ticket: TicketService.Ticket =  arguments?.getSerializable("ticket") as TicketService.Ticket
        ticketDetailsViewModel.setTicket(ticket)
        //FIND AIRPORTS
        ticketDetailsViewModel.findAirportById(ticket.from, isDepartureAirport = true)
        ticketDetailsViewModel.findAirportById(ticket.to, isDepartureAirport = false)

        ticketDetailsViewModel.departureAirport.observe(viewLifecycleOwner){
            if(it!=null){
                departure.text = it.name
            }
        }
        ticketDetailsViewModel.destinationAirport.observe(viewLifecycleOwner){
            if(it!=null){
                destination.text = it.name
            }
        }

        ticketDetailsViewModel.ticket.observe(viewLifecycleOwner){
            flightDate.text = Helpers.formatDate(Helpers.millisToLocaleDateTime(it.flightDate))
            passenger.text = it.passengerName + " " + it.passengerSurname
            flightName.text = it.flightName
            if(it.state == TicketService.TicketState.PURCHASED && it.id!=null){
                displayQR(it.id)
            }else{
                buyButton.visibility = View.VISIBLE
            }
        }
        //ACTIONS
        buyButton.setOnClickListener{
            ticketDetailsViewModel.buyTicket(ticket)
            ticketDetailsViewModel.ticket.observe(viewLifecycleOwner){
                if(it.state == TicketService.TicketState.PURCHASED && it.id!=null){
                    displayQR(it.id)
                }else{
                    buyButton.visibility = View.VISIBLE
                }
            }
        }

        ticketDetailsViewModel.statusMessage.observe(viewLifecycleOwner){
            if(it == StatusCode.SUCCESS){
                Snackbar.make(root, R.string.available_flights_ticket_purchased, Snackbar.LENGTH_SHORT).show()
            }else{
                Snackbar.make(root, R.string.an_error_occurred, Snackbar.LENGTH_SHORT).show()
            }
        }
        return root
    }

    override fun onPause() {
        super.onPause()
        MainActivity.instance.scanFloatingButton.show()
    }

    override fun onStart() {
        super.onStart()
        MainActivity.instance.scanFloatingButton.hide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun displayQR(text: String){
        buyButton.visibility = View.GONE
        val bitmap = Helpers.generateQRCode(text)
        qrCode.setImageBitmap(bitmap)
        qrCode.visibility = View.VISIBLE
    }
}
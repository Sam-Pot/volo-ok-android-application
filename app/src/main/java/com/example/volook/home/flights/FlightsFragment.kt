package com.example.volook.home.flights

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.volook.MainActivity
import com.example.volook.R
import com.example.volook.databinding.FragmentFlightsBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.lifecycle.ViewModelProvider
import com.example.volook.shared.helpers.Helpers
import java.util.Date

class FlightsFragment : Fragment() {

    private var _binding: FragmentFlightsBinding? = null

    private val binding get() = _binding!!
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var selectedOnce = false;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val flightsViewModel = ViewModelProvider(requireActivity()).get(FlightsViewModel::class.java)
        _binding = FragmentFlightsBinding.inflate(inflater, container, false)

        //GETTING FIELDS
        val fromField: AutoCompleteTextView = binding.flightsFrom
        val toField: AutoCompleteTextView = binding.flightsTo
        val departureDateField: EditText = binding.flightsDepartureDate
        val fareField: AutoCompleteTextView = binding.flightsFare
        fareField.dropDownVerticalOffset = -300
        val searchFlightsButton: Button = binding.flightsButtonSearchFlights
        val errorMessage: TextView = binding.flightsErrorMessage
        //LOCATION MANAGEMENT
        flightsViewModel.selectNearestAirport()
        flightsViewModel.defaultAirport.observe(viewLifecycleOwner){
            if(it!=null && !selectedOnce){
                fromField.setText(it.name)
                selectedOnce = true
            }
        }
        //GETTING AIRPORTS
        flightsViewModel.airports.observe(viewLifecycleOwner){
            val airportNames = ArrayList<String>()
            val airports = it
            for(airport in airports){
                airportNames.add(airport.name)
            }
            val airportAdapter = ArrayAdapter<String>(MainActivity.instance, android.R.layout.simple_dropdown_item_1line, airportNames)
            fromField.setAdapter(airportAdapter)
            toField.setAdapter(airportAdapter)
        }
        //GETTING FARES
        flightsViewModel.fares.observe(viewLifecycleOwner){
            val fareNames = ArrayList<String>()
            val fares = it
            for(fare in fares){
                fareNames.add(fare.name)
            }
            val faresAdapter = ArrayAdapter<String>(MainActivity.instance, android.R.layout.simple_dropdown_item_1line, fareNames)
            fareField.setAdapter(faresAdapter)
        }

        departureDateField.setOnClickListener{
            val currentDate = Calendar.getInstance()
            val year = currentDate.get(Calendar.YEAR)
            val month = currentDate.get(Calendar.MONTH)
            val day = currentDate.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                MainActivity.instance,
                DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, monthOfYear, dayOfMonth)
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    departureDateField.setText(dateFormat.format(selectedDate.time))
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        //ACTIONS
        fareField.setOnClickListener{
            fareField.showDropDown()
        }

        searchFlightsButton.setOnClickListener{
            val departureAirport = fromField.text.toString()
            val destinationAirport = toField.text.toString()
            val departureDate = if(departureDateField.text.toString().isNotBlank()){dateFormat.parse(departureDateField.text.toString()).time}else{null}
            val fare = fareField.text.toString()

            if(departureAirport.isNullOrBlank() || destinationAirport.isNullOrBlank() || fare.isNullOrBlank()
                || departureDate==null || departureDate <= Date().time){
                if(departureDate!=null && departureDate <= Date().time){
                    Helpers.displayMessage(errorMessage,R.string.invalid_date_msg)
                }else{
                    Helpers.displayMessage(errorMessage,R.string.fill_in_all_field_msg)
                }
            }else{
                flightsViewModel.departureFilter = departureAirport
                flightsViewModel.destinationFilter = destinationAirport
                flightsViewModel.fareFilter = fare
                flightsViewModel.departureDateFilter = departureDate
                findNavController().navigate(R.id.action_flights_availableFlights)
            }
        }
        val root: View = binding.root
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
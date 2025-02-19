package com.example.volook.home.flights

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.volook.MainActivity
import com.example.volook.R
import com.example.volook.databinding.FragmentAvailableFlightsBinding
import com.example.volook.shared.adapters.CustomCardData
import com.example.volook.shared.adapters.CustomDataAdapter
import com.example.volook.shared.api.flight.FlightService
import com.example.volook.shared.configs.StatusCode
import com.example.volook.shared.helpers.Helpers
import com.google.android.material.snackbar.Snackbar

class AvailableFlightsFragment : Fragment() {
    private var _binding: FragmentAvailableFlightsBinding? = null
    private val binding get() = _binding!!
    private lateinit var flightsViewModel: FlightsViewModel
    private lateinit var listView: ListView
    private lateinit var defaultMessage: TextView

    private var availableFlights: List<FlightService.Flight> = mutableListOf()
    private var availableFlightsView: MutableList<CustomCardData<String,String>> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        flightsViewModel = ViewModelProvider(requireActivity()).get(FlightsViewModel::class.java)
        _binding = FragmentAvailableFlightsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        //GETTING FIELDS
        listView = binding.availableFlightsList
        defaultMessage = binding.availableFlightsDefaultMessage
        //LINK DATA TO XML
        val adapter: CustomDataAdapter<String,String> = CustomDataAdapter(
            MainActivity.instance,
            android.R.layout.simple_list_item_2,
            availableFlightsView
        )
        listView.adapter = adapter

        flightsViewModel.flights.observe(viewLifecycleOwner){
            availableFlights = it
            //IF NO FLIGHTS DISPLAY MESSAGE
            if(availableFlights.isEmpty()){
                defaultMessage.visibility = View.VISIBLE
                listView.visibility = View.GONE
            }else{
                defaultMessage.visibility = View.GONE
                listView.visibility = View.VISIBLE
            }
            for(flight in availableFlights){
                val flightsMap = HashMap<String,String>()
                val title = flight.name
                flightsMap["Orario"] = Helpers.formatDate(Helpers.millisToLocaleDateTime(flight.departureDateTime))
                availableFlightsView.add(CustomCardData(title,flightsMap))
            }
            (listView.adapter as CustomDataAdapter<String,String>).notifyDataSetChanged()
        }

        //ACTIONS
        listView.setOnItemClickListener { parent, view, position, id ->
            val selectedElement = listView.adapter.getItem(position)
            val selectedFlight = availableFlights[position]

            val bundle: Bundle = Bundle()
            bundle.putSerializable("flight", selectedFlight)
            findNavController().navigate(R.id.navigation_booking_detail,bundle)
            availableFlightsView.clear()
            Log.v("SELECTED", selectedFlight.toString())
            Log.v("SELECTED", selectedElement.toString())
        }
        flightsViewModel.statusMessage.observe(viewLifecycleOwner){
            if(it != StatusCode.SUCCESS){
                Snackbar.make(root, R.string.an_error_occurred, Snackbar.LENGTH_SHORT).show()
            }
        }
        return root
    }

    override fun onStart() {
        refreshData()
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
        availableFlightsView.clear()
        (availableFlights as MutableList).clear()
        (listView.adapter as CustomDataAdapter<String,String>).notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun refreshData(){
        availableFlightsView.clear()
        (availableFlights as MutableList).clear()
        flightsViewModel.searchFlights()
        (listView.adapter as CustomDataAdapter<String,String>).notifyDataSetChanged()
    }
}
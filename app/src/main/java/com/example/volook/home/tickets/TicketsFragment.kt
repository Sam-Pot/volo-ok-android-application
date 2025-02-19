package com.example.volook.home.tickets

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.volook.MainActivity
import com.example.volook.R
import com.example.volook.databinding.FragmentTicketsBinding
import com.example.volook.shared.adapters.CustomCardData
import com.example.volook.shared.adapters.CustomDataAdapter
import com.example.volook.shared.api.ticket.TicketService
import com.example.volook.shared.configs.StatusCode
import com.example.volook.shared.helpers.Helpers
import com.google.android.material.snackbar.Snackbar
import java.util.HashMap

class TicketsFragment : Fragment() {

    private var _binding: FragmentTicketsBinding? = null
    private val binding get() = _binding!!

    private lateinit var ticketsViewModel: TicketsViewModel
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var listView: ListView
    private lateinit var defaultMessage: TextView

    private var tickets: List<TicketService.Ticket> = mutableListOf()
    private var ticketsView: MutableList<CustomCardData<String, String>> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ticketsViewModel = ViewModelProvider(requireActivity()).get(TicketsViewModel::class.java)
        _binding = FragmentTicketsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        //GETTING FIELDS
        listView = binding.ticketList
        defaultMessage = binding.ticketsDefaultMessage
        swipeRefreshLayout = binding.swipeRefreshLayout
        //LINK DATA TO XML
        val adapter: CustomDataAdapter<String, String> = CustomDataAdapter(
            MainActivity.instance,
            android.R.layout.simple_list_item_2,
            ticketsView
        )
        listView.adapter = adapter
        if(tickets.isEmpty()){
            defaultMessage.visibility = View.VISIBLE
            listView.visibility = View.GONE
        }
        ticketsViewModel.statusMessage.observe(viewLifecycleOwner) {
            if (it == StatusCode.SUCCESS) {
                if (!ticketsViewModel.tickets.value.isNullOrEmpty()) {
                    tickets = ticketsViewModel.tickets.value!!
                    for (ticket in tickets) {
                        val ticketsMap = HashMap<String, String>()
                        val title = if (ticket.flightName != null) {
                            ticket.flightName
                        } else {
                            ""
                        }!!
                        ticketsMap["datetime"] =
                            Helpers.formatDate(Helpers.millisToLocaleDateTime(ticket.flightDate))
                        ticketsMap["state"] =
                            if (ticket.state.name == TicketService.TicketState.BOOKED.toString()) {
                                "PRENOTATO"
                            } else {
                                "ACQUISTATO"
                            }
                        ticketsView.add(CustomCardData(title, ticketsMap))
                    }
                    defaultMessage.visibility = View.GONE
                    listView.visibility = View.VISIBLE
                    (listView.adapter as CustomDataAdapter<String, String>).notifyDataSetChanged()
                } else {
                    defaultMessage.visibility = View.VISIBLE
                    listView.visibility = View.GONE
                }
            }
        }
        //ACTIONS
        listView.setOnItemClickListener { parent, view, position, id ->
            val selectedElement = listView.adapter.getItem(position)
            val selectedTicket = tickets[position]
            val bundle: Bundle = Bundle()
            bundle.putSerializable("ticket", selectedTicket)
            findNavController().navigate(R.id.navigation_ticket_detail,bundle)
            ticketsView.clear()
            Log.v("SELECTED", selectedTicket.toString())
            Log.v("SELECTED", selectedElement.toString())
        }

        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
        }

        ticketsViewModel.statusMessage.observe(viewLifecycleOwner){
            if(it != StatusCode.SUCCESS && it != StatusCode.NEUTRAL){
                Snackbar.make(root, R.string.an_error_occurred, Snackbar.LENGTH_SHORT).show()
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        refreshData()
    }

    private fun refreshData(){
        ticketsView.clear()
        ticketsViewModel.clearData()
        ticketsViewModel.searchTickets()
        (listView.adapter as CustomDataAdapter<String,String>).notifyDataSetChanged()
        swipeRefreshLayout.isRefreshing = false
    }
}
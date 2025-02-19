package com.example.volook.shared.api.ticket

class TicketApiURL {
    companion object{
        private const val TICKET_CONTROLLER = "/tickets"

        const val FIND_ONE_URL:String = "$TICKET_CONTROLLER/{id}"
        const val FIND_URL: String = TICKET_CONTROLLER
        const val UPDATE_URL: String = TICKET_CONTROLLER
        const val BUY_URL: String = "$TICKET_CONTROLLER/payments"
        const val RESEND_TICKET: String = "$TICKET_CONTROLLER/sendTicket/{id}"
    }
}
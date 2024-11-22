package com.example.mobile_service_app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.mobile_service_app.BookingWithUserAndService

class AdminBookingAdapter(
    context: Context,
    private val bookings: List<BookingWithUserAndService>
) : ArrayAdapter<BookingWithUserAndService>(context, 0, bookings) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_admin_booking, parent, false)

        val booking = bookings[position]

        view.findViewById<TextView>(R.id.tvBookingId).text = "Booking #${booking.booking.id}"
        view.findViewById<TextView>(R.id.tvUsername).text = "User: ${booking.user.username}"
        view.findViewById<TextView>(R.id.tvServiceName).text = "Service: ${booking.service.serviceName}"
        view.findViewById<TextView>(R.id.tvBookingDate).text = "Date: ${booking.booking.bookingDate}"
        view.findViewById<TextView>(R.id.tvStatus).text = "Status: ${booking.booking.status}"
        view.findViewById<TextView>(R.id.tvDescription).text = "Problem: ${booking.booking.problemDescription}" // Updated this line
        view.findViewById<TextView>(R.id.tvLocation).text = "Location: ${booking.booking.location}" // Add this line


        // Set background color based on status
        val statusColor = when(booking.booking.status) {
            "Pending" -> R.color.status_pending
            "Confirmed" -> R.color.status_confirmed
            "In Progress" -> R.color.status_in_progress
            "Completed" -> R.color.status_completed
            "Cancelled" -> R.color.status_cancelled
            else -> R.color.status_pending

        }
        view.findViewById<TextView>(R.id.tvStatus).setBackgroundResource(statusColor)

        return view
    }
}
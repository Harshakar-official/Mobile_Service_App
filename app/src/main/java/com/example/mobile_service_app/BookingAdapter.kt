package com.example.mobile_service_app.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.mobile_service_app.R
import com.example.mobile_service_app.AppDatabase
import com.example.mobile_service_app.Booking
import com.example.mobile_service_app.BookingWithUserAndService

class BookingAdapter(
    private val context: Context,
    private val bookings: List<BookingWithUserAndService>,
    private val db: AppDatabase
) : BaseAdapter() {

    override fun getCount(): Int = bookings.size

    override fun getItem(position: Int) = bookings[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.booking_list_item, parent, false)

        val bookingWithDetails = bookings[position]

        // Find all views
        val tvBookingId = view.findViewById<TextView>(R.id.tvBookingId)
        val tvUsername = view.findViewById<TextView>(R.id.tvUsername)
        val tvServiceName = view.findViewById<TextView>(R.id.tvServiceName)
        val tvBookingDate = view.findViewById<TextView>(R.id.tvBookingDate)
        val tvDescription = view.findViewById<TextView>(R.id.tvDescription)
        val tvStatus = view.findViewById<TextView>(R.id.tvStatus)

        // Set data safely with null checks
        tvBookingId?.text = "Booking #${bookingWithDetails.booking.id}"
        tvUsername?.text = "Customer: ${bookingWithDetails.user.username}"
        tvServiceName?.text = "Service: ${bookingWithDetails.service.serviceName}"
        tvBookingDate?.text = "Date: ${bookingWithDetails.booking.bookingDate}"
        tvDescription?.text = "Problem: ${bookingWithDetails.booking.problemDescription}"
        tvStatus?.text = "Status: ${bookingWithDetails.booking.status}"

        // Set status background color
        tvStatus?.let { statusView ->
            val statusColor = when(bookingWithDetails.booking.status.toLowerCase()) {
                "pending" -> R.color.status_pending
                "confirmed" -> R.color.status_confirmed
                "in progress" -> R.color.status_in_progress
                "completed" -> R.color.status_completed
                "cancelled" -> R.color.status_cancelled
                else -> R.color.status_pending
            }
            statusView.setBackgroundResource(statusColor)
        }

        return view
    }


}

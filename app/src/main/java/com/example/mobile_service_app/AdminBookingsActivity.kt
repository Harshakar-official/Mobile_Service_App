package com.example.mobile_service_app

import android.os.Bundle
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_service_app.AppDatabase
import com.example.mobile_service_app.BookingWithUserAndService

class AdminBookingsActivity : AppCompatActivity() {
    private lateinit var listViewBookings: ListView
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_bookings)

        // Initialize database
        db = AppDatabase.getDatabase(this)

        // Initialize views
        listViewBookings = findViewById(R.id.listViewAdminBookings)

        // Set up action bar
        supportActionBar?.apply {
            title = "All Bookings"
            setDisplayHomeAsUpEnabled(true)
        }

        // Load and display bookings
        loadBookings()

        // Set up list item click listener
        setupListClickListener()
    }

    private fun loadBookings() {
        try {
            val bookings = db.bookingDao().getAllBookingsWithUserAndService()
            val adapter = AdminBookingAdapter(this, bookings)
            listViewBookings.adapter = adapter
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading bookings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListClickListener() {
        listViewBookings.setOnItemClickListener { _, _, position, _ ->
            val booking = listViewBookings.adapter.getItem(position) as BookingWithUserAndService
            showStatusUpdateDialog(booking)
        }
    }

    private fun showStatusUpdateDialog(bookingWithUserAndService: BookingWithUserAndService) {
        val statusOptions = arrayOf("Pending", "Confirmed", "In Progress", "Completed", "Cancelled")
        var selectedStatus = bookingWithUserAndService.booking.status

        AlertDialog.Builder(this)
            .setTitle("Update Booking Status")
            .setSingleChoiceItems(statusOptions, statusOptions.indexOf(selectedStatus)) { _, which ->
                selectedStatus = statusOptions[which]
            }
            .setPositiveButton("Update") { _, _ ->
                try {
                    // Update booking status
                    val updatedBooking = bookingWithUserAndService.booking.copy(status = selectedStatus)
                    db.bookingDao().updateBooking(updatedBooking)
                    Toast.makeText(this, "Status updated successfully", Toast.LENGTH_SHORT).show()
                    loadBookings() // Refresh the list
                } catch (e: Exception) {
                    Toast.makeText(this, "Error updating status", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

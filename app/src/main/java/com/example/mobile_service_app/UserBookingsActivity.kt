package com.example.mobile_service_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_service_app.Booking
import com.example.mobile_service_app.adapters.BookingAdapter

class UserBookingsActivity : AppCompatActivity() {
    private lateinit var listViewBookings: ListView
    private lateinit var tvNoBookings: TextView
    private lateinit var db: AppDatabase
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_bookings)

        // Initialize views
        listViewBookings = findViewById(R.id.listViewBookings)
        tvNoBookings = findViewById(R.id.tvNoBookings)

        // Get userId from intent
        userId = intent.getIntExtra("USER_ID", -1)

        // Initialize database
        db = AppDatabase.getDatabase(this)

        // Set up action bar
        supportActionBar?.apply {
            title = "My Bookings"
            setDisplayHomeAsUpEnabled(true)
        }

        // Load bookings
        loadBookings()
    }

    private fun loadBookings() {
        if (userId == -1) {
            tvNoBookings.visibility = View.VISIBLE
            return
        }

        val bookings = db.bookingDao().getUserBookingsWithService(userId)

        if (bookings.isEmpty()) {
            tvNoBookings.visibility = View.VISIBLE
            listViewBookings.visibility = View.GONE
        } else {
            tvNoBookings.visibility = View.GONE
            listViewBookings.visibility = View.VISIBLE

            val adapter = BookingAdapter(this, bookings, db)
            listViewBookings.adapter = adapter
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

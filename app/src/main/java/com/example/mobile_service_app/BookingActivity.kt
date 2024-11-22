package com.example.mobile_service_app

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.mobile_service_app.AppDatabase // Changed from ServiceDatabase
import com.example.mobile_service_app.Booking
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*
import com.google.android.gms.location.*

class BookingActivity : AppCompatActivity() {
    private lateinit var tvServiceName: TextView
    private lateinit var tvServicePrice: TextView
    private lateinit var btnSelectDate: Button
    private lateinit var btnSelectTime: Button
    private lateinit var etProblemDescription: EditText
    private lateinit var btnBook: Button
    private lateinit var db: AppDatabase // Changed from ServiceDatabase
    private lateinit var etLocation: EditText
    private lateinit var btnGetLocation: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationPermissionCode = 2


    private var selectedDate: String = ""
    private var selectedTime: String = ""
    private var serviceId: Int = -1
    private var userId: Int = -1

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)
        etLocation = findViewById(R.id.etLocation)
        btnGetLocation = findViewById(R.id.btnGetLocation)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        btnGetLocation.setOnClickListener {
            getCurrentLocation()
        }


        // Initialize database
        db = AppDatabase.getDatabase(this)


        // Get service details from intent
        serviceId = intent.getIntExtra("SERVICE_ID", -1)
        userId = intent.getIntExtra("USER_ID", -1)
        val serviceName = intent.getStringExtra("SERVICE_NAME") ?: ""
        val servicePrice = intent.getDoubleExtra("SERVICE_PRICE", 0.0)

        // Initialize views
        tvServiceName = findViewById(R.id.tvServiceName)
        tvServicePrice = findViewById(R.id.tvServicePrice)
        btnSelectDate = findViewById(R.id.btnSelectDate)
        btnSelectTime = findViewById(R.id.btnSelectTime)
        etProblemDescription = findViewById(R.id.etProblemDescription)
        btnBook = findViewById(R.id.btnBook)

        // Set service details
        tvServiceName.text = serviceName
        tvServicePrice.text = "₹${servicePrice}"

        // Setup action bar
        supportActionBar?.apply {
            title = "Book Service"
            setDisplayHomeAsUpEnabled(true)
        }

        // Set click listeners
        btnSelectDate.setOnClickListener { showDatePicker() }
        btnSelectTime.setOnClickListener { showTimePicker() }
        btnBook.setOnClickListener { handleBooking() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            calendar.set(selectedYear, selectedMonth, selectedDay)
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            selectedDate = dateFormat.format(calendar.time)
            btnSelectDate.text = "Date: $selectedDate"
        }, year, month, day).apply {
            datePicker.minDate = System.currentTimeMillis() - 1000
            show()
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            val timeString = String.format("%02d:%02d", selectedHour, selectedMinute)
            selectedTime = timeString
            btnSelectTime.text = "Time: $selectedTime"
        }, hour, minute, false).show()
    }

    private fun handleBooking() {

        if (serviceId == -1 || userId == -1) {
            Toast.makeText(this, "Invalid service or user", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
            Toast.makeText(this, "Please select date and time", Toast.LENGTH_SHORT).show()
            return
        }

        val problemDescription = etProblemDescription.text.toString().trim()
        if (problemDescription.isEmpty()) {
            etProblemDescription.error = "Please describe your problem"
            return
        }
        val location = etLocation.text.toString().trim()
        if (location.isEmpty()) {
            etLocation.error = "Please provide your location"
            return
        }

        val booking = Booking(
            userId = userId,
            serviceId = serviceId,
            bookingDate = selectedDate,
            bookingTime = selectedTime,
            problemDescription = problemDescription,
            location = location
        )

        try {
            db.bookingDao().insertBooking(booking)
            Toast.makeText(this, "Booking successful!", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Error making booking: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    private fun getCurrentLocation() {
        if (checkPermission()) {
            if (isLocationEnabled()) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        // Get address from location
                        getAddressFromLocation(location)
                    } else {
                        requestNewLocationData()
                    }
                }
            } else {
                Toast.makeText(this, "Please enable location", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun getAddressFromLocation(location: Location) {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val addressText = StringBuilder()

                // Get address lines
                for (i in 0..address.maxAddressLineIndex) {
                    addressText.append(address.getAddressLine(i))
                    if (i < address.maxAddressLineIndex) addressText.append(", ")
                }

                etLocation.setText(addressText.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error getting address", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = 5000
            fastestInterval = 2000
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        getAddressFromLocation(location)
                    }
                }
            },
            Looper.getMainLooper())
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            locationPermissionCode
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            }
        }
    }
}

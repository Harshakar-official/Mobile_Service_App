package com.example.mobile_service_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import com.example.mobile_service_app.AppDatabase
import com.example.mobile_service_app.ServiceModel

class MainActivity : AppCompatActivity() {
    private lateinit var listViewServices: ListView
    private lateinit var tvWelcome: TextView
    private lateinit var searchView: SearchView// Added for search
    private lateinit var db: AppDatabase
    private var isAdmin: Boolean = false
    private var currentUserId: Int = -1
    private var allServices: List<ServiceModel> = listOf()  // Added for search
    private lateinit var adapter: ServiceAdapter  // Added for search

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        listViewServices = findViewById(R.id.listViewServices)
        tvWelcome = findViewById(R.id.tvWelcome)
        searchView = findViewById(R.id.searchView)  // Added for search

        // Get user information from intent
        val username = intent.getStringExtra("USERNAME") ?: "User"
        isAdmin = intent.getBooleanExtra("IS_ADMIN", false)
        currentUserId = intent.getIntExtra("USER_ID", -1)

        // Set welcome message
        tvWelcome.text = "Welcome, $username!"

        Log.d("MainActivity", "Username: $username")
        Log.d("MainActivity", "IsAdmin: $isAdmin")
        Log.d("MainActivity", "UserID: $currentUserId")

        // Initialize database
        db = AppDatabase.getDatabase(this)

        // Add dummy services if database is empty
        insertDummyServices()

        // Display services
        displayServices()

        // Set up list item click listener
        setupListClickListener()

        // Setup search functionality
        setupSearchView()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterServices(newText)
                return true
            }
        })
    }

    private fun filterServices(query: String?) {
        if (query.isNullOrEmpty()) {
            adapter = ServiceAdapter(this, allServices)
        } else {
            val filteredList = allServices.filter { service ->
                service.serviceName.contains(query, ignoreCase = true) ||
                        service.description.contains(query, ignoreCase = true)
            }
            adapter = ServiceAdapter(this, filteredList)
        }
        listViewServices.adapter = adapter
    }

    private fun insertDummyServices() {
        val services = db.serviceDao().getAllServices()
        if (services.isEmpty()) {
            val dummyServices = listOf(
                ServiceModel(
                    serviceName = "Screen Repair",
                    description = "Fix broken or cracked screens",
                    price = 2500.0,
                    estimatedTime = "1-2 hours"
                ),
                ServiceModel(
                    serviceName = "Battery Replacement",
                    description = "Replace old or damaged battery",
                    price = 1500.0,
                    estimatedTime = "30 minutes"
                ),
                ServiceModel(
                    serviceName = "Software Update",
                    description = "Update system and remove bugs",
                    price = 1000.0,
                    estimatedTime = "1 hour"
                ),
                ServiceModel(
                    serviceName = "Data Recovery",
                    description = "Recover lost or deleted data",
                    price = 3000.0,
                    estimatedTime = "2-3 hours"
                ),
                ServiceModel(
                    serviceName = "Camera Repair",
                    description = "Fix camera related issues",
                    price = 2000.0,
                    estimatedTime = "1 hour"
                )
            )

            dummyServices.forEach { service ->
                db.serviceDao().insertService(service)
            }
        }
    }

    private fun displayServices() {
        allServices = db.serviceDao().getAllServices()  // Modified for search
        adapter = ServiceAdapter(this, allServices)     // Modified for search
        listViewServices.adapter = adapter
    }

    private fun setupListClickListener() {
        listViewServices.setOnItemClickListener { _, _, position, _ ->
            val service = listViewServices.adapter.getItem(position) as ServiceModel
            if (isAdmin) {
                showAdminServiceOptions(service)
            } else {
                showServiceDetails(service)
            }
        }
    }

    private fun showServiceDetails(service: ServiceModel) {
        Log.d("MainActivity", "Current UserID when booking: $currentUserId")

        AlertDialog.Builder(this)
            .setTitle(service.serviceName)
            .setMessage("""
                Description: ${service.description}
                Price: ₹${service.price}
                Estimated Time: ${service.estimatedTime}
            """.trimIndent())
            .setPositiveButton("Book Now") { _, _ ->
                if (currentUserId != -1) {
                    val intent = Intent(this, BookingActivity::class.java).apply {
                        putExtra("SERVICE_ID", service.id)
                        putExtra("SERVICE_NAME", service.serviceName)
                        putExtra("SERVICE_PRICE", service.price)
                        putExtra("USER_ID", currentUserId)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showAdminServiceOptions(service: ServiceModel) {
        AlertDialog.Builder(this)
            .setTitle("Admin Options")
            .setItems(arrayOf("View Details", "Edit", "Delete")) { _, which ->
                when (which) {
                    0 -> showServiceDetails(service)
                    1 -> showEditServiceDialog(service)
                    2 -> confirmDeleteService(service)
                }
            }
            .show()
    }

    private fun showEditServiceDialog(service: ServiceModel) {
        val intent = Intent(this, AdminActivity::class.java).apply {
            putExtra("EDIT_MODE", true)
            putExtra("SERVICE_ID", service.id)
            putExtra("SERVICE_NAME", service.serviceName)
            putExtra("SERVICE_DESCRIPTION", service.description)
            putExtra("SERVICE_PRICE", service.price)
            putExtra("SERVICE_TIME", service.estimatedTime)
        }
        startActivity(intent)
    }

    private fun confirmDeleteService(service: ServiceModel) {
        AlertDialog.Builder(this)
            .setTitle("Delete Service")
            .setMessage("Are you sure you want to delete ${service.serviceName}?")
            .setPositiveButton("Delete") { _, _ ->
                try {
                    db.serviceDao().deleteService(service)
                    displayServices() // Refresh the list
                    Toast.makeText(this, "Service deleted successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error deleting service", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        menu.findItem(R.id.menu_add_service)?.isVisible = isAdmin
        menu.findItem(R.id.menu_bookings)?.isVisible = !isAdmin
        menu.findItem(R.id.menu_admin_bookings)?.isVisible = isAdmin
        menu.findItem(R.id.menu_manage_users)?.isVisible = isAdmin
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_add_service -> {
                startActivity(Intent(this, AdminActivity::class.java))
                true
            }
            R.id.menu_bookings -> {
                val intent = Intent(this, UserBookingsActivity::class.java)
                intent.putExtra("USER_ID", currentUserId)
                startActivity(intent)
                true
            }
            R.id.menu_admin_bookings -> {
                startActivity(Intent(this, AdminBookingsActivity::class.java))
                true
            }
            R.id.menu_profile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("USER_ID", currentUserId)
                startActivity(intent)
                true
            }
            R.id.menu_manage_users -> {
                startActivity(Intent(this, UserManagementActivity::class.java))
                true
            }
            R.id.menu_logout -> {
                showLogoutConfirmation()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        displayServices()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        showLogoutConfirmation()
    }
}

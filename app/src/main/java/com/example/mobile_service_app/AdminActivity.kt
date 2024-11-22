package com.example.mobile_service_app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_service_app.ServiceModel
import com.example.mobile_service_app.AppDatabase

class AdminActivity : AppCompatActivity() {
    private lateinit var etServiceName: EditText
    private lateinit var etServiceDescription: EditText
    private lateinit var etServicePrice: EditText
    private lateinit var etEstimatedTime: EditText
    private lateinit var btnAddService: Button
    private lateinit var btnClear: Button
    private lateinit var db: AppDatabase

    private var isEditMode = false
    private var serviceId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // Initialize views
        etServiceName = findViewById(R.id.etServiceName)
        etServiceDescription = findViewById(R.id.etServiceDescription)
        etServicePrice = findViewById(R.id.etServicePrice)
        etEstimatedTime = findViewById(R.id.etEstimatedTime)
        btnAddService = findViewById(R.id.btnAddService)
        btnClear = findViewById(R.id.btnClear)

        // Initialize database
        db = AppDatabase.getDatabase(this)

        // Check if we're in edit mode
        isEditMode = intent.getBooleanExtra("EDIT_MODE", false)

        // Set up action bar
        supportActionBar?.apply {
            title = if (isEditMode) "Edit Service" else "Add New Service"
            setDisplayHomeAsUpEnabled(true)
        }

        // If in edit mode, populate fields with existing service data
        if (isEditMode) {
            serviceId = intent.getIntExtra("SERVICE_ID", 0)
            etServiceName.setText(intent.getStringExtra("SERVICE_NAME"))
            etServiceDescription.setText(intent.getStringExtra("SERVICE_DESCRIPTION"))
            etServicePrice.setText(intent.getDoubleExtra("SERVICE_PRICE", 0.0).toString())
            etEstimatedTime.setText(intent.getStringExtra("SERVICE_TIME"))
            btnAddService.text = "Update Service"
        }

        btnAddService.setOnClickListener {
            if (isEditMode) {
                updateService()
            } else {
                addNewService()
            }
        }

        btnClear.setOnClickListener {
            clearFields()
        }
    }

    private fun addNewService() {
        val serviceName = etServiceName.text.toString().trim()
        val description = etServiceDescription.text.toString().trim()
        val priceStr = etServicePrice.text.toString().trim()
        val estimatedTime = etEstimatedTime.text.toString().trim()

        // Validate inputs
        if (serviceName.isEmpty()) {
            etServiceName.error = "Service name is required"
            etServiceName.requestFocus()
            return
        }

        if (description.isEmpty()) {
            etServiceDescription.error = "Description is required"
            etServiceDescription.requestFocus()
            return
        }

        if (priceStr.isEmpty()) {
            etServicePrice.error = "Price is required"
            etServicePrice.requestFocus()
            return
        }

        if (estimatedTime.isEmpty()) {
            etEstimatedTime.error = "Estimated time is required"
            etEstimatedTime.requestFocus()
            return
        }

        try {
            val price = priceStr.toDouble()

            val newService = ServiceModel(
                serviceName = serviceName,
                description = description,
                price = price,
                estimatedTime = estimatedTime
            )

            db.serviceDao().insertService(newService)
            Toast.makeText(this, "Service added successfully!", Toast.LENGTH_SHORT).show()
            clearFields()

        } catch (e: NumberFormatException) {
            etServicePrice.error = "Please enter a valid price"
            etServicePrice.requestFocus()
        } catch (e: Exception) {
            Toast.makeText(this, "Error adding service: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateService() {
        val serviceName = etServiceName.text.toString().trim()
        val description = etServiceDescription.text.toString().trim()
        val priceStr = etServicePrice.text.toString().trim()
        val estimatedTime = etEstimatedTime.text.toString().trim()

        // Validate inputs
        if (serviceName.isEmpty()) {
            etServiceName.error = "Service name is required"
            etServiceName.requestFocus()
            return
        }

        if (description.isEmpty()) {
            etServiceDescription.error = "Description is required"
            etServiceDescription.requestFocus()
            return
        }

        if (priceStr.isEmpty()) {
            etServicePrice.error = "Price is required"
            etServicePrice.requestFocus()
            return
        }

        if (estimatedTime.isEmpty()) {
            etEstimatedTime.error = "Estimated time is required"
            etEstimatedTime.requestFocus()
            return
        }

        try {
            val price = priceStr.toDouble()

            val updatedService = ServiceModel(
                id = serviceId,
                serviceName = serviceName,
                description = description,
                price = price,
                estimatedTime = estimatedTime
            )

            db.serviceDao().updateService(updatedService)
            Toast.makeText(this, "Service updated successfully!", Toast.LENGTH_SHORT).show()
            finish()

        } catch (e: NumberFormatException) {
            etServicePrice.error = "Please enter a valid price"
            etServicePrice.requestFocus()
        } catch (e: Exception) {
            Toast.makeText(this, "Error updating service: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearFields() {
        etServiceName.text.clear()
        etServiceDescription.text.clear()
        etServicePrice.text.clear()
        etEstimatedTime.text.clear()
        etServiceName.requestFocus()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

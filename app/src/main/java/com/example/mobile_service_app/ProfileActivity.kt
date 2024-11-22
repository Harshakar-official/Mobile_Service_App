package com.example.mobile_service_app

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_service_app.AppDatabase
import com.example.mobile_service_app.User

class ProfileActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnEdit: Button
    private lateinit var btnSave: Button
    private lateinit var btnChangePassword: Button
    private lateinit var db: AppDatabase
    private var currentUserId: Int = -1
    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize database
        db = AppDatabase.getDatabase(this)

        // Initialize views
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        btnEdit = findViewById(R.id.btnEdit)
        btnSave = findViewById(R.id.btnSave)
        btnChangePassword = findViewById(R.id.btnChangePassword)

        // Get current user ID from intent
        currentUserId = intent.getIntExtra("USER_ID", -1)

        // Set up action bar
        supportActionBar?.apply {
            title = "My Profile"
            setDisplayHomeAsUpEnabled(true)
        }

        if (currentUserId != -1) {
            // Load user data
            loadUserProfile()
            // Set up button click listeners
            setupButtonListeners()
        } else {
            Toast.makeText(this, "Error loading profile", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun loadUserProfile() {
        try {
            // Use getUserById instead of getUserByUsername
            val user = db.userDao().getUserById(currentUserId)
            if (user != null) {
                currentUser = user
                etUsername.setText(user.username)
                etEmail.setText(user.email)
                etPhone.setText(user.phone)
                // Initially disable editing
                setFieldsEditable(false)
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupButtonListeners() {
        btnEdit.setOnClickListener {
            setFieldsEditable(true)
            btnEdit.visibility = View.GONE
            btnSave.visibility = View.VISIBLE
        }

        btnSave.setOnClickListener {
            saveProfile()
        }

        btnChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }
    }

    private fun setFieldsEditable(editable: Boolean) {
        etEmail.isEnabled = editable
        etPhone.isEnabled = editable
        // Username is not editable
        etUsername.isEnabled = false
    }

    private fun saveProfile() {
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        // Validate inputs
        if (email.isEmpty()) {
            etEmail.error = "Email is required"
            return
        }
        if (phone.isEmpty()) {
            etPhone.error = "Phone is required"
            return
        }

        try {
            // Update user object
            val updatedUser = currentUser.copy(
                email = email,
                phone = phone
            )

            // Update in database
            db.userDao().updateUser(updatedUser)
            currentUser = updatedUser

            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()

            // Reset UI state
            setFieldsEditable(false)
            btnSave.visibility = View.GONE
            btnEdit.visibility = View.VISIBLE

        } catch (e: Exception) {
            Toast.makeText(this, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showChangePasswordDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val etCurrentPassword = view.findViewById<EditText>(R.id.etCurrentPassword)
        val etNewPassword = view.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = view.findViewById<EditText>(R.id.etConfirmPassword)

        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(view)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = etCurrentPassword.text.toString()
                val newPassword = etNewPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()

                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (currentPassword != currentUser.password) {
                    Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "New passwords don't match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                try {
                    // Update password
                    val updatedUser = currentUser.copy(password = newPassword)
                    db.userDao().updateUser(updatedUser)
                    currentUser = updatedUser
                    Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error changing password: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

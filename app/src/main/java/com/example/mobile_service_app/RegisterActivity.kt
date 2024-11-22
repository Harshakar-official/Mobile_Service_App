package com.example.mobile_service_app

import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_service_app.AppDatabase
import com.example.mobile_service_app.User

class RegisterActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnRegister: Button
    private lateinit var btnShowPassword: ImageButton
    private lateinit var btnShowConfirmPassword: ImageButton
    private lateinit var db: AppDatabase
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Set up action bar
        supportActionBar?.apply {
            title = "Register"
            setDisplayHomeAsUpEnabled(true)
        }

        // Initialize views
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        btnRegister = findViewById(R.id.btnRegister)
        btnShowPassword = findViewById(R.id.btnShowPassword)
        btnShowConfirmPassword = findViewById(R.id.btnShowConfirmPassword)

        // Initialize database
        db = AppDatabase.getDatabase(this)

        // Set up click listeners
        btnRegister.setOnClickListener {
            registerUser()
        }

        // Toggle password visibility
        btnShowPassword.setOnClickListener {
            togglePasswordVisibility(etPassword, isPasswordVisible)
            isPasswordVisible = !isPasswordVisible
        }

        btnShowConfirmPassword.setOnClickListener {
            togglePasswordVisibility(etConfirmPassword, isConfirmPasswordVisible)
            isConfirmPasswordVisible = !isConfirmPasswordVisible
        }
    }

    private fun togglePasswordVisibility(editText: EditText, isVisible: Boolean) {
        if (isVisible) {
            // Hide password
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            // Show password
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        // Move cursor to the end of text
        editText.setSelection(editText.text.length)
    }

    private fun registerUser() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()
        val confirmPassword = etConfirmPassword.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()

        // Validate empty fields
        when {
            username.isEmpty() -> {
                etUsername.error = "Username is required"
                etUsername.requestFocus()
                return
            }
            password.isEmpty() -> {
                etPassword.error = "Password is required"
                etPassword.requestFocus()
                return
            }
            confirmPassword.isEmpty() -> {
                etConfirmPassword.error = "Confirm password is required"
                etConfirmPassword.requestFocus()
                return
            }
            email.isEmpty() -> {
                etEmail.error = "Email is required"
                etEmail.requestFocus()
                return
            }
            phone.isEmpty() -> {
                etPhone.error = "Phone number is required"
                etPhone.requestFocus()
                return
            }
        }

        // Validate username length
        if (username.length < 4) {
            etUsername.error = "Username must be at least 4 characters long"
            etUsername.requestFocus()
            return
        }

        // Validate email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Please enter a valid email address"
            etEmail.requestFocus()
            return
        }

        // Validate phone number
        if (!isValidPhoneNumber(phone)) {
            etPhone.error = "Please enter a valid phone number"
            etPhone.requestFocus()
            return
        }

        // Validate password
        if (!isPasswordValid(password)) {
            return
        }

        // Check if passwords match
        if (password != confirmPassword) {
            etConfirmPassword.error = "Passwords do not match"
            etConfirmPassword.requestFocus()
            return
        }

        try {
            // Check if username already exists
            if (db.userDao().isUsernameExists(username) > 0) {
                etUsername.error = "Username already exists"
                etUsername.requestFocus()
                return
            }

            // Check if email already exists
            if (db.userDao().isEmailExists(email) > 0) {
                etEmail.error = "Email already registered"
                etEmail.requestFocus()
                return
            }

            // Create new user
            val newUser = User(
                username = username,
                password = password,
                email = email,
                phone = phone,
                isAdmin = false
            )

            // Insert user into database
            db.userDao().registerUser(newUser)
            Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
            finish()

        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Error registering user: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        if (password.length < 6) {
            etPassword.error = "Password must be at least 6 characters long"
            etPassword.requestFocus()
            return false
        }

        if (!password.contains(Regex("[0-9]"))) {
            etPassword.error = "Password must contain at least one digit"
            etPassword.requestFocus()
            return false
        }

        if (!password.contains(Regex("[!@#\$%^&*(),.?\":{}|<>]"))) {
            etPassword.error = "Password must contain at least one special character"
            etPassword.requestFocus()
            return false
        }

        return true
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        // Basic phone number validation (adjust pattern as needed)
        return phone.matches(Regex("^[0-9]{10,12}$"))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

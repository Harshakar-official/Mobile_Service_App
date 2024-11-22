package com.example.mobile_service_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile_service_app.User
import com.example.mobile_service_app.AppDatabase

class LoginActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoToRegister: Button
    private lateinit var db: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize database
        db = AppDatabase.getDatabase(this)

        // Create admin user if not exists
        createAdminUser()

        // Initialize views
        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoToRegister = findViewById(R.id.btnGoToRegister)

        // Set up login button click listener
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()
            handleLogin()
        }



        // Set up register button click listener
        btnGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun handleLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString().trim()

        // Validate input fields
        if (username.isEmpty()) {
            etUsername.error = "Username is required"
            etUsername.requestFocus()
            return
        }

        if (password.isEmpty()) {
            etPassword.error = "Password is required"
            etPassword.requestFocus()
            return
        }

        // Attempt login
        val user = db.userDao().login(username, password)
        if (user != null) {
            Log.d("LoginActivity", "User ID: ${user.id}")
            Log.d("LoginActivity", "Username: ${user.username}")
            Log.d("LoginActivity", "Is Admin: ${user.isAdmin}")
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()

            // Create intent and add user data
            val intent = Intent(this, MainActivity::class.java).apply {
                putExtra("USERNAME", user.username)
                putExtra("IS_ADMIN", user.isAdmin)
                putExtra("USER_ID", user.id)  // Make sure this line is included
            }

            // Start MainActivity and clear back stack
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show()
        }
    }


    private fun createAdminUser() {
        // Check if admin exists
        val adminUser = db.userDao().getUserByUsername("admin")
        if (adminUser == null) {
            // Create admin user
            val admin = User(
                username = "admin",
                password = "admin123",
                email = "admin@example.com",
                phone = "1234567890",
                isAdmin = true
            )
            try {
                db.userDao().registerUser(admin)
                Toast.makeText(
                    this,
                    "Admin account created successfully",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    "Error creating admin account: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // This will exit the app when back is pressed from login screen
        moveTaskToBack(true)
    }
}

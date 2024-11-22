package com.example.mobile_service_app

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Button
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.mobile_service_app.AppDatabase
import com.example.mobile_service_app.User

class UserListAdapter(context: Context, private val users: List<User>) :
    ArrayAdapter<User>(context, android.R.layout.simple_list_item_1, users) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)

        val user = users[position]
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = "${user.username} (${user.email})"

        return view
    }
}

class UserManagementActivity : AppCompatActivity() {
    private lateinit var listViewUsers: ListView
    private lateinit var db: AppDatabase
    private lateinit var btnAddUser: Button
    private lateinit var userAdapter: UserListAdapter
    private lateinit var users: List<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)

        // Initialize views and database
        listViewUsers = findViewById(R.id.listViewUsers)
        btnAddUser = findViewById(R.id.btnAddUser)
        db = AppDatabase.getDatabase(this)

        // Set up action bar
        supportActionBar?.apply {
            title = "User Management"
            setDisplayHomeAsUpEnabled(true)
        }

        // Display users
        displayUsers()

        // Add new user button click listener
        btnAddUser.setOnClickListener {
            showAddUserDialog()
        }

        // Set up list item click listener
        listViewUsers.setOnItemClickListener { _, _, position, _ ->
            val user = users[position]
            showUserOptions(user)
        }
    }

    private fun displayUsers() {
        users = db.userDao().getAllUsers()
        userAdapter = UserListAdapter(this, users)
        listViewUsers.adapter = userAdapter
    }

    private fun showAddUserDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null)
        val etUsername = dialogView.findViewById<EditText>(R.id.etUsername)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
        val etPhone = dialogView.findViewById<EditText>(R.id.etPhone)

        AlertDialog.Builder(this)
            .setTitle("Add New User")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val username = etUsername.text.toString().trim()
                val password = etPassword.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val phone = etPhone.text.toString().trim()

                if (username.isEmpty() || password.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Check if username already exists
                val existingUser = db.userDao().getUserByUsername(username)
                if (existingUser != null) {
                    Toast.makeText(this, "Username already exists", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newUser = User(
                    username = username,
                    password = password,
                    email = email,
                    phone = phone,
                    isAdmin = false
                )

                try {
                    db.userDao().registerUser(newUser)
                    Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show()
                    displayUsers() // Refresh the list
                } catch (e: Exception) {
                    Toast.makeText(this, "Error adding user: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showUserOptions(user: User) {
        if (user.isAdmin) {
            Toast.makeText(this, "Cannot modify admin account", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("User Options")
            .setItems(arrayOf("View Details", "Change Password", "Delete User")) { _, which ->
                when (which) {
                    0 -> showUserDetails(user)
                    1 -> showChangePasswordDialog(user)
                    2 -> confirmDeleteUser(user)
                }
            }
            .show()
    }

    private fun showUserDetails(user: User) {
        AlertDialog.Builder(this)
            .setTitle("User Details")
            .setMessage("""
                Username: ${user.username}
                Email: ${user.email}
                Phone: ${user.phone}
                Admin: ${if (user.isAdmin) "Yes" else "No"}
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    @SuppressLint("MissingInflatedId")
    private fun showChangePasswordDialog(user: User) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_change_password_admin, null)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)

        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val newPassword = etNewPassword.text.toString().trim()
                val confirmPassword = etConfirmPassword.text.toString().trim()

                if (newPassword.isEmpty()) {
                    Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (newPassword != confirmPassword) {
                    Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                try {
                    val updatedUser = user.copy(password = newPassword)
                    db.userDao().updateUser(updatedUser)
                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(this, "Error updating password: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteUser(user: User) {
        AlertDialog.Builder(this)
            .setTitle("Delete User")
            .setMessage("Are you sure you want to delete user ${user.username}?")
            .setPositiveButton("Delete") { _, _ ->
                try {
                    db.userDao().deleteUser(user)
                    Toast.makeText(this, "User deleted successfully", Toast.LENGTH_SHORT).show()
                    displayUsers() // Refresh the list
                } catch (e: Exception) {
                    Toast.makeText(this, "Error deleting user: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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

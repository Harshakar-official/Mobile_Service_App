package com.example.mobile_service_app

import androidx.room.*

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    fun login(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: Int): User?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun registerUser(user: User)

    @Update
    fun updateUser(user: User)

    @Query("SELECT * FROM users")
    fun getAllUsers(): List<User>

    @Delete
    fun deleteUser(user: User)

    @Query("DELETE FROM users")
    fun deleteAllUsers()

    @Query("SELECT COUNT(*) FROM users WHERE username = :username")
    fun isUsernameExists(username: String): Int

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    fun isEmailExists(email: String): Int
}

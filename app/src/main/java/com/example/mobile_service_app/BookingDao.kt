package com.example.mobile_service_app

import androidx.room.*
import com.example.mobile_service_app.Booking
import com.example.mobile_service_app.BookingWithUserAndService

@Dao
interface BookingDao {
    @Transaction
    @Query("SELECT * FROM bookings ORDER BY id DESC")
    fun getAllBookingsWithUserAndService(): List<BookingWithUserAndService>

    @Transaction
    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY id DESC")
    fun getUserBookingsWithService(userId: Int): List<BookingWithUserAndService>

    @Insert
    fun insertBooking(booking: Booking)

    @Update
    fun updateBooking(booking: Booking)

    @Delete
    fun deleteBooking(booking: Booking)

    @Query("SELECT * FROM bookings WHERE id = :bookingId")
    fun getBookingById(bookingId: Int): Booking?

    @Transaction
    @Query("SELECT * FROM bookings WHERE id = :bookingId")
    fun getBookingWithUserAndService(bookingId: Int): BookingWithUserAndService?

    @Query("SELECT * FROM bookings WHERE status = :status ORDER BY id DESC")
    fun getBookingsByStatus(status: String): List<Booking>

    @Query("UPDATE bookings SET status = :newStatus WHERE id = :bookingId")
    fun updateBookingStatus(bookingId: Int, newStatus: String)

    @Query("SELECT * FROM bookings WHERE bookingDate = :date")
    fun getBookingsForDate(date: String): List<Booking>
}

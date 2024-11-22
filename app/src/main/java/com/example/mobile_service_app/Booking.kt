package com.example.mobile_service_app

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import java.util.Date

@Entity(tableName = "bookings",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"]
        ),
        ForeignKey(
            entity = ServiceModel::class,
            parentColumns = ["id"],
            childColumns = ["serviceId"]
        )
    ])

data class Booking(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val serviceId: Int,
    val bookingDate: String,
    val bookingTime: String,
    val problemDescription: String, // This is the field name we used before
    val status: String = "Pending",
    val location: String = ""
)

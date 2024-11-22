package com.example.mobile_service_app

import androidx.room.Embedded
import androidx.room.Relation

data class BookingWithUserAndService(
    @Embedded
    val booking: Booking,

    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val user: User,

    @Relation(
        parentColumn = "serviceId",
        entityColumn = "id"
    )
    val service: ServiceModel
)

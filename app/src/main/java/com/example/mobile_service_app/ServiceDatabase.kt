package com.example.mobile_service_app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mobile_service_app.ServiceModel

@Database(
    entities = [User::class, ServiceModel::class, Booking::class],
    version = 1,
    exportSchema = false
)
abstract class ServiceDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun serviceDao(): ServiceDao
    abstract fun bookingDao(): BookingDao
    companion object {
        private var instance: ServiceDatabase? = null

        fun getDatabase(context: Context): ServiceDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(
                    context.applicationContext,
                    ServiceDatabase::class.java,
                    "service_database"
                ).allowMainThreadQueries()
                    .build()
            }
            return instance!!
        }
    }
}

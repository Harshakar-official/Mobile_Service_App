package com.example.mobile_service_app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mobile_service_app.User
import com.example.mobile_service_app.ServiceModel
import com.example.mobile_service_app.Booking
import com.example.mobile_service_app.BookingDao
import com.example.mobile_service_app.ServiceDao
import com.example.mobile_service_app.UserDao

@Database(
    entities = [User::class, ServiceModel::class, Booking::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun serviceDao(): ServiceDao
    abstract fun bookingDao(): BookingDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mobile_service_database"
                )
                    .fallbackToDestructiveMigration() // This will recreate tables if schema changes
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

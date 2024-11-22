package com.example.mobile_service_app

import androidx.room.*
import com.example.mobile_service_app.ServiceModel

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services")
    fun getAllServices(): List<ServiceModel>

    // Fix: Change :serviceId to :id in the query to match parameter name
    @Query("SELECT * FROM services WHERE id = :id")
    fun getServiceById(id: Int): ServiceModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertService(service: ServiceModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMultipleServices(services: List<ServiceModel>)

    @Update
    fun updateService(service: ServiceModel)

    @Delete
    fun deleteService(service: ServiceModel)

    @Query("DELETE FROM services")
    fun deleteAllServices()

    @Query("SELECT * FROM services WHERE serviceName LIKE '%' || :searchQuery || '%'")
    fun searchServices(searchQuery: String): List<ServiceModel>
}

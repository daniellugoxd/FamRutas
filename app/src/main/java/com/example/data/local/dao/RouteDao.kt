package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.RouteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {

    @Query("SELECT * FROM routes ORDER BY timestamp DESC")
    fun getAllRoutes(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE isSharedWithFamily = 1 ORDER BY timestamp DESC")
    fun getSharedRoutes(): Flow<List<RouteEntity>>

    @Query("SELECT * FROM routes WHERE isActive = 1 LIMIT 1")
    fun getActiveRoute(): Flow<RouteEntity?>

    @Query("SELECT * FROM routes WHERE id = :id")
    suspend fun getRouteById(id: Long): RouteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity): Long

    @Update
    suspend fun updateRoute(route: RouteEntity)

    @Query("UPDATE routes SET isSharedWithFamily = :isShared WHERE id = :id")
    suspend fun updateSharedStatus(id: Long, isShared: Boolean)

    @Query("UPDATE routes SET isActive = 0")
    suspend fun clearActiveRoutes()

    @Query("UPDATE routes SET isActive = 1 WHERE id = :id")
    suspend fun setActiveRoute(id: Long)

    @Delete
    suspend fun deleteRoute(route: RouteEntity)

    @Query("DELETE FROM routes WHERE id = :id")
    suspend fun deleteRouteById(id: Long)
}

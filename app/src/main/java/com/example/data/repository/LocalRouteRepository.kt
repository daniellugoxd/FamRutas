package com.example.data.repository

import android.content.Context
import com.example.data.local.FamRutaDatabase
import com.example.data.local.dao.RouteDao
import com.example.data.local.entity.RouteEntity
import kotlinx.coroutines.flow.Flow

class LocalRouteRepository(private val routeDao: RouteDao) {

    constructor(context: Context) : this(
        FamRutaDatabase.getInstance(context).routeDao()
    )

    val allRoutes: Flow<List<RouteEntity>> = routeDao.getAllRoutes()
    val sharedRoutes: Flow<List<RouteEntity>> = routeDao.getSharedRoutes()
    val activeRoute: Flow<RouteEntity?> = routeDao.getActiveRoute()

    suspend fun saveRoute(route: RouteEntity): Long {
        return routeDao.insertRoute(route)
    }

    suspend fun setRouteShared(routeId: Long, isShared: Boolean) {
        routeDao.updateSharedStatus(routeId, isShared)
    }

    suspend fun activateRoute(routeId: Long) {
        routeDao.clearActiveRoutes()
        routeDao.setActiveRoute(routeId)
    }

    suspend fun deleteRoute(routeId: Long) {
        routeDao.deleteRouteById(routeId)
    }
}

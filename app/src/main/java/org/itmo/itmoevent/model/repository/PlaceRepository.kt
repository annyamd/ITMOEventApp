package org.itmo.itmoevent.model.repository

import android.util.Log
import org.itmo.itmoevent.model.data.dto.PlaceDto
import org.itmo.itmoevent.model.data.entity.Place
import org.itmo.itmoevent.model.network.PlaceApi

class PlaceRepository(private val placeApi: PlaceApi) {

    suspend fun getPlace(placeId: Int): Place? {
        Log.i("retrofit", "Try to load place")
        return try {
            val response = placeApi.getPlaceById(placeId)
            if (response.isSuccessful) {
                response.body()?.let {
                    mapPlaceDtoToEntity(it)
                }
            } else {
                null
            }
        } catch (ex: Exception) {
            Log.i("retrofit", ex.stackTraceToString())
            null
        }
    }

    private fun mapPlaceDtoToEntity(placeDto: PlaceDto) = Place(
        placeDto.id,
        placeDto.name,
        placeDto.address,
        placeDto.format,
        placeDto.room,
        placeDto.description,
        placeDto.latitude,
        placeDto.longitude,
        placeDto.renderInfo
    )

}
package org.itmo.itmoevent.model.repository

import android.util.Log
import org.itmo.itmoevent.model.data.dto.EventShortDto
import org.itmo.itmoevent.model.data.entity.EventShort
import org.itmo.itmoevent.model.network.EventApi
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

class EventRepository(private val eventApi: EventApi) {

    suspend fun getAllEvents(
        title: String? = null,
        from: Date? = null,
        to: Date? = null,
        status: String? = null,
        format: String? = null
    ): List<EventShort>? {
        return try {
            Log.i("retrofit", "Try to load events")
            val response = eventApi.getEvents(title, from, to, status, format)
            if (response.isSuccessful) {
                response.body()?.items?.map {
                    Log.i("retrofit", "Events loaded correctly: $it")
                    mapEventShortDtoToEntity(it)
                }
            } else {
                Log.i("retrofit", "Events: ${response.code()}, ${response.message()}")
                null
            }
        } catch (ex: Exception) {
            Log.i("retrofit", ex.stackTraceToString())
            null
        }
    }


    suspend fun getUserEventsByRole(roleId: Int): List<EventShort>? {
        return try {
            Log.i("retrofit", "Try to load user events, roleId = $roleId")
            val response = eventApi.getUserEventsByRole(roleId)
            if (response.isSuccessful) {
                Log.i("retrofit", "User events loaded correctly: ${response.body()}")
                response.body()?.let { list ->
                    list.filter {
                        it.status != "DRAFT"
                    }.map {
                        mapEventShortDtoToEntity(it)
                    }
                }
            } else {
                null
            }
        } catch (ex: Exception) {
            Log.i("retrofit", ex.stackTraceToString())
            null
        }
    }

    private fun mapEventShortDtoToEntity(entity: EventShortDto) =
        EventShort(
            entity.id,
            entity.title,
            entity.shortDesc,
            entity.status,
            getLocalDateTime(entity.start),
            getLocalDateTime(entity.end),
            entity.format
        )

    private fun getLocalDateTime(date: Date?) : LocalDateTime? {
        return date?.let {
            LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
        }
    }
}

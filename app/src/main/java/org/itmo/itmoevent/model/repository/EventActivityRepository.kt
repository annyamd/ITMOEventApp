package org.itmo.itmoevent.model.repository

import android.util.Log
import org.itmo.itmoevent.model.data.dto.EventDto
import org.itmo.itmoevent.model.data.entity.EventsActivity
import org.itmo.itmoevent.model.network.EventApi
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

class EventActivityRepository(private val eventApi: EventApi) {

    suspend fun getActivity(eventId: Int): EventsActivity? {
        Log.i("retrofit", "Try to load event info")
        return try {
            val response = eventApi.getEventInfo(eventId)
            if (response.isSuccessful) {
                response.body()?.let {
                    mapEventInfoDtoToEntity(it)
                }
            } else {
                null
            }
        } catch (ex: Exception) {
            Log.i("retrofit", ex.stackTraceToString())
            null
        }
    }

    private fun mapEventInfoDtoToEntity(eventDto: EventDto) = EventsActivity(
        eventDto.id,
        eventDto.title,
        eventDto.shortDescription,
        eventDto.fullDescription,
        eventDto.placeId,
        eventDto.status,
        getLocalDateTime(eventDto.startDate),
        getLocalDateTime(eventDto.endDate),
        eventDto.format,
        getLocalDateTime(eventDto.registrationStart),
        getLocalDateTime(eventDto.registrationEnd),
        eventDto.participantLimit,
        eventDto.participantAgeLowest,
        eventDto.participantAgeHighest,
        getLocalDateTime(eventDto.preparingStart),
        getLocalDateTime(eventDto.preparingEnd)
    )

    private fun getLocalDateTime(date: Date?) = date?.let {
        LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
    }

}

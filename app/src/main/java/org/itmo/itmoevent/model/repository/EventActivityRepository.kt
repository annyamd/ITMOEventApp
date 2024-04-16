package org.itmo.itmoevent.model.repository

import android.util.Log
import org.itmo.itmoevent.model.data.dto.EventDto
import org.itmo.itmoevent.model.data.entity.EventsActivity
import org.itmo.itmoevent.model.network.EventApi
import java.time.LocalDateTime
import java.time.ZoneId

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
        LocalDateTime.ofInstant(eventDto.startDate.toInstant(), ZoneId.systemDefault()),
        LocalDateTime.ofInstant(eventDto.endDate.toInstant(), ZoneId.systemDefault()),
        eventDto.format,
        LocalDateTime.ofInstant(
            eventDto.registrationStart.toInstant(),
            ZoneId.systemDefault()
        ),
        LocalDateTime.ofInstant(
            eventDto.registrationEnd.toInstant(),
            ZoneId.systemDefault()
        ),
        eventDto.participantLimit,
        eventDto.participantAgeLowest,
        eventDto.participantAgeHighest,
        LocalDateTime.ofInstant(
            eventDto.preparingStart.toInstant(),
            ZoneId.systemDefault()
        ),
        LocalDateTime.ofInstant(eventDto.preparingEnd.toInstant(), ZoneId.systemDefault())
    )

}

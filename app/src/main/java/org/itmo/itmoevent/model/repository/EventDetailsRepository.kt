package org.itmo.itmoevent.model.repository

import android.util.Log
import org.itmo.itmoevent.model.data.dto.EventDto
import org.itmo.itmoevent.model.data.dto.EventShortDto
import org.itmo.itmoevent.model.data.dto.ParticipantDto
import org.itmo.itmoevent.model.data.dto.PlaceDto
import org.itmo.itmoevent.model.data.dto.UserRoleDto
import org.itmo.itmoevent.model.data.entity.Event
import org.itmo.itmoevent.model.data.entity.EventShort
import org.itmo.itmoevent.model.data.entity.Participant
import org.itmo.itmoevent.model.data.entity.Place
import org.itmo.itmoevent.model.data.entity.UserRole
import org.itmo.itmoevent.model.network.EventApi
import org.itmo.itmoevent.model.network.PlaceApi
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

class EventDetailsRepository(private val eventApi: EventApi, private val placeApi: PlaceApi) {

    suspend fun updateEventParticipantIsMarked(
        eventId: Int,
        participantId: Int,
        isMarked: Boolean
    ): Boolean {
        return try {
            val response = eventApi.markEventParticipants(eventId, participantId, isMarked)
            response.isSuccessful
        } catch (ex: Exception) {
            false
        }
    }

    suspend fun getActivities(eventId: Int): List<EventShort>? {
        Log.i("retrofit", "Try to load activities")

        return try {
            val response = eventApi.getEventActivities(eventId)
            if (response.isSuccessful) {
                response.body()?.items?.map {
                    mapActivityDtoToEntity(it)
                }
            } else {
                null
            }
        } catch (ex: Exception) {
            Log.i("retrofit", ex.stackTraceToString())
            null
        }
    }

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

    suspend fun getEventInfo(eventId: Int): Event? {
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

    suspend fun getOrgs(eventId: Int): List<UserRole>? {
        Log.i("retrofit", "Try to load orgs")
        return try {
            val response = eventApi.getEventOrganizers(eventId)
            if (response.isSuccessful) {
                response.body()?.map {
                    mapOrgsDtoToEntity(it)
                }
            } else {
                null
            }
        } catch (ex: Exception) {
            Log.i("retrofit", ex.stackTraceToString())
            null
        }
    }

    suspend fun getParticipants(eventId: Int): List<Participant>? {
        Log.i("retrofit", "Try to load participants")

        return try {
            val response = eventApi.getEventParticipants(eventId)
            if (response.isSuccessful) {
                response.body()?.map {
                    mapParticipantDtoToEntity(it)
                }
            } else {
                null
            }
        } catch (ex: Exception) {
            Log.i("retrofit", ex.stackTraceToString())
            null
        }
    }


    private fun mapEventInfoDtoToEntity(eventDto: EventDto) = Event(
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

    private fun mapActivityDtoToEntity(activity: EventShortDto) = EventShort(
        activity.id,
        activity.title,
        activity.shortDesc,
        activity.status,
        LocalDateTime.ofInstant(activity.start?.toInstant(), ZoneId.systemDefault()),
        LocalDateTime.ofInstant(activity.end?.toInstant(), ZoneId.systemDefault()),
        activity.format
    )

    private fun mapOrgsDtoToEntity(orgDto: UserRoleDto) = UserRole(
        orgDto.id,
        orgDto.name,
        orgDto.surname,
        orgDto.roleName
    )

    private fun mapParticipantDtoToEntity(participantDto: ParticipantDto) = Participant(
        participantDto.id,
        participantDto.name,
        participantDto.email,
        participantDto.additionalInfo,
        participantDto.visited
    )

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

    private fun getLocalDateTime(date: Date?) = date?.let {
        LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
    }

}

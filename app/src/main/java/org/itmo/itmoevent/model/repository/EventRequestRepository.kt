package org.itmo.itmoevent.model.repository

import android.util.Log
import org.itmo.itmoevent.model.data.entity.EventRequest
import org.itmo.itmoevent.model.network.EventApi

class EventRequestRepository(private val eventApi: EventApi) {

    suspend fun getEventsRequests(): List<EventRequest>? {
        try {
            val response = eventApi.getEventRequests(ORGANIZER_ROLE_ID)
            return if (response.isSuccessful) {
                response.body()?.let { list ->
                    list.filter {
                        it.status == "" || it.status == null
                    }.map {
                        EventRequest(
                            it.id,
                            it.title ?: ""
                        )
                    }
                }
                } else {
                    null
                }
            } catch (ex: Exception) {
                Log.i("retrofit", ex.stackTraceToString())
                return null
            }
        }

        companion object {
            private const val ORGANIZER_ROLE_ID: Int = 3
        }

    }

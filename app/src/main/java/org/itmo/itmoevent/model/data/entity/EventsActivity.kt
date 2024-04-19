package org.itmo.itmoevent.model.data.entity

import java.time.LocalDateTime

data class EventsActivity (
    val id: Int,
    val title: String?,
    val shortDesc: String?,
    val longDesc: String?,
    val placeId: Int?,
    val status: String?,
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?,
    val format: String?,
    val regStart: LocalDateTime?,
    val regEnd: LocalDateTime?,
    val participantLimit: Int?,
    val participantAgeLowest: Int?,
    val participantAgeHighest: Int?,
    val preparingStart: LocalDateTime?,
    val preparingEnd: LocalDateTime?
)
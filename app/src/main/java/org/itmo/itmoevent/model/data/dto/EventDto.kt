package org.itmo.itmoevent.model.data.dto

import java.util.Date

data class EventDto(
    val id: Int,
    val title: String,
    val shortDescription: String?,
    val fullDescription: String?,
    val placeId: Int?,
    val status: String?,
    val startDate: Date?,
    val endDate: Date?,
    val format: String?,
    val registrationStart: Date?,
    val registrationEnd: Date?,
    val participantLimit: Int?,
    val participantAgeLowest: Int?,
    val participantAgeHighest: Int?,
    val preparingStart: Date?,
    val preparingEnd: Date?
)

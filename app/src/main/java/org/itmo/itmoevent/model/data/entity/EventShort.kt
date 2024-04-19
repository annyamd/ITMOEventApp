package org.itmo.itmoevent.model.data.entity

import java.time.LocalDateTime

data class EventShort(
    val id: Int,
    val title: String?,
    val shortDesc: String?,
    val status: String?,
    val start: LocalDateTime?,
    val end: LocalDateTime?,
    val format: String?
)

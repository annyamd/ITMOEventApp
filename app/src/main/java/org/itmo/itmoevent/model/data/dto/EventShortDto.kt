package org.itmo.itmoevent.model.data.dto

import com.google.gson.annotations.SerializedName
import java.util.Date

data class EventShortDto(
    val id: Int,
    val title: String?,
    @SerializedName("shortDescription")
    val shortDesc: String?,
    val status: String?,
    @SerializedName("startDate")
    val start: Date?,
    @SerializedName("endDate")
    val end: Date?,
    val format: String?
)

data class EventListDto(
    val items: List<EventShortDto>
)

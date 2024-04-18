package org.itmo.itmoevent.model.data.entity

data class Place(
    val id: Int,
    val name: String,
    val address: String,
    val format: String,
    val room: String,
    val description: String,
    val latitude: Float,
    val longitude: Float,
    val renderInfo: String?
)

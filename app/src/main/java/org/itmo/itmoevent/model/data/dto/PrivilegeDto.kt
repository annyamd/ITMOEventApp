package org.itmo.itmoevent.model.data.dto

import org.itmo.itmoevent.model.data.entity.enums.PrivilegeName

data class PrivilegeDto(
    val id: Int,
    val name: PrivilegeName,
    val description: String
)

data class PrivilegesListDto(
    val privileges: List<PrivilegeDto>
)
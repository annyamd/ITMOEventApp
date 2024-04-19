package org.itmo.itmoevent.model.repository

import android.util.Log
import org.itmo.itmoevent.model.data.dto.PrivilegeDto
import org.itmo.itmoevent.model.data.entity.Privilege
import org.itmo.itmoevent.model.data.entity.Role
import org.itmo.itmoevent.model.data.entity.enums.PrivilegeName
import org.itmo.itmoevent.model.network.RoleApi

class RoleRepository(private val roleApi: RoleApi) {

    var systemPrivileges: List<Privilege>? = null
    var systemPrivilegesNames: List<PrivilegeName>? = null


    suspend fun loadSystemPrivileges(): List<Privilege>? {
        Log.i("retrofit", "Try to load sys privileges")
        if (systemPrivileges == null) {
            try {
                val response = roleApi.getUserSystemPrivileges()
                if (response.isSuccessful) {
                    response.body()?.privileges?.let { list ->
                        systemPrivileges = list.map {
                            mapPrivilegeDtoToEntity(it)
                        }
                        Log.i("retrofit", "Priv loaded correctly: $systemPrivileges")

                        systemPrivilegesNames = list.map {
                            it.name
                        }
                    }
                } else {
                    return null
                }
            } catch (ex: Exception) {
                Log.i("retrofit", ex.stackTraceToString())
                return null
            }
        }

        return systemPrivileges
    }

    suspend fun getOrgRoles(): List<Role>? {
        try {
            Log.i("retrofit", "Try to load org roles")
            val response = roleApi.getOrganisationalRoles()
            return if (response.isSuccessful) {
                Log.i("retrofit", "Org roles loaded correctly: ${response.body()}")
                response.body()?.map {
                    Role(
                        it.id,
                        it.name
                    )
                }
            } else {
                null
            }
        } catch (ex: Exception) {
            Log.i("retrofit", ex.stackTraceToString())
            return null
        }
    }

    private fun mapPrivilegeDtoToEntity(privilegeDto: PrivilegeDto) = Privilege(
        privilegeDto.id,
        privilegeDto.name,
        privilegeDto.description
    )

}
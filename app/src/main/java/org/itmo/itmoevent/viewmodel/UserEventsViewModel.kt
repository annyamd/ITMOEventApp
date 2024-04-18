package org.itmo.itmoevent.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import org.itmo.itmoevent.model.data.entity.EventShort
import org.itmo.itmoevent.model.data.entity.Role
import org.itmo.itmoevent.model.repository.EventRepository
import org.itmo.itmoevent.model.repository.RoleRepository
import org.itmo.itmoevent.model.repository.EventRequestRepository

class UserEventsViewModel(
    private val eventReqRepository: EventRequestRepository,
    private val rolesRepository: RoleRepository,
    private val eventRepository: EventRepository
) : ViewModel() {

    private var roleList: List<Role>? = null

    val eventRequestsLiveData = ContentLiveDataProvider(
//        !rolesRepository.systemPrivilegesNames!!.contains(PrivilegeName.EDIT_EVENT_INFO),
        false,
        viewModelScope
    ) {
        viewModelScope.async { eventReqRepository.getEventsRequests() }
    }.contentLiveData

    val roleListLiveData = ContentLiveDataProvider(
        false,
        viewModelScope
    ) {
        viewModelScope.async {
            roleList = rolesRepository.getOrgRoles() ?: emptyList()
            roleList?.map {
                it.name
            }
        }
    }.contentLiveData

    var roleNameIndex = MutableLiveData<Int>()

    val roleEventListLiveData = roleNameIndex.switchMap { roleIndex ->
        ContentLiveDataProvider<List<EventShort>?>(
            false,
            viewModelScope
        ) {
            viewModelScope.async {
                roleList?.get(roleIndex)?.let {
                    eventRepository.getUserEventsByRole(it.id)
                }
            }
        }.contentLiveData
    }


    class UserEventsViewModelFactory(
        private val eventRequestRepository: EventRequestRepository,
        private val rolesRepository: RoleRepository,
        private val eventRepository: EventRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(UserEventsViewModel::class.java)) {
                return UserEventsViewModel(
                    eventRequestRepository,
                    rolesRepository,
                    eventRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}

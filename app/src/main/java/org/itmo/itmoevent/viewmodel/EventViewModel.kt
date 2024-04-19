package org.itmo.itmoevent.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.itmo.itmoevent.model.data.entity.Participant
import org.itmo.itmoevent.model.data.entity.UserRole
import org.itmo.itmoevent.model.data.entity.enums.PrivilegeName
import org.itmo.itmoevent.model.repository.EventDetailsRepository
import org.itmo.itmoevent.model.repository.RoleRepository
import org.itmo.itmoevent.viewmodel.ContentLiveDataProvider.ContentItemUIState.*

class EventViewModel(
    private val eventId: Int,
    private val eventDetailsRepository: EventDetailsRepository,
    roleRepository: RoleRepository
) : ViewModel() {

    fun markEventParticipant(participantId: Int, isMarked: Boolean) = viewModelScope.launch {
        eventDetailsRepository.updateEventParticipantIsMarked(eventId, participantId, isMarked)
    }

    fun markEventParticipantsAll() = viewModelScope.launch {
        participantsLiveData.value?.let {
            participants
                ?.forEach {
                    eventDetailsRepository.updateEventParticipantIsMarked(eventId, it.id, true)
                }
        }
    }

    val placeLiveData = ContentLiveDataProvider(
        !roleRepository.systemPrivilegesNames!!.contains(PrivilegeName.VIEW_EVENT_PLACE),
        viewModelScope
    ) {
        viewModelScope.async {
            eventDetailsRepository.getEventInfo(eventId)?.placeId?.let {
                eventDetailsRepository.getPlace(it)
            }
        }
    }.contentLiveData


    val activitiesLiveData = ContentLiveDataProvider(
        !roleRepository.systemPrivilegesNames!!.contains(PrivilegeName.VIEW_EVENT_ACTIVITIES),
        viewModelScope
    ) {
        viewModelScope.async { eventDetailsRepository.getActivities(eventId) }
    }.contentLiveData

    val eventInfoLiveData = ContentLiveDataProvider(
        false,
        viewModelScope
    ) {
        viewModelScope.async { eventDetailsRepository.getEventInfo(eventId) }
    }.contentLiveData

    val orgsLiveData = ContentLiveDataProvider(
        false,
//        !roleRepository.systemPrivilegesNames!!.contains(PrivilegeName.VIEW_ORGANIZER_USERS),
        viewModelScope
    ) {
        viewModelScope.async {
            eventDetailsRepository.getOrgs(eventId)

        }
    }.contentLiveData

    private var participants: List<Participant>? = null
    val participantsLiveData = ContentLiveDataProvider(
        false,
        viewModelScope
    ) {
        viewModelScope.async {
            participants = eventDetailsRepository.getParticipants(eventId)
            participants
        }
    }.contentLiveData


    val roleName = MutableLiveData<String>()
    val roleOrganizersList: LiveData<List<UserRole>?> = roleName.map { role ->
        if (orgsLiveData.value is Success<*>) {
            val orgList = (orgsLiveData.value as Success<*>).content as List<UserRole>?
            orgList?.filter {
                it.roleName == role
            }
        } else {
            emptyList()
        }
    }

    val rolesList: LiveData<List<String>> = orgsLiveData.map { orgsState ->
        if (orgsState is Success<*>) {
            val orgList = orgsState.content as List<UserRole>?
            orgList?.map {
                it.roleName
            }?.distinct() ?: emptyList()
        } else {
            emptyList()
        }
    }


    class EventViewModelFactory(
        private val eventId: Int,
        private val eventDetailsRepository: EventDetailsRepository,
        private val roleRepository: RoleRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EventViewModel::class.java)) {
                return EventViewModel(eventId, eventDetailsRepository, roleRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}
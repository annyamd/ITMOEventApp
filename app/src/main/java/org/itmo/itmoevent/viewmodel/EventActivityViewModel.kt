package org.itmo.itmoevent.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import org.itmo.itmoevent.model.data.entity.enums.PrivilegeName
import org.itmo.itmoevent.model.repository.EventActivityRepository
import org.itmo.itmoevent.model.repository.PlaceRepository
import org.itmo.itmoevent.model.repository.RoleRepository

class EventActivityViewModel(
    private val activityId: Int,
    private val activityRepository: EventActivityRepository,
    private val placeRepository: PlaceRepository,
    roleRepository: RoleRepository
) : ViewModel() {

    val activityInfoLiveData = ContentItemLiveDataProvider(
        false,
        viewModelScope
    ) {
        viewModelScope.async { activityRepository.getActivity(activityId) }
    }.contentLiveData

    val placeLiveData = ContentItemLiveDataProvider(
        !roleRepository.systemPrivilegesNames!!.contains(PrivilegeName.VIEW_EVENT_PLACE),
        viewModelScope
    ) {
        viewModelScope.async {
            activityRepository.getActivity(activityId)?.placeId?.let {
                placeRepository.getPlace(it)
            }
        }
    }.contentLiveData

    class ActivityViewModelModelFactory(
        private val activityId: Int,
        private val activityRepository: EventActivityRepository,
        private val placeRepository: PlaceRepository,
        private val roleRepository: RoleRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EventActivityViewModel::class.java)) {
                return EventActivityViewModel(
                    activityId,
                    activityRepository,
                    placeRepository,
                    roleRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}
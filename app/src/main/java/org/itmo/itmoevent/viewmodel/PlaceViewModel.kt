package org.itmo.itmoevent.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import org.itmo.itmoevent.model.data.entity.enums.PrivilegeName
import org.itmo.itmoevent.model.repository.PlaceRepository
import org.itmo.itmoevent.model.repository.RoleRepository

class PlaceViewModel(
    private val placeId: Int,
    private val placeRepository: PlaceRepository,
    roleRepository: RoleRepository
) : ViewModel() {

    val placeLiveData = ContentLiveDataProvider(
        !roleRepository.systemPrivilegesNames!!.contains(PrivilegeName.VIEW_EVENT_PLACE),
        viewModelScope
    ) {
        viewModelScope.async {
            placeRepository.getPlace(placeId)
        }
    }.contentLiveData

    class PlaceViewModelModelFactory(
        private val placeId: Int,
        private val placeRepository: PlaceRepository,
        private val roleRepository: RoleRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PlaceViewModel::class.java)) {
                return PlaceViewModel(
                    placeId,
                    placeRepository,
                    roleRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }

}
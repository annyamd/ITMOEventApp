package org.itmo.itmoevent.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import org.itmo.itmoevent.databinding.FragmentPlaceBinding
import org.itmo.itmoevent.model.data.entity.Place
import org.itmo.itmoevent.view.fragments.base.BaseFragment
import org.itmo.itmoevent.viewmodel.PlaceViewModel

class PlaceFragment : BaseFragment<FragmentPlaceBinding>() {
    private var placeId: Int? = null
    private var model: PlaceViewModel? = null
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentPlaceBinding
        get() = { inflater, container, attach ->
            FragmentPlaceBinding.inflate(inflater, container, attach)
        }

    override fun setup(view: View, savedInstanceState: Bundle?) {
        val placeId = requireArguments().getInt(PLACE_ID_ARG)
        this.placeId = placeId

        val model: PlaceViewModel by viewModels {
            PlaceViewModel.PlaceViewModelModelFactory(
                placeId,
                application.placeRepository,
                application.roleRepository
            )
        }
        this.model = model

        viewBinding.run {
            handleContentItemViewByLiveData(
                model.placeLiveData,
                placeContent,
                placeProgressBarMain.root,
                bindContent = ::bindPlace
            )
        }
    }

    private fun bindPlace(place: Place) {
        viewBinding.run {
            placeName.text = place.name
            placeAddress.text = place.description
            placeFormat.text = place.format
            placeRoom.text = place.room
            placeDesc.text = place.description
        }
    }

    companion object {
        const val PLACE_ID_ARG: String = "placeId"
    }

}
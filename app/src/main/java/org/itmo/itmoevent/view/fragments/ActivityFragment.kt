package org.itmo.itmoevent.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import org.itmo.itmoevent.databinding.EventInfoBinding
import org.itmo.itmoevent.databinding.FragmentActivBinding
import org.itmo.itmoevent.databinding.PlaceItemBinding
import org.itmo.itmoevent.model.data.entity.EventsActivity
import org.itmo.itmoevent.model.data.entity.Place
import org.itmo.itmoevent.view.fragments.binding.ActivityInfoContentBinding
import org.itmo.itmoevent.view.fragments.binding.ContentBinding
import org.itmo.itmoevent.view.fragments.binding.PlaceItemContentBinding
import org.itmo.itmoevent.viewmodel.EventActivityViewModel
import org.itmo.itmoevent.viewmodel.MainViewModel

class ActivityFragment : BaseFragment<FragmentActivBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentActivBinding
        get() = { inflater, container, attach ->
            FragmentActivBinding.inflate(inflater, container, attach)
        }

    private var activityId: Int? = null
    private val mainViewModel: MainViewModel by activityViewModels()
    private val placeContentBinding: ContentBinding<PlaceItemBinding, Place> =
        PlaceItemContentBinding()
    private val activityInfoContentBinding: ContentBinding<EventInfoBinding, EventsActivity> by lazy {
        ActivityInfoContentBinding(requireActivity())
    }

    override fun setup(view: View, savedInstanceState: Bundle?) {
        val activityId = requireArguments().getInt(ACTIVITY_ID_ARG)
        this.activityId = activityId

        val model: EventActivityViewModel by viewModels {
            EventActivityViewModel.ActivityViewModelModelFactory(
                activityId,
                application.eventActivityRepository,
                application.placeRepository,
                application.roleRepository
            )
        }

        viewBinding.run {
            handleContentItemViewByLiveData(
                model.activityInfoLiveData,
                activityContent,
                activityProgressBarMain.root,
                bindContent = ::bindActivityInfo
            )

            handleContentItemViewByLiveData(
                model.placeLiveData,
                activityInfo.eventPlaceCard.root,
                bindContent = ::bindPlace
            )
        }
    }

    private fun bindPlace(place: Place) {
        viewBinding.activityInfo.run {
            placeContentBinding.bindContentToView(eventPlaceCard, place)
            eventChipPlace.text = place.name

            eventPlaceCard.root.setOnClickListener {
                mainViewModel.selectPlace(place.id)
            }
        }
    }

    private fun bindActivityInfo(activity: EventsActivity) {
        activityInfoContentBinding.bindContentToView(viewBinding.activityInfo, activity)
    }

    companion object {
        const val ACTIVITY_ID_ARG: String = "activityId"
    }

}

package org.itmo.itmoevent.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import org.itmo.itmoevent.R
import org.itmo.itmoevent.databinding.FragmentActivBinding
import org.itmo.itmoevent.model.data.entity.EventsActivity
import org.itmo.itmoevent.model.data.entity.Place
import org.itmo.itmoevent.view.util.DisplayDateFormats
import org.itmo.itmoevent.viewmodel.EventActivityViewModel
import org.itmo.itmoevent.viewmodel.MainViewModel
import java.time.format.DateTimeFormatter

class ActivityFragment : BaseFragment<FragmentActivBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentActivBinding
        get() = { inflater, container, attach ->
            FragmentActivBinding.inflate(inflater, container, attach)
        }

    private var activityId: Int? = null
    private val mainViewModel: MainViewModel by activityViewModels()


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
            eventChipPlace.text = place.name
            eventPlaceCard.placeItemTitle.text = place.name
            eventPlaceCard.placeItemDesc.text = place.description
            eventPlaceCard.placeItemFormat.text = place.format

            eventPlaceCard.root.setOnClickListener {
                mainViewModel.selectPlace(place.id)
            }
        }
    }

    private fun bindActivityInfo(activity: EventsActivity) {
        viewBinding.activityInfo.run {
            val formatter =
                DateTimeFormatter.ofPattern(DisplayDateFormats.DATE_EVENT_FULL)
            activity.run {
                eventTitle.text = title
                eventShortDesc.text = shortDesc
                eventDescLong.text = longDesc
                eventChipStatus.text = status
                eventChipTime.text = startDate.format(formatter)
                eventDetailsTimeHold.text =
                    getString(
                        R.string.event_duration,
                        startDate.format(formatter),
                        endDate.format(formatter)
                    )
                eventDetailsTimeRegister.text =
                    getString(
                        R.string.event_duration,
                        regStart.format(formatter),
                        regEnd.format(formatter)
                    )
                eventDetailsTimePrepare.text =
                    getString(
                        R.string.event_duration,
                        preparingStart.format(formatter),
                        preparingEnd.format(formatter)
                    )
                eventDetailsAge.text = participantAgeLowest.toString()
                eventDetailsParticipantsMax.text = participantLimit.toString()
            }
        }
    }

    companion object {
        const val ACTIVITY_ID_ARG: String = "activityId"
    }

}
